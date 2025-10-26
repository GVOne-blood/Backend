package com.theblood.orderservice.mapper;


import com.theblood.orderservice.dto.request.ItemRequest;
import com.theblood.orderservice.dto.request.OrdersUpdateRequest;
import com.theblood.orderservice.dto.request.SingleOrderRequest;
import com.theblood.orderservice.dto.response.OrderDetailResponse;
import com.theblood.orderservice.model.Order;
import com.theblood.orderservice.model.OrderItem;
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

    List<SingleOrderRequest> toSingleOrderRequest(List<Order> order);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    List<Order> toOrder(List<SingleOrderRequest> request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Order toOrder(OrdersUpdateRequest updateRequest);

    @Mapping(target = "paymentMethod", ignore = true)
    OrderDetailResponse toOrderDetail(Order order);
}
