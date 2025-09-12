package com.spring_food.springfood.mapper;

import com.spring_food.springfood.dto.request.ProductRequest;
import com.spring_food.springfood.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

//    @Mapping(target = "id")
//    ProductDetail toProductDetail(Product product);
//
//    @Mapping(target = "id")
//    Product toProduct(ProductDetail productDetail);

    @Mapping(target = "id", ignore = true)
    Product toProduct(ProductRequest productRequest);


    void updateProductFromDto(ProductRequest productRequest, @MappingTarget Product product);

}
