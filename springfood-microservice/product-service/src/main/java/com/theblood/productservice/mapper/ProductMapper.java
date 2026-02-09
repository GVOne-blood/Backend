package com.theblood.productservice.mapper;

import com.theblood.common.dto.kafka.ProductValidationRequest;
import com.theblood.common.dto.response.ProductDetail;
import com.theblood.productservice.domain.Product;
import com.theblood.productservice.dto.request.ItemRequest;
import com.theblood.productservice.dto.request.ProductRequest;
import com.theblood.productservice.repository.projection.ProductProjection;
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

    ProductValidationRequest toProductValidationRequest(ProductRequest productRequest);

    ItemRequest toItemRequest(Product product);

    ProductDetail toProductDetail(Product product);

    ProductDetail toProductDetail(ProductProjection projection);

    @Mapping(source = "product", target = ".") // Ánh xạ tất cả các trường khớp tên từ 'product'
    @Mapping(source = "quantity", target = "quantity")
        // Ghi đè: chỉ định rõ trường 'quantity' phải lấy từ tham số 'quantity'
    ProductDetail toProductDetail(Product product, int quantity);

//    @Mapping(target = "quantity", ignore = true)
//    ProductDetail toProductDetailWithoutQuantity(Product product);

    List<ProductDetail> toProductDetail(List<Product> product);

    List<ProductDetail> toProductDetailFromProjection(List<ProductProjection> projections);

    //List<ProductDetail> toProductNotDetail(List<OrderItem> orderItems);

    ProductValidationRequest toProductValidationRequest(Product product);

    void updateProductFromDto(ProductRequest productRequest, @MappingTarget Product product);

}
