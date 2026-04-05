package com.theblood.productservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.minio.core.MinioClientCustom;
import com.theblood.minio.response.MinIOResponse;
import com.theblood.productservice.domain.Categories;
import com.theblood.productservice.domain.Product;
import com.theblood.productservice.domain.ProductCategory;
import com.theblood.productservice.domain.ProductImages;
import com.theblood.productservice.exception.custom.InvalidExcelFormatException;
import com.theblood.productservice.kafka.consumer.ProductServiceConsumer;
import com.theblood.productservice.kafka.service.OutboxService;
import com.theblood.productservice.repository.*;
import com.theblood.productservice.repository.projection.ProductProjection;
import com.theblood.productservice.resources.grpc.client_role.ProductGrpcClient;
import com.theblood.productservice.service.ProductService;
import com.theblood.productservice.service.ProductVariantsService;
import com.theblood.productservice.service.dto.UploadResult;
import com.theblood.productservice.service.dto.request.ProductRequest;
import com.theblood.productservice.service.dto.request.RelateProductRequest;
import com.theblood.productservice.service.dto.response.ProductImageResponse;
import com.theblood.productservice.service.mapper.ProductMapper;
import com.theblood.productservice.util.ProductExcelUtil;
import com.theblood.springfood.client.service.LoggingService;
import com.theblood.springfood.common.dto.response.ProductDetail;
import com.theblood.springfood.common.enums.FileStatus;
import com.theblood.springfood.common.enums.MimeType;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import com.theblood.springfood.common.grpc.ValidateProductCreationResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

@Slf4j
public class ProductServiceImpl implements ProductService {


    @Qualifier("uploadFileExecutor")
    private final ExecutorService uploadFileExecutor;
    private final ProductImagesRepository productImagesRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MinioClientCustom minioClientCustom;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductServiceConsumer productServiceConsumer;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductJDBCRepository productJDBCRepository;
    private final RedisServiceWrapper redisServiceWrapper;
    private final ProductCacheManager productCacheManager;
    private final ProductCacheService productCacheService;
    private final OutboxService outboxService;
    private final FeedbackRepository feedbackRepository;
    private final ProductGrpcClient productGrpcClient;
    private final ProductVariantsService productVariantsService;
    private final LoggingService loggingService;

    @Qualifier("redisObjectMapper")
    private final ObjectMapper objectMapper;
    @Value("${minio.bucket}")
    String MINIO_BUCKET_NAME;
    @Value("${minio.max-file-size}")
    Long MINIO_MAX_FILE_SIZE = 52428800L; // 50MB
    String REDIS_CACHE_RELATE_RESULT = "related_product:";
    String REDIS_CACHE_KEY = "product_relate_request_queue";
    int MAX_FILES_PER_PRODUCT = 11;

    List<String> EXCEL_HEADRES = List.of("shopId", "categoryName", "name", "description", "price", "images", "quantity", "sku", "msg", "exp", "wholesalePrice");

    @Override
    public Page<ProductDetail> getAllProductDetails(Pageable pageable) {
        Page<ProductProjection> projections = productRepository.findListProduct(pageable);
        return projections.map(productMapper::toProductDetail);
    }

    @Override
    public List<ProductDetail> getAllProductDetails() {
        // return productMapper.toProductDetail(productRepository.findAll());
        return productMapper.toProductDetail(productRepository.findTop300ByOrderByUpdatedAtDesc());

    }

    @Override
    public List<ProductDetail> getAllLastUpdatedProducts(LocalDateTime lastModifiedAt) {
        return productMapper.toProductDetail(productRepository.findAllByUpdatedAtGreaterThan(lastModifiedAt));
    }

    public Page<ProductDetail> getProductsByIds(List<UUID> productIds, Pageable pageable) {
        Page<ProductProjection> projections = productRepository.findByIds(productIds, pageable);
        return projections.map(productMapper::toProductDetail);
    }

