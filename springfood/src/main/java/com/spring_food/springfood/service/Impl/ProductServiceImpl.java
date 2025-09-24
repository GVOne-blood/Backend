package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.common.enums.SearchKeyword;
import com.spring_food.springfood.common.enums.ShopStatus;
import com.spring_food.springfood.dto.request.ProductRequest;
import com.spring_food.springfood.dto.request.SearchCriteria;
import com.spring_food.springfood.dto.response.ProductDetail;
import com.spring_food.springfood.exception.custom.InvalidDataException;
import com.spring_food.springfood.mapper.ProductMapper;
import com.spring_food.springfood.model.Categories;
import com.spring_food.springfood.model.Product;
import com.spring_food.springfood.model.ProductCategory;
import com.spring_food.springfood.model.Shop;
import com.spring_food.springfood.repository.CategoryRepository;
import com.spring_food.springfood.repository.ProductRepository;
import com.spring_food.springfood.repository.ShopRepository;
import com.spring_food.springfood.repository.UserRepository;
import com.spring_food.springfood.repository.impl.ProductJDBCRepository;
import com.spring_food.springfood.service.ProductService;
import com.spring_food.springfood.specification.SearchSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    UserRepository userRepository;
    ShopRepository shopRepository;
    ProductMapper productMapper;
    CategoryRepository categoryRepository;

    ProductJDBCRepository productJDBCRepository;

    @Override
    public Page<ProductDetail> getAllProductDetails(Pageable pageable) {
        Page<ProductDetail> products = productRepository.findListProduct(pageable);
        return products;
    }

    @Override
    public boolean isProductExists(String productId) {
        return productRepository.existsById(productId);
    }

    @Override
    public ProductDetail getProductDetailById(String productId) {

        return productMapper.toProductDetail(productJDBCRepository.findById(productId).get()); // JDBC template
        // return productRepository.findProductDetailById(productId).orElseThrow(() -> new InvalidDataException("Product not found"));
    }

    public boolean isProductExist(String shopId, String sku) {
        return productRepository.findProductBySku(shopId, sku).isEmpty();
    }

    public boolean isProductExist(String shopId) {
        return productRepository.findProductByShopId(shopId).isEmpty();
    }

    @PreAuthorize("hasRole('SHOP_OWNER') and hasAuthority('product:create')")
    @Override
    @Transactional
    public Product addProduct(ProductRequest productRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication.getName(); // Lấy tên người dùng hiện tại

        Optional<List<String>> userShop = userRepository.findUsernamesByShopId(productRequest.getShopId());
//        Optional<User> user =  userRepository.findByUsername(currentUser);
//        String userShop = user.get().getShop().getId();
        if (userShop.isEmpty()) throw new InvalidDataException("User not found");


        if (!userShop.get().contains(currentUser)) {
            throw new InvalidDataException("User does not have a shop or shop ID mismatch");
        }
        //  productRequest.setShopId();

        // if (!categoryRepository.existsById(productRequest.getCategoryName())) throw new InvalidDataException("Categories not found");

        Shop shop = shopRepository.findById(productRequest.getShopId())
                .orElseThrow(() -> new InvalidDataException("Shop not found"));
        if (shop.getShopStatus() == ShopStatus.ACTIVE) throw new InvalidDataException("Shop is not active");

        Product product =
                productMapper.toProduct(productRequest);
        if (isProductExist(productRequest.getShopId(), productRequest.getSku())) {
            throw new InvalidDataException("Product already exists");
        }
        product.setShop(shop);
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

        product.setProductCategories(categories.stream().map(cat -> {
            ProductCategory pc = new ProductCategory();
            pc.setCategories(cat);
            pc.setProduct(product);
            return pc;
        }).collect(Collectors.toSet()));

        product.setProductStatus(productRequest.getStatus());


        return productRepository.save(product);
    }

    @Override
    @PreAuthorize("hasRole('SHOP_OWNER') and hasAuthority('product:upadte')")
    @Transactional
    public Product updateProduct(String productId, ProductRequest productRequest) {
        Product productToUpdate = productRepository.findById(productId)
                .orElseThrow(() -> new InvalidDataException("Product not found with ID: " + productId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        List<String> authorizedUsers = userRepository.findUsernamesByShopId(productToUpdate.getShop().getId())
                .orElseThrow(() -> new InvalidDataException("Shop of the product not found or has no users"));

        if (!authorizedUsers.contains(currentUsername)) {
            throw new InvalidDataException("User is not authorized to update this product.");
        }

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

        return productRepository.save(productToUpdate);
    }

    @PreAuthorize("hasRole('SHOP_OWNER') and hasAuthority('product:delete')")
    @Override
    @Transactional
    public void deleteProduct(String productId) {
        if (productRepository.findProductDetailById(productId).isPresent()) {
            productRepository.deleteProductById(productId);
        } else {
            throw new InvalidDataException("Product not found");
        }
    }

    @Override
    public Page<ProductDetail> findByPrice(String from, String to, Pageable pageable) {

        BigDecimal priceFrom;
        BigDecimal priceTo;
        try {
            priceFrom = BigDecimal.valueOf(Double.parseDouble(from));
            priceTo = BigDecimal.valueOf(Double.parseDouble(to));
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Data sai");
        }
        if (priceFrom.compareTo(priceTo) > 0) throw new InvalidDataException("From must be not greater than to");

        //Criteria
//        Page<Product> res = productRepository.findByPrice(priceFrom, priceTo, pageable);
//
//        return res.map(productMapper::toProductDetail);

//        //Spec
        Specification<Product> spec = SearchSpecification.between(SearchKeyword.price.name(), priceFrom, priceTo);
        Page<Product> res = productRepository.findAll(spec, pageable);
        return res.map(productMapper::toProductDetail);

        //NamedJdbcTemplate
//        Page<Product> res = productJDBCRepository.findByPrice(priceFrom, priceTo, pageable);
//        return res.map(productMapper::toProductDetail);
    }


    /**
     * Dynamic search method for products using flexible search criteria
     *
     * @param pageable pagination information
     * @param params   varargs containing search criteria in format "field+operation+value"
     *                 Examples:
     *                 - "quantity>10" - find products with quantity greater than 10
     *                 - "price<=100" - find products with price less than or equal to 100
     *                 - "name:laptop" - find products with name containing "laptop"
     *                 - "status=ACTIVE" - find products with status equals to ACTIVE
     *                 - "price>=50", "quantity<100" - multiple criteria (AND condition)
     *                 <p>
     *                 Supported operations:
     *                 - "=" : equals
     *                 - "!=" : not equals
     *                 - ">" : greater than
     *                 - ">=" : greater than or equal
     *                 - "<" : less than
     *                 - "<=" : less than or equal
     *                 - ":" : contains (for string fields)
     * @return Page of ProductDetail matching the search criteria
     */

    @PreAuthorize("hasRole('ADMIN')")
    public Page<ProductDetail> search(Pageable pageable, Map<String, String> params) {

        if (params.isEmpty()) {
            return productRepository.findAll(pageable).map(productMapper::toProductDetail);
        }

        // Regex pattern to parse search criteria: keyword = operation + value
        // Examples: "quantity=>10", "price=<=100", "name=:laptop", "status==ACTIVE", "price=~50-200"
        // Pattern breakdown:
        // (!=|<=|>=|[:=<>~]) - captures operation (order matters for multi-char ops)
        // (.+) - captures the value (any characters)

        Pattern pattern = Pattern.compile("^(!=|<=|>=|[:=<>~])(.+)$");

        List<SearchCriteria> searchParams = new ArrayList<>();
        Specification<Product> spec = null;
        for (Map.Entry<String, String> entry : params.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();

            if (key.equals("page") || key.equals("size") || key.equals("sort")) continue;

            Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
                SearchCriteria searchParam = new SearchCriteria();
                searchParam.setKeyword(key);
                searchParam.setOperation(matcher.group(1));
                searchParam.setValue(matcher.group(2));
                searchParams.add(searchParam);
            }
        }

        // Build specifications based on search parameters

        for (SearchCriteria searchParam : searchParams) {
            Specification<Product> currentSpec = SearchSpecification.buildSpecification(searchParam);
            if (spec == null) {
                spec = currentSpec;
            } else {
                spec = spec.and(currentSpec);
            }
        }

        // If no valid search params, return all products
        if (spec == null) {
            return productRepository.findAll(pageable).map(productMapper::toProductDetail);
        }

        Page<Product> products = productRepository.findAll(spec, pageable);

        return products.map(productMapper::toProductDetail);
    }

}
