package com.theblood.shopservice.grpc.server_role;

import com.theblood.common.enums.Role;
import com.theblood.common.grpc.*;
import com.theblood.shopservice.common.enums.ShopStatus;
import com.theblood.shopservice.domain.Shop;
import com.theblood.shopservice.repository.ShopMemberRepository;
import com.theblood.shopservice.repository.ShopRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;

/**
 * Combined ProductService implementation handling both creation and update validation
 */
@GrpcService
@Slf4j
@RequiredArgsConstructor
public class ProductValidationService extends ProductServiceGrpc.ProductServiceImplBase {

    private final ShopRepository shopRepository;
    private final ShopMemberRepository shopMemberRepository;

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

    @Override
    public void validateProductUpdate(ValidateProductUpdateRequest validateProductUpdateRequest,
                                      StreamObserver<ValidateProductUpdateResponse> responseObserver) {
        try {
            boolean res = shopMemberRepository.existsByIdAndUserIdAndRoleName(
                (validateProductUpdateRequest.getShopId()),
                (validateProductUpdateRequest.getUserId()),
                Role.SHOP_OWNER.name()
            );

            String message = res ? "VALID" : "INVALID data";

            ValidateProductUpdateResponse response = ValidateProductUpdateResponse.newBuilder()
                .setIsValid(res)
                .setMessage(message)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("❌ Error validating product update", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }
}
