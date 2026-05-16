package com.theblood.productservice.service.mapper;

import com.theblood.productservice.domain.Sale;
import com.theblood.productservice.service.dto.request.SaleRequest;
import com.theblood.productservice.service.dto.response.SaleResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SaleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productSales", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Sale toSale(SaleRequest request);

    @Mapping(target = "active", ignore = true)
    @Mapping(target = "productIds", ignore = true)
    SaleResponse toSaleResponse(Sale sale);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productSales", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateSaleFromRequest(SaleRequest request, @MappingTarget Sale sale);
}
