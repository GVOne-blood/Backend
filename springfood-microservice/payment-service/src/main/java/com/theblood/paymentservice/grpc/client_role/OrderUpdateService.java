package com.theblood.paymentservice.grpc.client_role;

import com.theblood.springfood.common.grpc.OrderServiceGrpc;
import com.theblood.springfood.common.grpc.OrderUpdateRequest;
import com.theblood.springfood.common.grpc.OrderUpdateResponse;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderUpdateService {

    @GrpcClient("order-service")
    private OrderServiceGrpc.OrderServiceBlockingStub stub;

    public boolean updateOrder(OrderUpdateRequest orderUpdateRequest) {
        try {
            OrderUpdateResponse res = stub.orderUpdate(orderUpdateRequest);
            return res.getSuccess();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }
}
