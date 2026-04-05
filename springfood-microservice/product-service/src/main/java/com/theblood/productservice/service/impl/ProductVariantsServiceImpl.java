package com.theblood.productservice.service.impl;

import com.theblood.productservice.domain.ProductVariants;
import com.theblood.productservice.repository.ProductRepository;
import com.theblood.productservice.repository.ProductVariantsRepository;
import com.theblood.productservice.service.ProductVariantsService;
import com.theblood.productservice.service.dto.request.VariantsRequest;
import com.theblood.productservice.service.dto.response.VariantsDeleteResponse;
import com.theblood.productservice.service.dto.response.VariantsResponse;
import com.theblood.springfood.client.service.LoggingService;
import com.theblood.springfood.common.enums.ActionType;
import com.theblood.springfood.common.exception.custom.NotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductVariantsServiceImpl implements ProductVariantsService {

    ProductRepository productRepository;
    ProductVariantsRepository productVariantsRepository;
    LoggingService loggingService;

    @Override
    public List<VariantsResponse> getProductVariants(String productId) {

        if (!productRepository.existsById(UUID.fromString(productId))) {
            log.error("Product with id {} not found", productId);
            throw new NotFoundException("Product not found");
        }
        List<VariantsResponse> variantsResponses = productVariantsRepository.findAllByProductId(productId);
        if (variantsResponses.isEmpty()) {
            log.error("Product with id {} has no variants", productId);
            throw new NotFoundException("Product has no variants");
        }


        return variantsResponses;
    }

    @Override
    public List<VariantsResponse> createProductVariants(String productId, List<VariantsRequest> variantsRequest) {
        validateProduct(productId);
        List<ProductVariants> productVariantsList = new ArrayList<>();
        variantsRequest.forEach(variant -> {
            ProductVariants productVariants = new ProductVariants();
            productVariants.setProductId(productId);
            productVariants.setVariantName(variant.getVariantName());
            productVariants.setPrice(variant.getPrice());
            productVariants.setStock(variant.getStock());
            productVariants.setIsAvailable(variant.getStock() != 0);
            productVariants.setImageUrl(productVariants.getImageUrl());
            productVariants.setSku(productVariants.getSku());
            productVariants.setAttributes(productVariants.getAttributes());

            productVariantsList.add(productVariants);

        });
        productVariantsRepository.saveAll(productVariantsList);
        return null;
    }

    @Override
    public List<VariantsResponse> updateProductVariants(String productId, List<VariantsRequest> variantsRequest) {
        return null;
    }

    @Override
    public VariantsDeleteResponse deleteProductVariants(String productId, List<String> variantsId) {
        validateProduct(productId);
        List<ProductVariants> productVariantsList = productVariantsRepository.findAllByProductIdAndVariantsId(productId, variantsId);
        if (productVariantsList.isEmpty()) {
            throw new NotFoundException("Product with id " + productId + " has no variants");
        }

        productVariantsRepository.deleteAll(productVariantsList);


        //create log
        loggingService.createLogAction(
                ActionType.DELETE.name(),                                           // actionType
                String.join(", ", variantsId),                     // oldValue
                null,                                              // newValue
                "Deleted " + productVariantsList.size() + " product variants",  // description
                "product_variants",                                // tableName
                productId,                                         // objectId
                null,                                              // accountId (sẽ tự lấy từ context)
                null,                                              // userName (sẽ tự lấy từ context)
                null,                                              // shopId (sẽ tự lấy từ context)
                null,                                              // ipAddress (sẽ tự lấy từ context)
                null                                               // userAgent (sẽ tự lấy từ context)
        );

        return VariantsDeleteResponse.builder()
                .deleteCount(variantsId.size())
                .success(productVariantsList.size())
                .message("Deleted " + productVariantsList.size() + " variants successfully")
                .build();

    }

    private void validateProduct(String productId) {

        productRepository.findById(UUID.fromString(productId)).orElseThrow(() -> {
            throw new NotFoundException("Product with id " + productId + " not found");
        });
    }

}
