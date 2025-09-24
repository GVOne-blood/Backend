package com.spring_food.springfood.repository;

import com.spring_food.springfood.dto.response.OrderDetailResponse;
import com.spring_food.springfood.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {


    @Query("SELECT new com.spring_food.springfood.dto.response.OrderDetailResponse(od.id, od.user.id, od.shop.id, od.createdAt, od.subtotalAmount, od.discount, od.finalPrice, od.paymentMethod.id, od.paymentStatus, od.orderStatus, od.bookingItems, od.address.id, od.deliveredAt) FROM Order od")
    Page<OrderDetailResponse> getAllOrder(Pageable pageable);


}
