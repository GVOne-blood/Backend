package com.theblood.orderservice.service.Impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.common.dto.kafka.Event;
import com.theblood.springfood.common.dto.kafka.OrderCreationEvent;
import com.theblood.springfood.common.dto.request.ItemRequest;
import com.theblood.springfood.common.dto.request.ShopOrderRequest;
import com.theblood.springfood.common.dto.response.ProductDetail;
import com.theblood.springfood.common.enums.MessageStatus;
import com.theblood.springfood.common.enums.OrderStatus;
import com.theblood.springfood.common.enums.PaymentMethod;
import com.theblood.springfood.common.enums.kafka.SagaOrderEventType;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import com.theblood.orderservice.common.util.OrderStatusValidationUtil;
import com.theblood.orderservice.dto.request.OrderRequest;
import com.theblood.orderservice.dto.request.OrdersUpdateRequest;
import com.theblood.orderservice.dto.request.SingleOrderRequest;
import com.theblood.orderservice.dto.response.OrderDetailResponse;
import com.theblood.orderservice.dto.response.OrderItemView;
import com.theblood.orderservice.dto.response.OrderPaymentResponse;
import com.theblood.orderservice.grpc.client_role.OrderTranfer;
import com.theblood.orderservice.grpc.client_role.OrderValidation;
import com.theblood.orderservice.kafka.consumer.OrderServiceConsumer;
import com.theblood.orderservice.kafka.event.OrderAddressEvent;
import com.theblood.orderservice.kafka.model.OutboxMessage;
import com.theblood.orderservice.mapper.OrderMapper;
import com.theblood.orderservice.model.Order;
import com.theblood.orderservice.model.OrderItem;
import com.theblood.orderservice.repository.OrderItemRepository;
import com.theblood.orderservice.repository.OrderRepository;
import com.theblood.orderservice.repository.OutboxMessageRepository;
import com.theblood.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.TransactionRolledbackException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    OrderMapper orderMapper;
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OutboxMessageRepository outboxMessageRepository;
    ObjectMapper objectMapper;
    OrderTranfer orderTranfer;
    KafkaTemplate<String, Object> kafkaTemplate;
    OrderServiceConsumer orderServiceConsumer;
    OrderValidation orderValidation;
    com.theblood.orderservice.service.RealtimeNotifierClient realtimeNotifierClient;
