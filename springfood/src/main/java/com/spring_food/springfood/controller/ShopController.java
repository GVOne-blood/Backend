package com.spring_food.springfood.controller;


import com.spring_food.springfood.dto.request.ShopRequest;
import com.spring_food.springfood.dto.response.ResponseData;
import com.spring_food.springfood.dto.response.ShopDetailResponse;
import com.spring_food.springfood.dto.response.UserDetail;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.service.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopController {


    ShopService shopService;

    @GetMapping("/")
    public ResponseEntity<ResponseData<Page<ShopDetailResponse>>> getAllShop(
            @PageableDefault(size = 5, page = 0, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {

        return ResponseEntity.ok(
                new ResponseData<>(200, "get all shop successfully", shopService.getAllShop(pageable)));

    }

    @PostMapping("/")
    public ResponseEntity<ResponseData<ShopDetailResponse>> registerShop(
            @RequestBody ShopRequest shopRequest,
            @AuthenticationPrincipal User user

    ) {
        try {
            ShopDetailResponse newShop = shopService.registerShop(shopRequest, user.getId());
            return new ResponseEntity<>(new ResponseData<>(201, "create shop successfully", newShop), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ResponseData<>(400, "register shop fail : " + ex.getMessage()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @GetMapping("/member")
    public ResponseEntity<ResponseData<Page<UserDetail>>> getAllShopMember(
            @PageableDefault(size = 10, page = 0, sort = "firstName", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(new ResponseData<>(200, "get all staff of shop successfully", shopService.getAllStaffs(pageable, user.getId())));
    }


}
