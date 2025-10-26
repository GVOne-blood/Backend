package com.theblood.shopservice.grpc.server_role;

import com.theblood.common.enums.Role;
import com.theblood.productservice.grpc.ProductServiceGrpc;
import com.theblood.productservice.grpc.ValidateProductUpdateRequest;
import com.theblood.productservice.grpc.ValidateProductUpdateResponse;
import com.theblood.shopservice.repository.ShopMemberRepository;
import com.theblood.shopservice.repository.ShopRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

/**
 * Service này sử dụng gRPC client tự động qua Spring boot starter
 */
@GrpcService
@Slf4j
@RequiredArgsConstructor
public class ProductUpdateService extends ProductServiceGrpc.ProductServiceImplBase {

    ShopRepository shopRepository;
    ShopMemberRepository shopMemberRepository;

    @Override
    public void validateProductUpdate(ValidateProductUpdateRequest validateProductUpdateRequest,
                                      StreamObserver<ValidateProductUpdateResponse> responseObserver) {

        try {
            boolean res = (shopMemberRepository.existsByIdAndUserIdAndRoleName(UUID.fromString(validateProductUpdateRequest.getShopId()), UUID.fromString(validateProductUpdateRequest.getUserId()), Role.SHOP_OWNER.name()));
            String message = "INVALID data";
            if (res) message = "VALID";
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