    @Override
    public Page<ProductDetail> getListProductsRelated(Pageable pageable, UUID productId) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) throw new InvalidDataException("Product not found");

        // Check Redis first
        String redisKey = "related_products:" + productId;
        List<UUID> relatedProductIds = redisServiceWrapper.getUuidList(redisKey);

        if (relatedProductIds != null && !relatedProductIds.isEmpty()) {
            // Get products by ID
            return getProductsByIds(relatedProductIds, pageable);
        }

        try {
            List<String> categoryNames = product.get().getProductCategories().stream()
                    .map(pc -> pc.getCategories().getName())
                    .collect(Collectors.toList());
            RelateProductRequest request = new RelateProductRequest(productId, categoryNames);
            Future<?> future = productCacheService.submitProductRelateTask(request);
            future.get(10, TimeUnit.SECONDS);

            // Read from cache after task completion
            relatedProductIds = redisServiceWrapper.getUuidList(redisKey);
            if (relatedProductIds != null && !relatedProductIds.isEmpty()) {
                return getProductsByIds(relatedProductIds, pageable);
            } else {
                throw new RuntimeException("Failed to cache related products");
            }
        } catch (Exception e) {
            log.error("Error fetching related products for {}: {}", productId, e.getMessage());
            // Fallback to direct DB query
            Page<ProductProjection> projections = productCategoryRepository.findAllProductsByCategoryName(productId, pageable);
            Page<ProductDetail> productRelated = projections.map(productMapper::toProductDetail);
            List<UUID> productRelatedIds = productRelated.stream()
                    .map(ProductDetail::getId)
                    .collect(Collectors.toList());
            redisServiceWrapper.setUuidList(redisKey, productRelatedIds);
            redisServiceWrapper.setTimeout(redisKey, 1, TimeUnit.HOURS);
            return productRelated;
        }
    }

    // without pagination & redis queue
    @Override
    public List<ProductDetail> getListProductsRelated(UUID productId, int limit) {
        if (productId.toString().isEmpty()) throw new InvalidDataException("Product not found");
        List<String> categoryNames = productRepository.findById(productId)
                .orElseThrow(() -> new InvalidDataException("Product not found"))
                .getProductCategories().stream()
                .map(pc -> pc.getCategories().getName())
                .collect(Collectors.toList());
        return productRepository.findRandomRelatedProducts(
                categoryNames,
                productId,
                limit
        ).stream().map(productMapper::toProductDetail).collect(Collectors.toList());

    }

    @Override
    public boolean isProductExists(UUID productId) {
        return productRepository.existsById(productId);
    }

    @Override
    public ProductDetail getProductDetailById(UUID productId) {
        String productRedisKey = "product_detail:" + productId;
        Object rawValue = redisServiceWrapper.getValue(productRedisKey);

        if (rawValue != null) {
            ProductDetail cachedProduct;
            if (rawValue instanceof ProductDetail) {
                cachedProduct = (ProductDetail) rawValue;
            } else {
                // Deserialize from LinkedHashMap or other serialized form
                cachedProduct = objectMapper.convertValue(rawValue, ProductDetail.class);
            }
            return cachedProduct;
        }

        // Cache miss, submit task to fetch and cache
        try {
            Future<?> future = productCacheService.submitProductDetailTask(productId);
            future.get(10, TimeUnit.SECONDS); // Wait for completion

            // Submit related products task asynchronously
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                List<String> categoryNames = productOpt.get().getProductCategories().stream()
                        .map(pc -> pc.getCategories().getName())
                        .collect(Collectors.toList());
                RelateProductRequest request = new RelateProductRequest(productId, categoryNames);
                productCacheService.submitProductRelateTask(request); // Async, no wait
            }

            // Read from cache after task completion
            rawValue = redisServiceWrapper.getValue(productRedisKey);
            if (rawValue != null) {
                if (rawValue instanceof ProductDetail) {
                    return (ProductDetail) rawValue;
                } else {
                    return objectMapper.convertValue(rawValue, ProductDetail.class);
                }
            } else {
                throw new RuntimeException("Failed to cache product detail");
            }
        } catch (Exception e) {
            log.error("Error fetching product detail for {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to fetch product detail", e);
        }
    }

    public boolean isProductExist(UUID shopId, String sku) {
        return productRepository.findProductBySku(shopId, sku).isPresent();
    }

    public boolean isProductExist(UUID shopId) {
        return productRepository.findProductByShopId(shopId).isEmpty();
    }

    // @PreAuthorize("hasRole('SHOP_OWNER') and hasAuthority('product:create')")
    @Override
    @Transactional
    public Product addProduct(ProductRequest productRequest) throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication.getName(); // Lấy tên người dùng hiện tại

        // validate using gRPC
        ValidateProductCreationResponse validationResponse = productGrpcClient.validateProduct(
                productRequest.getSku(),
                productRequest.getShopId().toString(),
                currentUser,
                productRequest.getCategoryNames()
        );

        if (!validationResponse.getIsValid()) {
            throw new InvalidDataException("Product validation failed: " + validationResponse.getMessage());
        }


