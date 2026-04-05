package com.theblood.orderservice.grpc.client_role;


import com.theblood.springfood.common.dto.request.ShopOrderRequest;
import com.theblood.springfood.common.grpc.ShopServiceGrpc;
import com.theblood.springfood.common.grpc.ShopValidationRequest;
import com.theblood.springfood.common.grpc.ShopValidationResponse;
import com.theblood.orderservice.repository.OrderItemRepository;
import com.theblood.orderservice.repository.OrderRepository;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component

public class OrderValidation {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @GrpcClient("shop-service")
    private ShopServiceGrpc.ShopServiceBlockingStub shopServiceStub;

    public boolean shopValidation(List<ShopOrderRequest> shops) {

        try {
            for (ShopOrderRequest shop : shops) {
                ShopValidationRequest shopValidationRequest = ShopValidationRequest.newBuilder()
                        .setShopId(shop.getShopId())
                        .setShippingMethod(shop.getShippingMethod())
                        .setShippingFee(shop.getShippingFee().toString())
                        .setShopVoucher(shop.getShopVoucher())
                        .setNote(shop.getNote())
                        .build();

                ShopValidationResponse response = shopServiceStub
                        .withDeadlineAfter(5, TimeUnit.SECONDS)
                        .validateShop(shopValidationRequest);
                if (!response.getIsValid()) {
                    log.error("Shop Validation Failed : No shop own this product: ShopId = " + shop.getShopId() + "is INACTIVE or didn't exists");
                    return false;
                }
            }
            return true;
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed for shopId {}: {}", shops, e.getMessage());
            return false;
        }

    }
}
