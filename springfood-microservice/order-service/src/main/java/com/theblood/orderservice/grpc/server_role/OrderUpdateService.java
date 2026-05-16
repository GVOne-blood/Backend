package com.theblood.orderservice.grpc.server_role;

import com.theblood.springfood.common.enums.OrderStatus;
import com.theblood.springfood.common.grpc.OrderServiceGrpc;
import com.theblood.springfood.common.grpc.OrderUpdateRequest;
import com.theblood.springfood.common.grpc.OrderUpdateResponse;
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
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderUpdateService extends OrderServiceGrpc.OrderServiceImplBase {

    OrderRepository orderRepository;
    OrderService orderService;
    OrderMapper orderMapper;
    OrderItemRepository orderItemRepository;

    @Override
    public void orderUpdate(OrderUpdateRequest orderUpdateRequest, StreamObserver<OrderUpdateResponse> responseObserver) {
        List<Order> orders = orderRepository.findByReferenceId(UUID.fromString(orderUpdateRequest.getReferenceId()));

        if (orders.isEmpty()) {
            responseObserver.onNext(OrderUpdateResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("No order found")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        OrdersUpdateRequest ordersUpdateRequest = new OrdersUpdateRequest();
        //    ordersUpdateRequest.setOrder(orderMapper.toSingleOrderRequest(orders));
        ordersUpdateRequest.setOrder(orders);
        ordersUpdateRequest.setOrderStatus(OrderStatus.valueOf(orderUpdateRequest.getOrderStatusUpdate()));
        ordersUpdateRequest.setTransactionType(TransactionType.PAYMENT);

        try {
            orderService.updatePaymentPendingOrders(ordersUpdateRequest);
            responseObserver.onNext(OrderUpdateResponse.newBuilder()
                    .setMessage("Update order successfully ")
                    .setSuccess(true)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
