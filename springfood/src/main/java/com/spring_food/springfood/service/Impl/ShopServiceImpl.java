package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.dto.request.ShopRequest;
import com.spring_food.springfood.dto.response.ShopDetailResponse;
import com.spring_food.springfood.dto.response.UserDetail;
import com.spring_food.springfood.exception.custom.InvalidDataException;
import com.spring_food.springfood.mapper.UserMapper;
import com.spring_food.springfood.model.Shop;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.repository.ShopRepository;
import com.spring_food.springfood.service.ShopService;
import com.spring_food.springfood.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopServiceImpl implements ShopService {

    ShopRepository shopRepository;
    UserService userService;

    UserMapper userMapper;

    @Override
    public Page<ShopDetailResponse> getAllShop(Pageable pageable) {
        return null;
    }

    @Override
    public Page<UserDetail> getAllStaffs(Pageable pageable, String userId) {

        String shopId =
                shopRepository.findShopByUserId(userId);
        if (shopId.isEmpty())
            throw new InvalidDataException("shop of this user not found");
        Page<User> res = shopRepository.getAllStaffByShopId(pageable, shopId);
        return res.map(userMapper::toUserDetail);
    }

    @Override
    public ShopDetailResponse registerShop(ShopRequest shopRequest, String userId) {

        String shopId =
                shopRepository.findShopByUserId(userId);
        if (shopId.isEmpty())
            throw new InvalidDataException("shop of this user not found");

        
        return null;
    }

    @Override
    public boolean isShopExists(String shopId) {
        return shopRepository.existsById(shopId);
    }

    @Override
    public Shop getShopById(String shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new InvalidDataException("Shop not found with id: " + shopId));
    }
}
