package com.theblood.orderservice.service.Impl;


import com.theblood.common.enums.PaymentMethod;
import com.theblood.common.enums.ProductStatus;
import com.theblood.common.exception.custom.InvalidDataException;
import com.theblood.orderservice.common.enums.OrderStatus;
import com.theblood.orderservice.common.util.OrderStatusValidationUtil;
import com.theblood.orderservice.dto.request.ItemRequest;
import com.theblood.orderservice.dto.request.OrderRequest;
import com.theblood.orderservice.dto.request.OrdersUpdateRequest;
import com.theblood.orderservice.dto.request.SingleOrderRequest;
import com.theblood.orderservice.dto.response.OrderDetailResponse;
import com.theblood.orderservice.dto.response.OrderPaymentResponse;
import com.theblood.orderservice.mapper.OrderMapper;
import com.theblood.orderservice.model.Order;
import com.theblood.orderservice.model.OrderItem;
import com.theblood.orderservice.repository.OrderItemRepository;
import com.theblood.orderservice.repository.OrderRepository;
import com.theblood.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    ProductService productService;
    ProductRepository productRepository;
    OrderRepository orderRepository;
    AddressRepository addressRepository;
    PaymentRepository paymentRepository;
    PaymentService paymentService;
    ShopService shopService;
    OrderItemRepository orderItemRepository;
    VNPayService vnPayService;

    ProductMapper productMapper;
    OrderMapper orderMapper;
    UserRepository userRepository;

    @Override
    public Page<OrderDetailResponse> getListOrderForAdmin(Pageable pageable, String userId) {
        //return orderRepository.getAllOrder(pageable);
        return null;
    }

    @Override
    public Page<OrderDetailResponse> getListOrderForShop(Pageable pageable, String ShopOwnerId) {
        return null;
    }

    @Override
    public Page<OrderDetailResponse> getListOrderForUser(Pageable pageable, String userId) {
        return null;
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
    public OrderPaymentResponse createOrders(HttpServletRequest request, OrderRequest orderRequest, String userId) throws UnsupportedEncodingException {

        PaymentTransactions paymentTransaction = new PaymentTransactions();
        List<OrderDetailResponse> orderDetailResponseList = new ArrayList<>();
        OrderDetailResponse orderDetailResponse = new OrderDetailResponse();
        List<ShopOrderRequest> shops = orderRequest.getShopOrderItems();
        List<Order> allOrders = new ArrayList<>();
        //   BigDecimal finalPrice = new BigDecimal(0);

        // fee theo khoảng cách bla...
        BigDecimal distanceFee = new BigDecimal(0);

        List<ItemRequest> items;
        StringBuilder orderPaymentInfo = new StringBuilder();
        StringBuilder notes = new StringBuilder();

        Optional<Address> addr = addressRepository.findByUserId(orderRequest
                .getShippingAddressId(), userId);

        if (addr.isEmpty()) throw new InvalidDataException("Invalid Shipping Address");

        for (ShopOrderRequest shop : shops) {
            if (!shopService.isShopExists(shop.getShopId())) throw new InvalidDataException("Shop does not exist");
            items = shop.getItems();
            List<Order> orders = new ArrayList<>();
            Order order = new Order();
            List<ProductDetail> productList = new ArrayList<>();
            BigDecimal subtotalPrice = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();
            User user = userRepository.findById(userId).get();
            order.setUser(user);

            // Set shop for order
            Shop shopEntity = shopService.getShopById(shop.getShopId());
            order.setShop(shopEntity);

            for (ItemRequest item : items) {
                if (productService.isProductExists(item.getProductId())) {
                    Product product =
                            productRepository.
                                    findById(item.getProductId()).orElseThrow(() -> new InvalidDataException("product not found"));
                    //check quantity
                    if (product.getQuantity() < item.getQuantity() || product.getQuantity() <= 0)
                        throw new InvalidDataException("quantity must be less than stock");
                    //check available
                    if (product.getProductStatus() != ProductStatus.AVAILABLE)
                        throw new InvalidDataException("product status no longer AVAILABLE");

                    OrderItem orderItem = new OrderItem();
                    product.setQuantity(product.getQuantity() - item.getQuantity());
                    orderItem.setProduct(product);
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setOrder(order);
                    productList.add(productMapper.toProductDetail(product, item.getQuantity()));
                    //discount amount default
                    orderItem.setDiscountAmount(BigDecimal.ZERO);

                    BigDecimal itemPrice = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    orderItem.setPriceAtBooking(itemPrice);

                    orderItems.add(orderItem);

                    subtotalPrice = subtotalPrice.add(itemPrice);
                }
            }


            // Đảm bảo có ít nhất 1 sản phẩm trong order
            if (orderItems.isEmpty()) {
                throw new InvalidDataException("Order must contain at least one valid item");
            }

            // Set các orderItems vào order
            order.setBookingItems(orderItems);

            //note
            order.setCustomerNotes(shop.getNote());
            // payment method
            Payment payment = paymentRepository.findById(orderRequest.getPaymentMethod().name()).orElseThrow(() -> new InvalidDataException("Invalid Payment method"));

            if (!payment.getIsActive()) throw new InvalidDataException("Payment method is not active");
            order.setPaymentMethod(payment);
            //order status và payment status
            if (!payment.getId().equals(PaymentMethod.COD.name())) {
                order.setOrderStatus(OrderStatus.PENDING_PAYMENT);
            } else {
                order.setOrderStatus(OrderStatus.PENDING);
            }


            //address
            order.setAddress(addr.get());
            //shipping fee
            order.setShippingFee(BigDecimal.ZERO);

            order.setSubtotalAmount(subtotalPrice);

            //discount
            order.setDiscount(BigDecimal.ZERO);

            BigDecimal finalPrice = (subtotalPrice.add(order.getShippingFee().add(distanceFee)).subtract(order.getDiscount()));
            //total price
            order.setFinalPrice(finalPrice);

            // orderRepository.save(order);
            orders.add(order);
            allOrders.add(order);


            for (OrderItem item : orderItems) {
                item.setOrder(order);
                //  orderItemRepository.save(item);
            }

            orderDetailResponse = orderMapper.toOrderDetail(order);
            orderDetailResponse.setOrderId(order.getId());
            orderDetailResponse.setOrderDate(order.getCreatedAt());
            orderDetailResponse.setShippingFee(distanceFee);
            orderDetailResponse.setPaymentMethod(order.getPaymentMethod().getId());
            orderDetailResponse.setUserId(order.getUser().getId());
            //   orderDetailResponse.setItems(productMapper.toProductDetail(order.getBookingItems()));
            orderDetailResponse.setItems(productList);
            orderDetailResponse.setShopId(order.getShop().getId());
            orderDetailResponse.
                    setShippingAddress((addr.get().getAddressDetail() + " " + addr.get().getStreet()).trim() + ", " + addr.get().getWard() + ", " + addr.get().getCity());
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

        PaymentInfoRequest paymentInfo = new PaymentInfoRequest();
        paymentInfo.setAmount(amount);
        paymentInfo.setUserId(userId);
        paymentInfo.setPaymentMethod(PaymentMethod.VNPAY);
        paymentInfo.setStatus(TransactionStatus.PENDING);
        paymentTransaction = paymentService.createPaymentTransaction(paymentInfo, allOrders);

        OrderPaymentResponse orderPaymentResponse = new OrderPaymentResponse();
        orderPaymentResponse.setOrderDetails(orderDetailResponseList);
        orderPaymentResponse.setAmount(amount);
        orderPaymentResponse.setTotalShippingFee(totalShippingFee);
        orderPaymentResponse.setTransactionId(paymentTransaction.getTransactionNo());

        if (orderDetailResponse.getPaymentMethod().equals(PaymentMethod.VNPAY.name())) {
            VNPayPaymentRequest vnPayPaymentRequest = new VNPayPaymentRequest();
            vnPayPaymentRequest.setOrderInfo(orderPaymentInfo.toString());
            vnPayPaymentRequest.setUserId(userId);
            // maybe cái này không quan trọng vì nó là trans tổng thể
            vnPayPaymentRequest.setTxnRef(paymentTransaction.getId());
            vnPayPaymentRequest.setAmount(amount);
            vnPayPaymentRequest.setTransactionNo(paymentTransaction.getTransactionNo());
            orderPaymentResponse.setPaymentUrl(vnPayService.createPaymentUrl(request, vnPayPaymentRequest));
        }
        return orderPaymentResponse;
    }

    /**
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

        List<SingleOrderRequest> modifyFields = updateRequest.getOrder();
        List<String>
                ids = new ArrayList<>();
        for (SingleOrderRequest x : modifyFields) {
            if (addressRepository.findById(x.getAddressId()).isEmpty()) {
                throw new InvalidDataException("Address of order " + x.getOrderId() + "not found");
            }
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

            if (updateRequest.getPaymentMethod().name().equals(PaymentMethod.VNPAY.name())) {
                response.setPaymentUrl(getVNPayReturnUrl(request, orders));
            }
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
    public OrderPaymentResponse updatePaymentPendingOrders(OrdersUpdateRequest updateRequest) {
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

    private String getVNPayReturnUrl(HttpServletRequest request, List<Order> orders) throws UnsupportedEncodingException {
        VNPayPaymentRequest vnPayPaymentRequest = new VNPayPaymentRequest();
        vnPayPaymentRequest.setOrderInfo("Thanh toan cho don hang " + orders.get(0).getPaymentTransactions().getId());
        vnPayPaymentRequest.setPaymentMethod(PaymentMethod.VNPAY);
        vnPayPaymentRequest.setAmount(getTotalAmount(orders));
        vnPayPaymentRequest.setTxnRef(orders.get(0).getPaymentTransactions().getId());
        return vnPayService.createPaymentUrl(request, vnPayPaymentRequest);
    }

    private Long getTotalAmount(List<Order> orders) {

        Long amount = 0l;
        for (Order orderItem : orders) {
            amount += orderItem.getFinalPrice().longValue();
        }
        return amount;
    }
}
