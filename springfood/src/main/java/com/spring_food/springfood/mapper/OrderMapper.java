package com.spring_food.springfood.mapper;

import com.spring_food.springfood.dto.request.ItemRequest;
import com.spring_food.springfood.dto.response.OrderDetailResponse;
import com.spring_food.springfood.model.Order;
import com.spring_food.springfood.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderItem toOrderItem(ItemRequest itemRequest);

    @Mapping(target = "paymentMethod", ignore = true)
    OrderDetailResponse toOrderDetail(Order order);
}
