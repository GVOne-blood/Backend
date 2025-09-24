package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.request.ShopRequest;
import com.spring_food.springfood.dto.response.ShopDetailResponse;
import com.spring_food.springfood.dto.response.UserDetail;
import com.spring_food.springfood.model.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShopService {

    Page<ShopDetailResponse> getAllShop(Pageable pageable);

    Page<UserDetail> getAllStaffs(Pageable pageable, String userId);

    ShopDetailResponse registerShop(ShopRequest shopRequest, String userId);

    boolean isShopExists(String shopId);

    Shop getShopById(String shopId);
}
