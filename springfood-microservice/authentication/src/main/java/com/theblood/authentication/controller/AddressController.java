package com.theblood.authentication.controller;


import com.theblood.authentication.dto.response.AddressDetail;
import com.theblood.authentication.service.AddressService;
import com.theblood.common.dto.request.CustomUserPrincipal;
import com.theblood.common.dto.response.ResponseData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("profile/addr")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AddressController {
    AddressService addressService;

    @GetMapping("/")
    public ResponseData<List<AddressDetail>> getUserAddresses(@AuthenticationPrincipal CustomUserPrincipal user) {
        return new ResponseData<>(200, "Get user addresses successfully ", addressService.findAllUserAddresses(user.getUserId()));

    }

    @DeleteMapping
    public ResponseData<?> deleteUserAddress(@AuthenticationPrincipal CustomUserPrincipal user, @RequestBody List<UUID> addressIds) {
        try {
            addressService.deleteAddresses(user.getUserId(), addressIds);
            return new ResponseData<>(204, "Delete address by user id : " + user.getUserId() + "\n completed ", null);
        } catch (Exception ex) {
            return new ResponseData<>(400, "Delete address by user id : " + user.getUserId() + "\n fail ", null);
        }
    }
}
