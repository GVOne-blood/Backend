package com.theblood.shopservice.service;

import com.theblood.shopservice.dto.request.StaffRequest;
import com.theblood.shopservice.dto.response.StaffResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StaffService {
    Page<StaffResponse> getStaffByShopId(String shopId, Pageable pageable);

    StaffResponse getStaffDetail(String shopId, String staffId);

    StaffResponse createStaff(String shopId, StaffRequest request);

    StaffResponse updateStaff(String shopId, String staffId, StaffRequest request);

    void deleteStaff(String shopId, String staffId);
}
