package com.spring_food.springfood.mapper;

import com.spring_food.springfood.dto.request.ItemRequest;
import com.spring_food.springfood.dto.request.ProductRequest;
import com.spring_food.springfood.dto.response.ProductDetail;
import com.spring_food.springfood.model.OrderItem;
import com.spring_food.springfood.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

//    @Mapping(target = "id")
//    ProductDetail toProductDetail(Product product);
//
//    @Mapping(target = "id")
//    Product toProduct(ProductDetail productDetail);

    @Mapping(target = "id", ignore = true)
    Product toProduct(ProductRequest productRequest);

    ItemRequest toItemRequest(Product product);

    ProductDetail toProductDetail(Product product);

    @Mapping(source = "product", target = ".") // Ánh xạ tất cả các trường khớp tên từ 'product'
    @Mapping(source = "quantity", target = "quantity")
        // Ghi đè: chỉ định rõ trường 'quantity' phải lấy từ tham số 'quantity'
    ProductDetail toProductDetail(Product product, int quantity);

//    @Mapping(target = "quantity", ignore = true)
//    ProductDetail toProductDetailWithoutQuantity(Product product);

    List<ProductDetail> toProductDetail(List<Product> product);

    List<ProductDetail> toProductNotDetail(List<OrderItem> orderItems);


    void updateProductFromDto(ProductRequest productRequest, @MappingTarget Product product);

}