//        Optional<List<String>> userShop = userRepository.findUsernamesByShopId(productRequest.getShopId());
//        Optional<User> user =  userRepository.findByUsername(currentUser);
//        String userShop = user.get().getShop().getId();
//        if (userShop.isEmpty()) throw new InvalidDataException("User not found");
//        if (!userShop.get().contains(currentUser)) {
//            throw new InvalidDataException("User does not have a shop or shop ID mismatch");
//        }
        //  productRequest.setShopId();

        // if (!categoryRepository.existsById(productRequest.getCategoryName())) throw new InvalidDataException("Categories not found");
        // create a product. save invalidate field
        Product product =
                productMapper.toProduct(productRequest);
        // process categories
        //
        List<String> categoriesNames;
        if (productRequest.getCategoryNames().contains(",")) {
            categoriesNames = Arrays.stream(productRequest.getCategoryNames().split(","))
                    .map(String::trim) // Loại bỏ khoảng trắng thừa
                    .collect(Collectors.toList());
        } else {
            categoriesNames = List.of(productRequest.getCategoryNames().trim());
        }

        List<Categories> categories = categoryRepository.findAllById(categoriesNames);

        if (categories.isEmpty()) throw
                new InvalidDataException("invalid Categories ");

//        product.setProductCategories(categories.stream().map(cat -> {
//            ProductCategory pc = new ProductCategory();
//            pc.setCategories(cat);
//            pc.setProduct(product);
//            return pc;
//        }).collect(Collectors.toSet()));

        product.setProductStatus(productRequest.getStatus());
        if (isProductExist(productRequest.getShopId(), productRequest.getSku())) {
            throw new InvalidDataException("Product already exists");
        }
        product.setShopId(productRequest.getShopId());
        return productRepository.save(product);
    }

    @Transactional
    public List<ProductDetail> addProductsBatch(List<ProductRequest> productRequests) {
        List<Product> products = new ArrayList<>();

        for (ProductRequest request : productRequests) {
            Product product = productMapper.toProduct(request);
            // Thêm logic validation, set relationships, etc.
            products.add(product);
        }

        List<Product> savedProducts = productRepository.saveAll(products);

        return savedProducts.stream()
                .map(productMapper::toProductDetail)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ProductDetail> addProductsByExcel(MultipartFile file) throws IOException {
        validateExcelFile(file);
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet dataSheet = workbook.getSheetAt(0);
        validateExcelHeader(dataSheet.getRow(0));

        List<ProductRequest> productRequests = new ArrayList<>();

        for (int i = 1; i < dataSheet.getLastRowNum(); i++) {
            Row row = dataSheet.getRow(i);
            if (row.toString().isEmpty()) continue;
            String rawImages = "";
            Cell imageCell = row.getCell(5);
            if (imageCell != null) {
                rawImages = imageCell.getStringCellValue();
            }

            // 2. Chuyển đổi thành JSON String hợp lệ
            // Logic: Tách chuỗi bằng dấu phẩy (nếu có), trim khoảng trắng, đưa vào List
            List<String> imageList = new ArrayList<>();
            if (rawImages != null && !rawImages.isBlank()) {
                if (rawImages.contains(",")) {
                    // Trường hợp nhiều ảnh: "url1, url2, url3"
                    String[] splitImages = rawImages.split(",");
                    for (String img : splitImages) {
                        imageList.add(img.trim());
                    }
                } else {
                    // Trường hợp 1 ảnh duy nhất
                    imageList.add(rawImages.trim());
                }
            }

            // 3. Serialize List thành JSON String: ["url1", "url2"]
            // Lưu ý: objectMapper đã được inject sẵn trong class của bạn
            String jsonImagesObj = objectMapper.writeValueAsString(imageList);
            try {

                ProductRequest productRequest = ProductRequest.builder()
                        .shopId(ProductExcelUtil.CellUtils.getCellValueAsUUID(row.getCell(0)))
                        .categoryNames(row.getCell(1).getStringCellValue())
                        .name(row.getCell(2).getStringCellValue())
                        .description(row.getCell(3).getStringCellValue())
                        .price(row.getCell(4).getStringCellValue())
                        .images(jsonImagesObj)
                        .quantity(ProductExcelUtil.CellUtils.getCellValueAsInteger(row.getCell(6)))
                        .sku(row.getCell(7).getStringCellValue())
                        .msg(ProductExcelUtil.CellUtils.getCellValueAsLocalDate(row.getCell(8)))
                        .exp(ProductExcelUtil.CellUtils.getCellValueAsLocalDate(row.getCell(9)))
                        .wholesalePrice(row.getCell(10).getStringCellValue())
                        .avgRate(ProductExcelUtil.CellUtils.getCellValueAsBigDecimal(row.getCell(11)))
                        .build();
                productRequests.add(productRequest);
            } catch (InvalidDataException e) {
                log.error(e.getMessage());
                throw new InvalidDataException(e.getMessage());
            }
        }
        return addProductsBatch(productRequests);

    }

    @Override
    @PreAuthorize("hasRole('SHOP_OWNER') and hasAuthority('product:upadte')")
    @Transactional
    public Product updateProduct(UUID productId, ProductRequest productRequest) {
        Product productToUpdate = productRepository.findById(productId)
                .orElseThrow(() -> new InvalidDataException("Product not found with ID: " + productId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();


        kafkaTemplate.send("product-update-request", productToUpdate.getShopId());


//        List<String> authorizedUsers = userRepository.findUsernamesByShopId(productToUpdate.getShop().getId())
//                .orElseThrow(() -> new InvalidDataException("Shop of the product not found or has no users"));
//
//        if (!authorizedUsers.contains(currentUsername)) {
//            throw new InvalidDataException("User is not authorized to update this product.");
//        }

        productMapper.updateProductFromDto(productRequest, productToUpdate);

        if (productRequest.getCategoryNames() != null && !productRequest.getCategoryNames().isBlank()) {
            // XÓA CÁC LIÊN KẾT HIỆN TẠI
            productToUpdate.getProductCategories().clear();

            List<String> categoriesNames;
            if (productRequest.getCategoryNames().contains(",")) {
                categoriesNames = Arrays.stream(productRequest.getCategoryNames().split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
            } else {
                categoriesNames = List.of(productRequest.getCategoryNames().trim());
            }


            List<Categories> newCategories = categoryRepository.findAllById(categoriesNames);

            if (newCategories.size() != categoriesNames.size()) {
                throw new InvalidDataException("One or more categories not found.");
            }
            for (Categories cat : newCategories) {
                ProductCategory pc = new ProductCategory();
                pc.setCategories(cat);
                pc.setProduct(productToUpdate);
                productToUpdate.getProductCategories().add(pc);
            }
        }

        if (productRequest.getStatus() != null) {
            productToUpdate.setProductStatus(productRequest.getStatus());
        }

        Product updatedProduct = productRepository.save(productToUpdate);

        // Invalidate cache after successful update
        productCacheManager.invalidateProductCache(productId);

        return updatedProduct;
    }

    @PreAuthorize("hasRole('SHOP_OWNER') and hasAuthority('product:delete')")
    @Override
    @Transactional
    public void deleteProduct(UUID productId) {
        if (productRepository.findProductDetailById(productId).isPresent()) {
            productRepository.deleteProductById(productId);

            // Invalidate cache after successful deletion
            productCacheManager.invalidateProductCache(productId);
        } else {
            throw new InvalidDataException("Product not found");
        }
    }


    @Override
    public ProductImageResponse uploadImages(UUID userId, UUID productId, List<MultipartFile> files) {

        validateProductImages(files, productId);

        List<Future<UploadResult>> futures = new ArrayList<>();
        List<MinIOResponse> uploadedUrls = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();
        for (MultipartFile file : files) {

            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(file.getBytes());
                String objectName = "products/" + productId + "/" + "_" + file.getOriginalFilename();
                String originalFilename = file.getOriginalFilename();
                if (minioClientCustom.objectExists(objectName))
                    throw new IllegalArgumentException("Object already exists");
                Future<UploadResult> future = uploadFileExecutor.submit(() -> {
                    try {
                        MinIOResponse response = minioClientCustom.upload(outputStream, objectName);
                        return new UploadResult(response, originalFilename, response.getMessage(), response.getContentType());
                    } catch (Exception e) {
                        log.error("Failed to upload file: {}", originalFilename, e);
                        throw new RuntimeException("Upload failed", e);
                    }
                });

                futures.add(future);

            } catch (IOException e) {
                log.error("Failed to read file: {}", file.getOriginalFilename(), e);
                failedFiles.add(file.getOriginalFilename());
            }
        }
        // Đợi kết quả
        for (Future<UploadResult> future : futures) {
            ProductImages productImages = new ProductImages();
            productImages.setProduct_id(productId);
            productImages.setUploadedBy(String.valueOf(userId));
            productImages.setBucketName(MINIO_BUCKET_NAME);
            try {
                UploadResult response = future.get(60, TimeUnit.SECONDS);
                productImages.setImageUrl(response.getMinioResponse().getUrl());
                productImages.setStatus(FileStatus.ACTIVE);
                productImages.setObjectName(response.getMinioResponse().getObjectKey());
                productImages.setFileSize(response.getMinioResponse().getSize());
                productImages.setOriginalFileName(response.getOriginFileName());
                uploadedUrls.add(response.getMinioResponse()); // hoặc thuộc tính phù hợp

            } catch (Exception e) {
                productImages.setStatus(FileStatus.ARCHIVED);
                log.error("Upload execution failed", e);
                failedFiles.add(e.getMessage());
            }
            productImagesRepository.save(productImages);
        }

        return ProductImageResponse.builder()
                .minIOResponses(uploadedUrls)
                .saveAt(LocalDateTime.now())
                .productId(productId)
                .build();
    }

    @Override
    public void deleteProductImage(UUID productImagesId) {
        ProductImages productImages = productImagesRepository.findById(productImagesId).orElse(null);
        if (productImages == null) {
            throw new InvalidDataException("Product image not found with ID: " + productImagesId);
        }
        boolean res = minioClientCustom.deleteObject(productImages.getObjectName());
        if (res) {
            productImagesRepository.deleteById(productImagesId);
        } else {
            throw new InvalidDataException("Failed to delete image from storage for ID: " + productImagesId);
        }
    }


    private void validateExcelHeader(Row headerRow) {
        if (headerRow == null) throw new InvalidExcelFormatException("Excel header rows is null");
        ProductRequest productRequest = new ProductRequest();

        for (int i = 0; i < EXCEL_HEADRES.size(); i++) {
            Cell cell = headerRow.getCell(i);
            String headerValue = cell != null ? cell.getStringCellValue().trim() : null;
            if (!headerValue.equals(EXCEL_HEADRES.get(i))) {
                throw new InvalidExcelFormatException("Excel header  " + headerValue + " are not equal with expected " + EXCEL_HEADRES.get(i - 1));
            }
        }

    }

    private void validateExcelFile(MultipartFile file) {
        if (file.isEmpty() || file.getSize() == 0)
            throw new InvalidDataException("Excel file size is null or empty");

        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals(MimeType.CSV.getMimeType()) &&
                        !contentType.equals(MimeType.XLSX.getMimeType()) &&
                        !contentType.equals(MimeType.XLS.getMimeType()))) {
            throw new InvalidDataException("Invalid file type: " + contentType);
        }

        if (file.getOriginalFilename().length() > 255)
            throw new InvalidDataException("Excel file name is longer than 255 characters");


    }

    private void validateProductImages(List<MultipartFile> files, UUID productId) {

        for (MultipartFile file : files) {
            if (file.getName().length() > 255) {
                throw new InvalidDataException("File name too long: " + file.getName());
            }
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/gif"))) {
                throw new InvalidDataException("Invalid file type: " + contentType);
            }
            if (file.getSize() > MINIO_MAX_FILE_SIZE) { // 50MB
                throw new InvalidDataException("File size exceeds limit (50MB): " + file.getName());
            }
            if (file.isEmpty()) {
                throw new InvalidDataException("File is empty: " + file.getName());
            }
        }
        int totalExistFiles = productImagesRepository.totalFileCurrentStore(productId);
        if (files.size() + totalExistFiles > MAX_FILES_PER_PRODUCT) ;
        {
            throw new InvalidDataException("Exceeds maximum number of images per product: " + MAX_FILES_PER_PRODUCT);
        }

    }
