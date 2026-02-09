package com.theblood.shopservice.controller;

import com.theblood.common.dto.response.ResponseData;
import com.theblood.shopservice.dto.response.ShopResponse;
import com.theblood.shopservice.service.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class ShopController {

    ShopService shopService;

    @GetMapping("/")
    public ResponseData<Page<ShopResponse>> getAllShops(@PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return new ResponseData<>(200, "Success", shopService.getAllShops(pageable));
    }


}
