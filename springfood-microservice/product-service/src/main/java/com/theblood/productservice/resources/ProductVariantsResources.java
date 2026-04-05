package com.theblood.productservice.resources;

import com.theblood.productservice.service.ProductVariantsService;
import com.theblood.productservice.service.dto.request.VariantsRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/variants")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class ProductVariantsResources {

    ProductVariantsService productVariantsService;

    @GetMapping("")
    public ResponseEntity<?> getProductVariants(@RequestParam String productId) {
        var res = productVariantsService.getProductVariants(productId);
        return ResponseEntity.ok().body(res);
    }

    @PostMapping("")
    public ResponseEntity<?> createProductVariants(
            @RequestBody String productId,
            @RequestBody List<VariantsRequest> productVariants) {
        var res = productVariantsService.createProductVariants(productId, productVariants);
        return ResponseEntity.ok().body(res);
    }

    @PutMapping("")
    public ResponseEntity<?> updateProductVariants(
            @RequestBody String productId,
            @RequestBody List<VariantsRequest> productVariants) {
        var res = productVariantsService.updateProductVariants(productId, productVariants);
        return ResponseEntity.ok().body(res);
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteProductVariants(
            @RequestBody String productId,
            @RequestBody List<String> variantsId) {
        var res = productVariantsService.deleteProductVariants(productId, variantsId);
        return ResponseEntity.ok().body(res);
    }
}
