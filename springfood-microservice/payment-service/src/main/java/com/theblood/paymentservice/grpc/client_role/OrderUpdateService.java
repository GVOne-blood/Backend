package com.theblood.paymentservice.grpc.client_role;

import com.theblood.common.grpc.OrderServiceGrpc;
import com.theblood.common.grpc.OrderUpdateRequest;
import com.theblood.common.grpc.OrderUpdateResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderUpdateService {

    OrderServiceGrpc.OrderServiceBlockingStub stub;

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
