package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.dto.request.ProductRequest;
import com.spring_food.springfood.dto.response.ProductDetail;
import com.spring_food.springfood.exception.custom.InvalidDataException;
import com.spring_food.springfood.mapper.ProductMapper;
import com.spring_food.springfood.model.Categories;
import com.spring_food.springfood.common.enums.ShopStatus;
import com.spring_food.springfood.model.Product;
import com.spring_food.springfood.model.ProductCategory;
import com.spring_food.springfood.model.Shop;
import com.spring_food.springfood.repository.CategoryRepository;
import com.spring_food.springfood.repository.ProductRepository;
import com.spring_food.springfood.repository.ShopRepository;
import com.spring_food.springfood.repository.UserRepository;
import com.spring_food.springfood.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    @Override
    public List<ProductDetail> getAllProductDetails() {
        List<ProductDetail> products = productRepository.findListProduct();
        return products;
    }

    @Override
    public ProductDetail getProductDetailById(String productId) {
        return productRepository.findProductDetailById(productId).orElseThrow(() -> new InvalidDataException("Product not found"));
    }

    private boolean isProductExist(String productId) {
        return productRepository.findById(productId).isPresent();
    }

    private boolean isProductExist(String shopId, String sku) {
        return productRepository.findProductBySku(shopId, sku).isEmpty();
    }

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
        } ).collect(Collectors.toSet()));

        product.setProductStatus(productRequest.getStatus());


        return productRepository.save(product);
    }

    @Override
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
                        .map(String::trim) // Loại bỏ khoảng trắng thừa
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
    @Override
    @Transactional
    public void deleteProduct(String productId) {
        if (productRepository.findProductDetailById(productId).isPresent()) {
            productRepository.deleteProductById(productId);
        } else {
            throw new InvalidDataException("Product not found");
        }
    }
}
