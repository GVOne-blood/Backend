package com.theblood.shopservice.grpc.server_role;

import com.theblood.common.exception.custom.InvalidDataException;
import com.theblood.shopservice.common.enums.ShopStatus;
import com.theblood.shopservice.grpc.ShopServiceGrpc;
import com.theblood.shopservice.grpc.ShopValidationRequest;
import com.theblood.shopservice.grpc.ShopValidationResponse;
import com.theblood.shopservice.model.Shop;
import com.theblood.shopservice.repository.ShopMemberRepository;
import com.theblood.shopservice.repository.ShopRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;
import java.util.UUID;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class OrderValidationService extends ShopServiceGrpc.ShopServiceImplBase {

    private final ShopRepository shopRepository;
    private final ShopMemberRepository shopMemberRepository;

    @Override
    public void validateShop(ShopValidationRequest request, StreamObserver<ShopValidationResponse> response) {

        boolean isValid = false;
        String message = "";
        try {
            Optional<Shop> shop = shopRepository.findById(UUID.fromString(request.getShopId()));
            if (request.getShopId().isEmpty() || shop.isEmpty() || shop.get().getShopStatus() == ShopStatus.CLOSED)
                throw new InvalidDataException("this shop is not exists or had close");

            if (request.getShippingFee().isEmpty()) {

                message = "this shippingFee is empty";
                throw new InvalidDataException("this shippingFee is empty");
            }
            ShopValidationResponse ans = ShopValidationResponse.newBuilder()
                    .setIsValid(isValid)
                    .setMessage(message)
                    .build();
            response.onNext(ans);
            response.onCompleted();
        } catch (Exception e) {
            log.error("❌ Error validating product update : ", e);
            response.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }
}
