package com.theblood.authentication.service;


import com.theblood.authentication.dto.response.AddressDetail;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    List<AddressDetail> findAllUserAddresses(UUID userId);

    void deleteAddresses(UUID userId, List<UUID> addressId);
}
