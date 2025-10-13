package com.theblood.productservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.common.exception.custom.InvalidDataException;
import com.theblood.productservice.dto.request.RelateProductRequest;
import com.theblood.productservice.dto.response.ProductDetail;
import com.theblood.productservice.mapper.ProductMapper;
import com.theblood.productservice.model.Product;
import com.theblood.productservice.repository.*;
import com.theblood.productservice.repository.projection.ProductProjection;
import com.theblood.productservice.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductServiceImpl implements ProductService {

    String REDIS_CACHE_RELATE_RESULT = "related_product:";
    String REDIS_CACHE_KEY = "product_relate_request_queue";

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    ProductMapper productMapper;
    ProductCategoryRepository productCategoryRepository;
    //    UserRepository userRepository;
    //    ShopRepository shopRepository;
    //    CategoryRepository categoryRepository;
    ProductJDBCRepository productJDBCRepository;
    RedisServiceWrapper redisServiceWrapper;
    ProductCacheManager productCacheManager;
    ProductCacheService productCacheService;
    FeedbackRepository feedbackRepository;
    @Qualifier("redisObjectMapper")
    ObjectMapper objectMapper;

    @Override
    public Page<ProductDetail> getAllProductDetails(Pageable pageable) {
        Page<ProductProjection> projections = productRepository.findListProduct(pageable);
        return projections.map(productMapper::toProductDetail);
    }

    @Override
    public List<ProductDetail> getAllProductDetails() {
        return productMapper.toProductDetail(productRepository.findAll());
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
        return productRepository.findProductBySku(shopId, sku).isEmpty();
    }

    public boolean isProductExist(UUID shopId) {
        return productRepository.findProductByShopId(shopId).isEmpty();
    }
//
//    @PreAuthorize("hasRole('SHOP_OWNER') and hasAuthority('product:create')")
//    @Override
//    @Transactional
//    public Product addProduct(ProductRequest productRequest) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String currentUser = authentication.getName(); // Lấy tên người dùng hiện tại
//
//        Optional<List<String>> userShop = userRepository.findUsernamesByShopId(productRequest.getShopId());
////        Optional<User> user =  userRepository.findByUsername(currentUser);
////        String userShop = user.get().getShop().getId();
//        if (userShop.isEmpty()) throw new InvalidDataException("User not found");
//
//
//        if (!userShop.get().contains(currentUser)) {
//            throw new InvalidDataException("User does not have a shop or shop ID mismatch");
//        }
//        //  productRequest.setShopId();
//
//        // if (!categoryRepository.existsById(productRequest.getCategoryName())) throw new InvalidDataException("Categories not found");
//
//        Shop shop = shopRepository.findById(productRequest.getShopId())
//                .orElseThrow(() -> new InvalidDataException("Shop not found"));
//        if (shop.getShopStatus() == ShopStatus.ACTIVE) throw new InvalidDataException("Shop is not active");
//
//        Product product =
//                productMapper.toProduct(productRequest);
//        if (isProductExist(productRequest.getShopId(), productRequest.getSku())) {
//            throw new InvalidDataException("Product already exists");
//        }
//        product.setShop(shop);
//        // process categories
//        //
//        List<String> categoriesNames;
//        if (productRequest.getCategoryNames().contains(",")) {
//            categoriesNames = Arrays.stream(productRequest.getCategoryNames().split(","))
//                    .map(String::trim) // Loại bỏ khoảng trắng thừa
//                    .collect(Collectors.toList());
//        } else {
//            categoriesNames = List.of(productRequest.getCategoryNames().trim());
//        }
//
//        List<Categories> categories = categoryRepository.findAllById(categoriesNames);
//
//        if (categories.isEmpty()) throw
//                new InvalidDataException("invalid Categories ");
//
//        product.setProductCategories(categories.stream().map(cat -> {
//            ProductCategory pc = new ProductCategory();
//            pc.setCategories(cat);
//            pc.setProduct(product);
//            return pc;
//        }).collect(Collectors.toSet()));
//
//        product.setProductStatus(productRequest.getStatus());
//
//
//        return productRepository.save(product);
//    }
//
//    @Override
//    @PreAuthorize("hasRole('SHOP_OWNER') and hasAuthority('product:upadte')")
//    @Transactional
//    public Product updateProduct(UUID productId, ProductRequest productRequest) {
//        Product productToUpdate = productRepository.findById(productId)
//                .orElseThrow(() -> new InvalidDataException("Product not found with ID: " + productId));
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String currentUsername = authentication.getName();
//
//        List<String> authorizedUsers = userRepository.findUsernamesByShopId(productToUpdate.getShop().getId())
//                .orElseThrow(() -> new InvalidDataException("Shop of the product not found or has no users"));
//
//        if (!authorizedUsers.contains(currentUsername)) {
//            throw new InvalidDataException("User is not authorized to update this product.");
//        }
//
//        productMapper.updateProductFromDto(productRequest, productToUpdate);
//
//        if (productRequest.getCategoryNames() != null && !productRequest.getCategoryNames().isBlank()) {
//            // XÓA CÁC LIÊN KẾT HIỆN TẠI
//            productToUpdate.getProductCategories().clear();
//
//            List<String> categoriesNames;
//            if (productRequest.getCategoryNames().contains(",")) {
//                categoriesNames = Arrays.stream(productRequest.getCategoryNames().split(","))
//                        .map(String::trim)
//                        .collect(Collectors.toList());
//            } else {
//                categoriesNames = List.of(productRequest.getCategoryNames().trim());
//            }
//
//
//            List<Categories> newCategories = categoryRepository.findAllById(categoriesNames);
//
//            if (newCategories.size() != categoriesNames.size()) {
//                throw new InvalidDataException("One or more categories not found.");
//            }
//            for (Categories cat : newCategories) {
//                ProductCategory pc = new ProductCategory();
//                pc.setCategories(cat);
//                pc.setProduct(productToUpdate);
//                productToUpdate.getProductCategories().add(pc);
//            }
//        }
//
//        if (productRequest.getStatus() != null) {
//            productToUpdate.setProductStatus(productRequest.getStatus());
//        }
//
//        Product updatedProduct = productRepository.save(productToUpdate);
//
//        // Invalidate cache after successful update
//        productCacheManager.invalidateProductCache(productId);
//
//        return updatedProduct;
//    }
//
//    @PreAuthorize("hasRole('SHOP_OWNER') and hasAuthority('product:delete')")
//    @Override
//    @Transactional
//    public void deleteProduct(UUID productId) {
//        if (productRepository.findProductDetailById(productId).isPresent()) {
//            productRepository.deleteProductById(productId);
//
//            // Invalidate cache after successful deletion
//            productCacheManager.invalidateProductCache(productId);
//        } else {
//            throw new InvalidDataException("Product not found");
//        }
//    }
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