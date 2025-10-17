package com.theblood.shopservice.grpc;

import com.theblood.productservice.grpc.ProductServiceGrpc;
import com.theblood.productservice.grpc.ValidateProductCreationRequest;
import com.theblood.productservice.grpc.ValidateProductCreationResponse;
import com.theblood.shopservice.common.enums.ShopStatus;
import com.theblood.shopservice.model.Shop;
import com.theblood.shopservice.repository.ShopRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductValidationService extends ProductServiceGrpc.ProductServiceImplBase {

    private final ShopRepository shopRepository;

    @Override
    public void validateProductCreation(ValidateProductCreationRequest request,
                                        StreamObserver<ValidateProductCreationResponse> responseObserver) {
        try {
            log.info("Validating product creation for shopId: {}, sku: {}", request.getShopId(), request.getSku());

            // Validate shop exists and is active
            Optional<Shop> shop = shopRepository.findById(java.util.UUID.fromString(request.getShopId()));
            if (shop.isEmpty()) {
                responseObserver.onNext(ValidateProductCreationResponse.newBuilder()
                        .setIsValid(false)
                        .setMessage("Shop not found")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            if (shop.get().getShopStatus() != ShopStatus.ACTIVE) {
                responseObserver.onNext(ValidateProductCreationResponse.newBuilder()
                        .setIsValid(false)
                        .setMessage("Shop is not active")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            // Additional validations can be added here
            // e.g., check user permissions, product limits, etc.

            responseObserver.onNext(ValidateProductCreationResponse.newBuilder()
                    .setIsValid(true)
                    .setMessage("Product creation validated successfully")
                    .build());
            responseObserver.onCompleted();

            log.info("Product creation validation successful for shopId: {}", request.getShopId());

        } catch (Exception e) {
            log.error("Error validating product creation: {}", e.getMessage(), e);
            responseObserver.onNext(ValidateProductCreationResponse.newBuilder()
                    .setIsValid(false)
                    .setMessage("Validation failed: " + e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }
}