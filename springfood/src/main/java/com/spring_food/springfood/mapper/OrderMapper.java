package com.spring_food.springfood.mapper;

import com.spring_food.springfood.dto.request.ItemRequest;
import com.spring_food.springfood.dto.request.OrdersUpdateRequest;
import com.spring_food.springfood.dto.request.SingleOrderRequest;
import com.spring_food.springfood.dto.response.OrderDetailResponse;
import com.spring_food.springfood.model.Order;
import com.spring_food.springfood.model.OrderItem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderItem toOrderItem(ItemRequest itemRequest);


    @Mapping(target = "id", ignore = true)
    Order toOrder(SingleOrderRequest singleOrderRequest);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    List<Order> toOrder(List<SingleOrderRequest> request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Order toOrder(OrdersUpdateRequest updateRequest);

    @Mapping(target = "paymentMethod", ignore = true)
    OrderDetailResponse toOrderDetail(Order order);
}
