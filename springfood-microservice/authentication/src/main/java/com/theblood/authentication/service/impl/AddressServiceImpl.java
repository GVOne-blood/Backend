package com.theblood.authentication.service.impl;

import com.theblood.authentication.dto.request.AddressRequest;
import com.theblood.authentication.dto.response.AddressDetail;
import com.theblood.authentication.model.Address;
import com.theblood.authentication.model.User;
import com.theblood.authentication.repository.AddressRepository;
import com.theblood.authentication.repository.UserRepository;
import com.theblood.authentication.service.AddressService;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    AddressRepository addressRepository;
    UserRepository userRepository;

    @Override
    public List<AddressDetail> findAllUserAddresses(UUID userId) {
        return addressRepository.findAllUserAddressesByUserId(userId).stream()
            .map(this::toDetail)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressDetail createAddress(UUID userId, AddressRequest request) {
        validateRequest(request);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidDataException("User not found: " + userId));

        long existingCount = addressRepository.countByUserId(userId);
        boolean shouldDefault = Boolean.TRUE.equals(request.getIsDefault()) || existingCount == 0;

        if (shouldDefault) {
            addressRepository.unsetDefaultForUser(userId);
        }

        Address address = new Address();
        applyRequest(address, request);
        address.setUser(user);
        address.setDefault(shouldDefault);

        Address saved = addressRepository.save(address);
        log.info("Created address {} for user {}", saved.getId(), userId);
        return toDetail(saved);
    }

    @Override
    @Transactional
    public AddressDetail updateAddress(UUID userId, UUID addressId, AddressRequest request) {
        validateRequest(request);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
            .orElseThrow(() -> new InvalidDataException(
                "Address " + addressId + " not found for user " + userId));

        // Nếu request set default = true → unset cái cũ trước
        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.isDefault()) {
            addressRepository.unsetDefaultForUser(userId);
            address.setDefault(true);
        } else if (Boolean.FALSE.equals(request.getIsDefault())) {
            // Cấm bỏ default nếu đây là địa chỉ default duy nhất
            if (address.isDefault()) {
                throw new InvalidDataException(
                    "Cannot unset default. Set another address as default first.");
            }
        }

        applyRequest(address, request);
        Address saved = addressRepository.save(address);
        log.info("Updated address {} for user {}", saved.getId(), userId);
        return toDetail(saved);
    }

    @Override
    @Transactional
    public AddressDetail setDefaultAddress(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
            .orElseThrow(() -> new InvalidDataException(
                "Address " + addressId + " not found for user " + userId));

        if (address.isDefault()) {
            return toDetail(address);
        }

        addressRepository.unsetDefaultForUser(userId);
        address.setDefault(true);
        Address saved = addressRepository.save(address);
        log.info("Set address {} as default for user {}", saved.getId(), userId);
        return toDetail(saved);
    }

    @Override
    @Transactional
    public void deleteAddresses(UUID userId, List<UUID> addressIds) {
        if (addressIds == null || addressIds.isEmpty()) {
            throw new InvalidDataException("addressIds must not be empty");
        }

        // Chỉ xoá các address thuộc user (chống IDOR)
        List<Address> ownedAddresses = addressIds.stream()
            .map(id -> addressRepository.findByIdAndUserId(id, userId).orElse(null))
            .filter(a -> a != null)
            .collect(Collectors.toList());

        if (ownedAddresses.isEmpty()) {
            log.warn("No owned addresses to delete from list {} for user {}", addressIds, userId);
            return;
        }

        boolean deletingDefault = ownedAddresses.stream().anyMatch(Address::isDefault);
        addressRepository.deleteAll(ownedAddresses);

        // Nếu xoá default → promote address tiếp theo lên làm default
        if (deletingDefault) {
            List<Address> remaining = addressRepository.findAllUserAddressesByUserId(userId);
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setDefault(true);
                addressRepository.save(newDefault);
                log.info("Promoted address {} to default for user {}", newDefault.getId(), userId);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void validateRequest(AddressRequest request) {
        if (request == null) {
            throw new InvalidDataException("Address request must not be null");
        }
    }

    /**
     * Apply field từ request vào entity. Bỏ qua field null/blank để hỗ trợ
     * partial update (FE chỉ gửi field cần đổi).
     */
    private void applyRequest(Address address, AddressRequest request) {
        if (notBlank(request.getLabel())) address.setLabel(request.getLabel());
        if (notBlank(request.getRecipientName())) address.setRecipientName(request.getRecipientName());
        if (notBlank(request.getPhoneNumber())) address.setPhoneNumber(request.getPhoneNumber());
        if (notBlank(request.getStreetAddress())) address.setStreet(request.getStreetAddress());
        if (notBlank(request.getWard())) address.setWard(request.getWard());
        if (notBlank(request.getDistrict())) address.setDistrict(request.getDistrict());
        if (notBlank(request.getCity())) address.setCity(request.getCity());
        if (request.getDetails() != null) address.setDetails(request.getDetails());
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private AddressDetail toDetail(Address a) {
        return AddressDetail.builder()
            .id(a.getId() != null ? a.getId().toString() : null)
            .label(a.getLabel())
            .recipientName(a.getRecipientName())
            .phoneNumber(a.getPhoneNumber())
            .streetAddress(a.getStreet())
            .ward(a.getWard())
            .district(a.getDistrict())
            .city(a.getCity())
            .details(a.getDetails())
            .isDefault(a.isDefault())
            .build();
    }
}
