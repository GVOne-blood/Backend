package com.theblood.identityservice.service;


import com.theblood.identityservice.dto.response.AddressDetail;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    List<AddressDetail> findAllUserAddresses(UUID userId);

    void deleteAddresses(UUID userId, List<UUID> addressId);
}
