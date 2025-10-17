package com.theblood.productservice.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductGrpcClient {

    private final ManagedChannel channel;
    private final ProductServiceGrpc.ProductServiceBlockingStub stub;

    public ProductGrpcClient() {
        // Connect to shop-service gRPC server
        this.channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext() // For development only
                .build();
        this.stub = ProductServiceGrpc.newBlockingStub(channel);
    }

    public ValidateProductCreationResponse validateProduct(String sku, String shopId, String username, String categoryNames) {
        ValidateProductCreationRequest request = ValidateProductCreationRequest.newBuilder()
                .setSku(sku)
                .setShopId(shopId)
                .setUsername(username)
                .setCategoryNames(categoryNames)
                .build();

        return stub.validateProductCreation(request);
    }
}