//
//    @Override
//    public Page<ProductDetail> findByPrice(String from, String to, Pageable pageable) {
//
//        BigDecimal priceFrom;
//        BigDecimal priceTo;
//        try {
//            priceFrom = BigDecimal.valueOf(Double.parseDouble(from));
//            priceTo = BigDecimal.valueOf(Double.parseDouble(to));
//        } catch (NumberFormatException e) {
//            throw new InvalidDataException("Data sai");
//        }
//        if (priceFrom.compareTo(priceTo) > 0) throw new InvalidDataException("From must be not greater than to");
//
//        //Criteria
////        Page<Product> res = productRepository.findByPrice(priceFrom, priceTo, pageable);
////
////        return res.map(productMapper::toProductDetail);
//
////        //Spec
//        Specification<Product> spec = SearchSpecification.between(SearchKeyword.price.name(), priceFrom, priceTo);
//        Page<Product> res = productRepository.findAll(spec, pageable);
//        return res.map(productMapper::toProductDetail);
//
//        //NamedJdbcTemplate
////        Page<Product> res = productJDBCRepository.findByPrice(priceFrom, priceTo, pageable);
////        return res.map(productMapper::toProductDetail);
//    }
//
//
//    /**
//     * Dynamic search method for products using flexible search criteria
//     *
//     * @param pageable pagination information
//     * @param params   varargs containing search criteria in format "field+operation+value"
//     *                 Examples:
//     *                 - "quantity>10" - find products with quantity greater than 10
//     *                 - "price<=100" - find products with price less than or equal to 100
//     *                 - "name:laptop" - find products with name containing "laptop"
//     *                 - "status=ACTIVE" - find products with status equals to ACTIVE
//     *                 - "price>=50", "quantity<100" - multiple criteria (AND condition)
//     *                 <p>
//     *                 Supported operations:
//     *                 - "=" : equals
//     *                 - "!=" : not equals
//     *                 - ">" : greater than
//     *                 - ">=" : greater than or equal
//     *                 - "<" : less than
//     *                 - "<=" : less than or equal
//     *                 - ":" : contains (for string fields)
//     * @return Page of ProductDetail matching the search criteria
//     */
//
//    @PreAuthorize("hasRole('ADMIN')")
//    public Page<ProductDetail> search(Pageable pageable, Map<String, String> params) {
//
//        if (params.isEmpty()) {
//            return productRepository.findAll(pageable).map(productMapper::toProductDetail);
//        }
//
//        // Regex pattern to parse search criteria: keyword = operation + value
//        // Examples: "quantity=>10", "price=<=100", "name=:laptop", "status==ACTIVE", "price=~50-200"
//        // Pattern breakdown:
//        // (!=|<=|>=|[:=<>~]) - captures operation (order matters for multi-char ops)
//        // (.+) - captures the value (any characters)
//
//        Pattern pattern = Pattern.compile("^(!=|<=|>=|[:=<>~])(.+)$");
//
//        List<SearchCriteria> searchParams = new ArrayList<>();
//        Specification<Product> spec = null;
//        for (Map.Entry<String, String> entry : params.entrySet()) {
//
//            String key = entry.getKey();
//            String value = entry.getValue();
//
//            if (key.equals("page") || key.equals("size") || key.equals("sort")) continue;
//
//            Matcher matcher = pattern.matcher(value);
//            if (matcher.matches()) {
//                SearchCriteria searchParam = new SearchCriteria();
//                searchParam.setKeyword(key);
//                searchParam.setOperation(matcher.group(1));
//                searchParam.setValue(matcher.group(2));
//                searchParams.add(searchParam);
//            }
//        }
//
//        // Build specifications based on search parameters
//
//        for (SearchCriteria searchParam : searchParams) {
//            Specification<Product> currentSpec = SearchSpecification.buildSpecification(searchParam);
//            if (spec == null) {
//                spec = currentSpec;
//            } else {
//                spec = spec.and(currentSpec);
//            }
//        }
//
//        // If no valid search params, return all products
//        if (spec == null) {
//            return productRepository.findAll(pageable).map(productMapper::toProductDetail);
//        }
//
//        Page<Product> products = productRepository.findAll(spec, pageable);
//
//        return products.map(productMapper::toProductDetail);
//    }


}