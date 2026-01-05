package com.theblood.identityservice.service.impl;

import com.theblood.identityservice.dto.response.AddressDetail;
import com.theblood.identityservice.model.Address;
import com.theblood.identityservice.repository.AddressRepository;
import com.theblood.identityservice.service.AddressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequiredArgsConstructor

public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    public List<AddressDetail> findAllUserAddresses(UUID userId) {
        List<Address> addresses = addressRepository.findAllUserAddressesByUserId(userId);

        List<AddressDetail> addressDetails = new ArrayList<>();
        addresses.forEach(address -> {
            AddressDetail addressDetail = new AddressDetail();
            addressDetail.setDetails(""" 
                    %s %s %s %s 
                    """.formatted(address.getDetails(), address.getStreet(), address.getWard(), address.getCity()));
            addressDetails.add(addressDetail);
        });
        return addressDetails;
    }

    @Override
    public void deleteAddresses(UUID userId, List<UUID> addressIds) {

        // check user
        addressRepository.deleteAllById(addressIds);
    }


}
