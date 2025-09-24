package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.common.enums.OrderStatus;
import com.spring_food.springfood.common.enums.PaymentMethod;
import com.spring_food.springfood.common.enums.ProductStatus;
import com.spring_food.springfood.common.enums.TransactionStatus;
import com.spring_food.springfood.config.VNPayConfig;
import com.spring_food.springfood.dto.request.ItemRequest;
import com.spring_food.springfood.dto.request.OrderRequest;
import com.spring_food.springfood.dto.request.ShopOrderRequest;
import com.spring_food.springfood.dto.request.VNPayPaymentRequest;
import com.spring_food.springfood.dto.response.OrderDetailResponse;
import com.spring_food.springfood.dto.response.OrderPaymentResponse;
import com.spring_food.springfood.dto.response.ProductDetail;
import com.spring_food.springfood.exception.custom.InvalidDataException;
import com.spring_food.springfood.mapper.OrderMapper;
import com.spring_food.springfood.mapper.ProductMapper;
import com.spring_food.springfood.model.*;
import com.spring_food.springfood.repository.*;
import com.spring_food.springfood.service.OrderService;
import com.spring_food.springfood.service.ProductService;
import com.spring_food.springfood.service.ShopService;
import com.spring_food.springfood.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    ShopService shopService;
    OrderItemRepository orderItemRepository;
    VNPayService vnPayService;
    VNPayConfig vnPayConfig;

    ProductMapper productMapper;
    OrderMapper orderMapper;
    UserRepository userRepository;

    @Override
    public Page<OrderDetailResponse> getListOrderForAdmin(Pageable pageable, String userId) {
        return orderRepository.getAllOrder(pageable);
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

        String transactionId = VNPayConfig.getRandomNumber(8);
        List<OrderDetailResponse> orderDetailResponseList = new ArrayList<>();
        OrderDetailResponse orderDetailResponse = new OrderDetailResponse();
        List<ShopOrderRequest> shops = orderRequest.getShopOrderItems();
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
            Payment payment = paymentRepository.findById(orderRequest.getPaymentInfo()
                    .getPaymentMethod()).orElseThrow(() -> new InvalidDataException("Invalid Payment method"));

            if (!payment.getIsActive()) throw new InvalidDataException("Payment method is not active");
            order.setPaymentMethod(payment);
            //order status và payment status
            if (payment.getId().equals(PaymentMethod.VNPAY.name())) {
                order.setOrderStatus(OrderStatus.PENDING_PAYMENT);
                order.setPaymentStatus(TransactionStatus.PENDING); // Chờ thanh toán VNPay
                //transaction code
                order.setPaymentTransactionId(transactionId);
            } else {
                order.setOrderStatus(OrderStatus.PENDING);
                order.setPaymentStatus(TransactionStatus.PENDING); // Chờ xác nhận
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

            orderRepository.save(order);


            for (OrderItem item : orderItems) {
                item.setOrder(order);
                orderItemRepository.save(item);
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
        }
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
        orderPaymentResponse.setTransactionId(transactionId);
        if (orderDetailResponse.getPaymentMethod().equals(PaymentMethod.VNPAY.name())) {
            VNPayPaymentRequest vnPayPaymentRequest = new VNPayPaymentRequest();
            vnPayPaymentRequest.setOrderInfo(orderPaymentInfo.toString());
            vnPayPaymentRequest.setAmount(amount);
            vnPayPaymentRequest.setGeneratedTransactionId(orderPaymentResponse.getTransactionId());
            orderPaymentResponse.setPaymentUrl(vnPayService.createPaymentUrl(request, vnPayPaymentRequest));
        }
        return orderPaymentResponse;
    }


    @Override
    public void deleteOrder(String orderId) {

    }
}