//    ProductService productService;
//    ProductRepository productRepository;
//    AddressRepository addressRepository;
//    PaymentRepository paymentRepository;
//    PaymentService paymentService;
//    ShopService shopService;
//    VNPayService vnPayService;
//    ProductMapper productMapper;
//    UserRepository userRepository;

    @Override
    public Page<OrderDetailResponse> getListOrderForAdmin(Pageable pageable, String userId) {
        //return orderRepository.getAllOrder(pageable);
        return null;
    }

    @Override
    public Page<OrderDetailResponse> getListOrderForShop(Pageable pageable, UUID shopId) {
        Page<Order> orders = orderRepository.findByShopId(shopId, pageable);
        return orders.map(this::toOrderDetail);
    }

    @Override
    public Page<OrderDetailResponse> getListOrderForUser(Pageable pageable, UUID userId) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(this::toOrderDetail);
    }

    @Override
    public OrderDetailResponse getOrderDetailForUser(UUID orderId, UUID userId) {
        Order order = orderRepository.findByUserIdAndOrderId(userId, orderId)
                .orElseThrow(() -> new InvalidDataException("Order not found"));
        return toOrderDetail(order);
    }

    @Override
    public OrderDetailResponse getOrderDetailByOrderId(UUID orderId, UUID shopId) {
        Order order = orderRepository.findByShopIdAndOrderId(shopId, orderId)
                .orElseThrow(() -> new InvalidDataException("Order not found"));
        return toOrderDetail(order);
    }

    @Transactional
    @Override
    public OrderDetailResponse approveOrder(UUID orderId, UUID shopId) {
        Order order = orderRepository.findByShopIdAndOrderId(shopId, orderId)
                .orElseThrow(() -> new InvalidDataException("Order not found"));

        OrderStatus current = order.getOrderStatus();
        // Chỉ cho phép approve các trạng thái "đang chờ shop xử lý"
        if (current != OrderStatus.PENDING
                && current != OrderStatus.PENDING_PAYMENT
                && current != OrderStatus.PROCESSING) {
            throw new InvalidDataException(
                    "Only orders in PENDING/PROCESSING can be approved. Current status: " + current);
        }

        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        // Realtime push tới user đặt hàng: "Đơn của bạn đã được duyệt"
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("orderId", order.getId().toString());
            payload.put("shopId", order.getShopId() != null ? order.getShopId().toString() : null);
            payload.put("status", OrderStatus.CONFIRMED.name());
            payload.put("amount", order.getFinalPrice());
            if (order.getUserId() != null) {
                realtimeNotifierClient.notifyUser(
                        order.getUserId().toString(), "ORDER_APPROVED", payload);
            }
            // Đồng thời broadcast về topic shop để các tab khác của owner refresh
            if (order.getShopId() != null) {
                realtimeNotifierClient.notifyShop(
                        order.getShopId().toString(), "ORDER_STATUS_CHANGED", payload);
            }
        } catch (Exception ex) {
            log.warn("Realtime push ORDER_APPROVED failed: {}", ex.getMessage());
        }

        return toOrderDetail(order);
    }

    private OrderDetailResponse toOrderDetail(Order order) {
        OrderDetailResponse dto = orderMapper.toOrderDetail(order);
        dto.setOrderId(order.getId());
        dto.setOrderDate(order.getCreatedAt());
        dto.setPaymentMethod(order.getPaymentMethod().name());
        dto.setUserId(order.getUserId());
        dto.setShopId(order.getShopId());
        dto.setSubtotalAmount(order.getSubtotalAmount());
        dto.setDiscount(order.getDiscount());
        dto.setFinalPrice(order.getFinalPrice());
        dto.setShippingFee(order.getShippingFee());
        dto.setOrderStatus(order.getOrderStatus());
        // Materialise line items from order_items so the FE sees the actual
        // ordered quantity (not product stock) per line.
        dto.setItems(loadOrderItems(order.getId()));
        return dto;
    }

    /**
     * Loads {@code order_items} rows for the given order and converts them to
     * {@link OrderItemView}. {@code image} is left null here — the FE either
     * already has the product image cached or can call
     * {@code GET /products/{id}} when it needs to render one. Avoiding the
     * fan-out keeps order list endpoints cheap.
     */
    private List<OrderItemView> loadOrderItems(UUID orderId) {
        if (orderId == null) return List.of();
        return orderItemRepository.findByOrderId(orderId).stream()
                .map(item -> OrderItemView.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .priceAtBooking(item.getPriceAtBooking())
                        .image(null)
                        .build())
                .toList();
    }

    /**
     * Nghiệp vụ :
     * Một shop ~ 1 order
     * 1. Lấy ds shop
     * 2. Check exists
     * 3. Lấy ds product
     * 4. Check quantity, status, Add to OrderItem
     *
     * @param orderRequest
     * @param userId
     * @return
     */
    @Transactional
    @Override
    public OrderPaymentResponse createOrders(HttpServletRequest request, OrderRequest orderRequest, String userId) throws UnsupportedEncodingException, TransactionRolledbackException, JsonProcessingException {

        List<OrderDetailResponse> orderDetailResponseList = new ArrayList<>();
        List<ShopOrderRequest> shops = orderRequest.getShopOrderItems();
        List<Order> allOrders = new ArrayList<>();
        //   BigDecimal finalPrice = new BigDecimal(0);

        // fee theo khoảng cách bla...
        BigDecimal distanceFee = new BigDecimal(0);

        List<ItemRequest> items;
        StringBuilder orderPaymentInfo = new StringBuilder();

        //check shop
        if (!orderValidation.shopValidation(shops)) throw new InvalidDataException("Shop Validation Failed");

        //check product in shop

        // add address
        OrderAddressEvent addressEvent = OrderAddressEvent.builder()
                .userId(UUID.fromString(userId))
                .addressId(orderRequest.getShippingAddressId())
                .build();

        kafkaTemplate.send("order-address-request", addressEvent);
//        Optional<Address> addr = addressRepository.findByUserId(orderRequest
//                .getShippingAddressId(), userId);
//
//        if (addr.isEmpty()) throw new InvalidDataException("Invalid Shipping Address");


        for (ShopOrderRequest shop : shops) {
            items = shop.getItems();
            if (items == null || items.isEmpty()) {
                throw new InvalidDataException("Order must contain at least one valid item");
            }
            List<Order> orders = new ArrayList<>();
            OrderDetailResponse orderDetailResponse = new OrderDetailResponse();
            Order order = new Order();
            List<ProductDetail> productList = new ArrayList<>();
            BigDecimal subtotalPrice = BigDecimal.ZERO;
            order.setUserId(UUID.fromString(userId));
            order.setOrderStatus(OrderStatus.PROCESSING);
            order.setCustomerNotes(shop.getNote());
            if (orderRequest.getPaymentMethod() == PaymentMethod.COD)
                order.setPaymentMethod(PaymentMethod.COD);
            else if (orderRequest.getPaymentMethod() == PaymentMethod.VNPAY)
                order.setPaymentMethod(PaymentMethod.VNPAY);
            else throw new InvalidDataException("Unsupported PaymentMethod ");

            //address
            // order.setAddress(addr.get());
            //shipping fee
            orderRepository.save(order);

            //start choreography saga
            String transactionId = UUID.randomUUID().toString();
            OrderCreationEvent payload = OrderCreationEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId()).products(items)
                    .build();

            OutboxMessage outboxMessage = new OutboxMessage();
            outboxMessage.setTopic("order-creation-saga");
            outboxMessage.setStatus(MessageStatus.PENDING);
            outboxMessage.setMessageId(transactionId); // Đảm bảo các event cùng 1 luồng distributed transaction cùng 1 partition để được xử lý đồng bộ
            outboxMessage.setPayload(objectMapper.writeValueAsString(Event.builder()
                    .eventType(SagaOrderEventType.ORDER_CREATED.name())
                    .transactionId(transactionId)
                    .payload(payload)
                    .build()));
            outboxMessageRepository.save(outboxMessage);

            // caculate price after saga transaction complete
            order.setShippingFee(BigDecimal.ZERO);
            order.setSubtotalAmount(subtotalPrice);
            //discount
            order.setDiscount(BigDecimal.ZERO);
            BigDecimal finalPrice = (subtotalPrice.add(order.getShippingFee().add(distanceFee)).subtract(order.getDiscount()));
            //total price
            order.setFinalPrice(finalPrice);

            //payment with gRPC
//            kafkaTemplate.send("order-creation-saga", Event.builder()
//                    .eventType(SagaOrderEventType.ORDER_CREATED.name())
//                    .transactionId(transactionId)
//                    .payload(payload)
//                    .build()
//            );

            // Set shop for single order
            order.setShopId(UUID.fromString(shop.getShopId()));

            // orderRepository.save(order);
            orders.add(order);
            allOrders.add(order);

//            for (OrderItem item : orderItems) {
//                item.setOrder(order);
//                //  orderItemRepository.save(item);
//            }

            orderDetailResponse = orderMapper.toOrderDetail(order);
            orderDetailResponse.setOrderId(order.getId());
            orderDetailResponse.setOrderDate(order.getCreatedAt());
            orderDetailResponse.setShippingFee(distanceFee);
            orderDetailResponse.setPaymentMethod(order.getPaymentMethod().name());
            orderDetailResponse.setUserId(order.getUserId());
            //   orderDetailResponse.setItems(productMapper.toProductDetail(order.getBookingItems()));
            orderDetailResponse.setItems(productList);
            orderDetailResponse.setShopId(order.getShopId());
//            orderDetailResponse.
//                    setShippingAddress((addr.get().getAddressDetail() + " " + addr.get().getStreet()).trim() + ", " + addr.get().getWard() + ", " + addr.get().getCity());
            orderDetailResponseList.add(orderDetailResponse);


            for (ProductDetail p : productList) orderPaymentInfo.append(p.getName()).append(" ");
            orderRepository.saveAll(orders);
        }
        // create payment transaction

        Long amount = 0l;
        BigDecimal totalShippingFee = new BigDecimal(0);
        for (OrderDetailResponse x : orderDetailResponseList) {
            amount += x.getFinalPrice().longValue();
            totalShippingFee = totalShippingFee.add(x.getShippingFee());
            //- orderRequest.getGlobalVoucher()
        }


        OrderPaymentResponse orderPaymentResponse = new OrderPaymentResponse();
        orderPaymentResponse.setOrderDetails(orderDetailResponseList);
        orderPaymentResponse.setAmount(amount);
        orderPaymentResponse.setTotalShippingFee(totalShippingFee);
