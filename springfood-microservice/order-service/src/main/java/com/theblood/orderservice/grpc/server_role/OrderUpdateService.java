package com.theblood.orderservice.grpc.server_role;

import com.theblood.common.enums.OrderStatus;
import com.theblood.common.grpc.OrderUpdateRequest;
import com.theblood.common.grpc.OrderUpdateResponse;
import com.theblood.orderservice.common.enums.TransactionType;
import com.theblood.orderservice.dto.request.OrdersUpdateRequest;
import com.theblood.orderservice.mapper.OrderMapper;
import com.theblood.orderservice.model.Order;
import com.theblood.orderservice.repository.OrderItemRepository;
import com.theblood.orderservice.repository.OrderRepository;
import com.theblood.orderservice.service.OrderService;
import io.grpc.stub.StreamObserver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderUpdateService {

    OrderRepository orderRepository;
    OrderService orderService;
    OrderMapper orderMapper;
    OrderItemRepository orderItemRepository;

    @GrpcClient("payment-service")
    public void updateOrder(OrderUpdateRequest orderUpdateRequest, StreamObserver<OrderUpdateResponse> responseObserver) {
        List<Order> orders = orderRepository.findByReferenceId(UUID.fromString(orderUpdateRequest.getReferenceId()));

        if (orders.isEmpty()) {
            responseObserver.onNext(OrderUpdateResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("No order found")
                    .build());
        }

        OrdersUpdateRequest ordersUpdateRequest = new OrdersUpdateRequest();
        //    ordersUpdateRequest.setOrder(orderMapper.toSingleOrderRequest(orders));
        ordersUpdateRequest.setOrder(orders);
        ordersUpdateRequest.setOrderStatus(OrderStatus.valueOf(orderUpdateRequest.getOrderStatusUpdate()));
        ordersUpdateRequest.setTransactionType(TransactionType.PAYMENT);

        try {
            responseObserver.onNext(OrderUpdateResponse.newBuilder().setMessage("Update order successfully ").setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }

        orderService.updatePaymentPendingOrders(ordersUpdateRequest);
        // return ordersUpdateRequest;
    }
}
