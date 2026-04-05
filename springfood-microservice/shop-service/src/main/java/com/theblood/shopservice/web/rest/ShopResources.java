package com.theblood.shopservice.web.rest;

import com.theblood.shopservice.service.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/shop")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ShopResources {

    ShopService shopService;

    @GetMapping("")
    public ResponseEntity<?> getAllShops(
        @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(shopService.getAllShops(pageable));
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<?> getUserShop(@PathVariable String shopId) {
        return ResponseEntity.ok(shopService.getShopById(shopId));
    }

    @PostMapping("")
    public ResponseEntity<?> shopRegister() {
        return ResponseEntity.ok("Shop Registered");
    }

    //phe duyet shop, chi admin moi duoc phe duyet
    @PostMapping("/approve")
    public ResponseEntity<?> shopApprove() {
        return ResponseEntity.ok("Shop Approved");
    }

    @PutMapping("")
    public ResponseEntity<?> shopUpdate() {
        return ResponseEntity.ok("Shop Updated");
    }

    @DeleteMapping("")
    public ResponseEntity<?> shopDelete() {
        return ResponseEntity.ok("Shop Deleted");
    }

}
