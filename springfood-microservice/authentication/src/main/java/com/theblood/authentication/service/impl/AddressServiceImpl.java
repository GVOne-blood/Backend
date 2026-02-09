package com.theblood.authentication.service.impl;

import com.theblood.authentication.dto.response.AddressDetail;
import com.theblood.authentication.model.Address;
import com.theblood.authentication.repository.AddressRepository;
import com.theblood.authentication.service.AddressService;
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
