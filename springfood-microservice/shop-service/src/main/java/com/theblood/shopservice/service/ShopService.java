package com.theblood.shopservice.service;

import com.theblood.shopservice.dto.response.ShopResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShopService {

    Page<ShopResponse> getAllShops(Pageable pageable);
}
