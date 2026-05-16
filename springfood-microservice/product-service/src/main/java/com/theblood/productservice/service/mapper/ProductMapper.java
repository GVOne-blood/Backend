package com.theblood.productservice.service.mapper;

import com.theblood.productservice.domain.Product;
import com.theblood.productservice.repository.projection.ProductProjection;
import com.theblood.productservice.service.dto.request.ItemRequest;
import com.theblood.productservice.service.dto.request.ProductRequest;
import com.theblood.springfood.common.dto.kafka.ProductValidationRequest;
import com.theblood.springfood.common.dto.response.ProductDetail;
import com.theblood.springfood.common.dto.response.ProductDetailWithShop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    Product toProduct(ProductRequest productRequest);

    ProductValidationRequest toProductValidationRequest(ProductRequest productRequest);

    ItemRequest toItemRequest(Product product);

    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "discountPercentage", ignore = true)
    ProductDetail toProductDetail(Product product);

    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "discountPercentage", ignore = true)
    ProductDetail toProductDetail(ProductProjection projection);

    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "discountPercentage", ignore = true)
    ProductDetailWithShop toProductDetailWithShop(ProductProjection projection);

    @Mapping(source = "product", target = ".") // Ánh xạ tất cả các trường khớp tên từ 'product'
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "discountPercentage", ignore = true)
        // Ghi đè: chỉ định rõ trường 'quantity' phải lấy từ tham số 'quantity'
    ProductDetail toProductDetail(Product product, int quantity);

    List<ProductDetail> toProductDetail(List<Product> product);

    List<ProductDetail> toProductDetailFromProjection(List<ProductProjection> projections);

    List<ProductDetailWithShop> toProductDetailWithShop(List<ProductProjection> projections);

    ProductValidationRequest toProductValidationRequest(Product product);

    void updateProductFromDto(ProductRequest productRequest, @MappingTarget Product product);

}
