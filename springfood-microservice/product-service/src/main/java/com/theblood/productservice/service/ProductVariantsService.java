package com.theblood.productservice.service;

import com.theblood.productservice.service.dto.request.VariantsRequest;
import com.theblood.productservice.service.dto.response.VariantsDeleteResponse;
import com.theblood.productservice.service.dto.response.VariantsResponse;

import java.util.List;

public interface ProductVariantsService {

    List<VariantsResponse> getProductVariants(String productId);

    List<VariantsResponse> createProductVariants(String productId, List<VariantsRequest> variantsRequest);

    List<VariantsResponse> updateProductVariants(String productId, List<VariantsRequest> variantsRequest);

    VariantsDeleteResponse deleteProductVariants(String productId, List<String> variantsId);
}
