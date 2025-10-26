package com.theblood.orderservice.repository;

import com.theblood.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {


//    @Query("SELECT new com.spring_food.springfood.dto.response.OrderDetailResponse(od.id, od.user.id, od.shop.id, od.createdAt, od.subtotalAmount, od.discount, od.finalPrice, od.paymentMethod.id, od.paymentStatus, od.orderStatus, od.bookingItems, od.address.id, od.deliveredAt) FROM Order od")
//    Page<OrderDetailResponse> getAllOrder(Pageable pageable);

//    @Query("SELECT o FROM Order o WHERE o.id = :orderId AND o.user.id = :userId")
//    Optional<Order> findByUserIdAndOrderId(String orderId, String userId);

}