// tạo paymment transaction id để chuẩn bị thanh toan
        String referenceId;
        OrderStatus paymentStatus = orderRequest.getPaymentMethod() == PaymentMethod.COD
                ? OrderStatus.PENDING
                : OrderStatus.PENDING_PAYMENT;
        referenceId = orderTranfer.creationPaymentTransactionRequest(
                UUID.fromString(userId),
                amount,
                paymentStatus,
                orderDetailResponseList.get(0).getPaymentMethod()
        );
        if (referenceId == null || referenceId.isBlank()) throw new InvalidDataException("reference Id is Blank");

// lưu dấu liên kết với payment transaction vào tất cả order của các shop
        for (Order order : allOrders) {
            order.setReferenceId(UUID.fromString(referenceId));
        }
        orderPaymentResponse.setReferenceId(referenceId);

        // Realtime push notify tới shop owner: "Có đơn mới, cần phê duyệt"
        for (Order o : allOrders) {
            try {
                java.util.Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("orderId", o.getId() != null ? o.getId().toString() : null);
                payload.put("shopId", o.getShopId() != null ? o.getShopId().toString() : null);
                payload.put("userId", o.getUserId() != null ? o.getUserId().toString() : null);
                payload.put("status", o.getOrderStatus() != null ? o.getOrderStatus().name() : null);
                payload.put("amount", o.getFinalPrice());
                payload.put("paymentMethod", o.getPaymentMethod() != null ? o.getPaymentMethod().name() : null);
                payload.put("createdAt", o.getCreatedAt() != null ? o.getCreatedAt().toString() : null);
                if (o.getShopId() != null) {
                    realtimeNotifierClient.notifyShop(o.getShopId().toString(), "ORDER_CREATED", payload);
                }
            } catch (Exception ex) {
                log.warn("Push realtime ORDER_CREATED failed: {}", ex.getMessage());
            }
        }

        return orderPaymentResponse;
    }

    /**
     * method chưa sẵn sang
     * REQUIRE : Tất cả các order phải thuộc cùng 1 giỏ hàng(cùng 1 transactionId), các order phải được thanh toán cùng lúc (thanh toán sản phẩm trong giỏ hàng)
     * NOTE : Hàm dùng để update order, nhưng khi update order lên trạng thái mới sẽ buộc phải kèm theo các event liên quan đến nghiệp vụ
     * nên dữ liệu trả về của hàm sẽ là một order cha (OrderPaymentResponse) đã được cập nhật
     *
     * @param request
     * @param updateRequest
     * @param
     * @return
     * @throws UnsupportedEncodingException
     */
    @Transactional
    @Override
    public OrderPaymentResponse updatePendingOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest) throws UnsupportedEncodingException {

        OrderPaymentResponse response = new OrderPaymentResponse();

        List<SingleOrderRequest> modifyFields = (List<SingleOrderRequest>) updateRequest.getOrder();
        List<String>
                ids = new ArrayList<>();
        for (SingleOrderRequest x : modifyFields) {
//            if (addressRepository.findById(x.getAddressId()).isEmpty()) {
//                throw new InvalidDataException("Address of order " + x.getOrderId() + "not found");
//            }
            ids.add(x.getOrderId());
        }


        List<Order> orders = orderRepository.findAllById(ids);
        if (ids.size() != orders.size() || orders.isEmpty() || ids.isEmpty()) {
            throw new InvalidDataException("Orders must contain at least one valid order");
        }

        OrderStatus requestOrderStatus = updateRequest.getOrderStatus();
        if (!OrderStatusValidationUtil.isValidStatusTransition(requestOrderStatus))
            throw new InvalidDataException("Invalid Order Status: This Status unreachable");


        // đổi phương thức thanh toán từ COD -> online transfer
        if (requestOrderStatus.equals(OrderStatus.PENDING_PAYMENT)) {

//            if (updateRequest.getPaymentMethod().name().equals(PaymentMethod.VNPAY.name())) {
//                response.setPaymentUrl(getVNPayReturnUrl(request, orders));
//            }
            for (int i = 0; i < orders.size(); i++) {
                if (!orders.get(i).getId().equals(modifyFields.get(i).getOrderId())) {

                    throw new InvalidDataException("Invalid Order ID request");
                }
                orders.get(i).setOrderStatus(OrderStatus.PENDING_PAYMENT);
            }

        } else if (requestOrderStatus.equals(OrderStatus.CONFIRMED)) {
            for (Order order : orders) {

                // sàn gửi thông báo đến shop để nó chuẩn bị, nó nhấn confirm thì đơn hàng sẽ là confirm
                order.setOrderStatus(OrderStatus.CONFIRMED);
            }
        }
        //map customer note & addressId
        orders = orderMapper.toOrder(modifyFields);
        return response;
    }

    @Override
    @Transactional
    public OrderPaymentResponse updatePaymentPendingOrders(OrdersUpdateRequest updateRequest) {
        List<Order> orders = (List<Order>) updateRequest.getOrder();
        orders.forEach(order -> {
            order.setOrderStatus(OrderStatus.CONFIRMED);
        });
        return null;
    }

    @Override
    public OrderPaymentResponse updateConfirmedOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId) {
        return null;
    }

    @Override
    public OrderPaymentResponse updateProcessingOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId) {
        return null;
    }

    @Override
    public OrderPaymentResponse updateReadyPickupOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId) {
        return null;
    }

    @Override
    public OrderPaymentResponse updateShippingOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId) {
        return null;
    }

    @Override
    public OrderPaymentResponse updateCompletedOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId) {
        return null;
    }

    @Override
    public OrderPaymentResponse updateOrderReturnOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId) {
        return null;
    }


    @Override
    public void deleteOrder(String orderId) {

        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty() || order.get().getOrderStatus().equals(OrderStatus.DELETED))
            throw new InvalidDataException("Order with id " + orderId + " not found");

        order.get().setOrderStatus(OrderStatus.DELETED);
    }

    @Transactional
    @Override
    public OrderDetailResponse updateShopOrderStatus(UUID orderId, UUID shopId, OrderStatus targetStatus, String reason) {
        if (orderId == null || shopId == null || targetStatus == null) {
            throw new InvalidDataException("orderId, shopId and targetStatus are required");
        }

        Order order = orderRepository.findByShopIdAndOrderId(shopId, orderId)
            .orElseThrow(() -> new InvalidDataException("Order not found"));

        OrderStatus current = order.getOrderStatus();
        if (current == targetStatus) {
            // Idempotent: nothing to do, just return current snapshot.
            return toOrderDetail(order);
        }

        // Validate transition against the centralised state machine
        java.util.List<OrderStatus> allowed =
            com.theblood.orderservice.common.util.OrderStatusValidationUtil.getValidStatusTransition(current);
        if (!allowed.contains(targetStatus)) {
            throw new InvalidDataException(
                "Cannot transition order from " + current + " to " + targetStatus);
        }

        order.setOrderStatus(targetStatus);
        if (targetStatus == OrderStatus.COMPLETED) {
            order.setDeliveredAt(java.time.LocalDateTime.now());
        }
        orderRepository.save(order);

        // Realtime push so the buyer sees status changes without refresh.
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("orderId", order.getId().toString());
            payload.put("shopId", order.getShopId() != null ? order.getShopId().toString() : null);
            payload.put("status", targetStatus.name());
            if (reason != null && !reason.isBlank()) payload.put("reason", reason);
            if (order.getUserId() != null) {
                realtimeNotifierClient.notifyUser(
                    order.getUserId().toString(), "ORDER_STATUS_CHANGED", payload);
            }
            if (order.getShopId() != null) {
                realtimeNotifierClient.notifyShop(
                    order.getShopId().toString(), "ORDER_STATUS_CHANGED", payload);
            }
        } catch (Exception ex) {
            log.warn("Realtime push ORDER_STATUS_CHANGED failed: {}", ex.getMessage());
        }

        return toOrderDetail(order);
    }
//
//    private String getVNPayReturnUrl(HttpServletRequest request, List<Order> orders) throws UnsupportedEncodingException {
//        VNPayPaymentRequest vnPayPaymentRequest = new VNPayPaymentRequest();
//        vnPayPaymentRequest.setOrderInfo("Thanh toan cho don hang " + orders.get(0).getPaymentTransactions().getId());
//        vnPayPaymentRequest.setPaymentMethod(PaymentMethod.VNPAY);
//        vnPayPaymentRequest.setAmount(getTotalAmount(orders));
//        vnPayPaymentRequest.setTxnRef(orders.get(0).getPaymentTransactions().getId());
//        return vnPayService.createPaymentUrl(request, vnPayPaymentRequest);
//    }

    private Long getTotalAmount(List<Order> orders) {

        Long amount = 0l;
        for (Order orderItem : orders) {
            amount += orderItem.getFinalPrice().longValue();
        }
        return amount;
    }
}
