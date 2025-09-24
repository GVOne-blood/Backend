package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.request.ShippingAddressRequest;

public interface AddressService {

    void registerAddress(ShippingAddressRequest request);
}
