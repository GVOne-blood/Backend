package com.theblood.authentication.service;


import com.theblood.authentication.dto.request.AddressRequest;
import com.theblood.authentication.dto.response.AddressDetail;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    /** GET /profile/addr — list địa chỉ của user, default lên đầu. */
    List<AddressDetail> findAllUserAddresses(UUID userId);

    /** POST /profile/addr — tạo địa chỉ mới. Tự set default nếu lần đầu hoặc request.isDefault=true. */
    AddressDetail createAddress(UUID userId, AddressRequest request);

    /** PUT /profile/addr/{addressId} — cập nhật địa chỉ. */
    AddressDetail updateAddress(UUID userId, UUID addressId, AddressRequest request);

    /** PATCH /profile/addr/{addressId}/default — đặt làm default, bỏ default khỏi các địa chỉ khác. */
    AddressDetail setDefaultAddress(UUID userId, UUID addressId);

    /** DELETE /profile/addr — xoá nhiều địa chỉ theo list ID. */
    void deleteAddresses(UUID userId, List<UUID> addressId);
}
