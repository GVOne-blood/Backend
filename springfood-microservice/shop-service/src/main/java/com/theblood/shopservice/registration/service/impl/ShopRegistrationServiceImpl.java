package com.theblood.shopservice.registration.service.impl;

import com.theblood.shopservice.dto.request.BankAccountRegistrationDTO;
import com.theblood.shopservice.dto.request.BusinessDocDTO;
import com.theblood.shopservice.dto.request.IndividualKycDTO;
import com.theblood.shopservice.dto.request.ShopRequest;
import com.theblood.shopservice.registration.dto.ShopRegistrationStep1Request;
import com.theblood.shopservice.registration.dto.ShopRegistrationStep2Request;
import com.theblood.shopservice.registration.dto.ShopRegistrationStep3Request;
import com.theblood.shopservice.registration.dto.ShopRegistrationStep4Request;
import com.theblood.shopservice.registration.domain.ShopBusinessDocument;
import com.theblood.shopservice.registration.domain.ShopRegistrationRequest;
import com.theblood.shopservice.registration.domain.UserEkyc;
import com.theblood.shopservice.registration.repository.ShopBusinessDocumentRepository;
import com.theblood.shopservice.registration.repository.ShopRegistrationRequestRepository;
import com.theblood.shopservice.registration.repository.UserEkycRepository;
import com.theblood.shopservice.registration.service.ShopRegistrationService;
import com.theblood.shopservice.repository.ShopRepository;
import com.theblood.springfood.client.api.PaymentClient;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopRegistrationServiceImpl implements ShopRegistrationService {

    ShopRegistrationRequestRepository shopRegistrationRequestRepository;
    UserEkycRepository userEkycRepository;
    ShopBusinessDocumentRepository shopBusinessDocumentRepository;
    ShopRepository shopRepository;
    PaymentClient paymentClient;

    @Transactional
    @Override
    public UUID submitRegistration(ShopRequest request, UUID userId) {
        if (request == null) {
            throw new InvalidDataException("Shop registration request is required");
        }
        if (userId == null) {
            throw new InvalidDataException("User ID is required");
        }

        validateShopNameAvailable(request.getShopName());

        ShopRegistrationRequest registrationRequest = mapRegistrationRequest(request, userId);
        registrationRequest.setStatus("PENDING");
        ShopRegistrationRequest saved = shopRegistrationRequestRepository.save(registrationRequest);

        UserEkyc userEkyc = mapUserEkyc(request.getKyc(), userId, saved.getRequestId());
        userEkycRepository.save(userEkyc);

        BusinessDocDTO businessDoc = request.getBusinessDoc();
        if (businessDoc != null) {
            ShopBusinessDocument document = mapBusinessDoc(businessDoc, saved.getRequestId());
            shopBusinessDocumentRepository.save(document);
        }

        return saved.getRequestId();
    }

    @Transactional
    @Override
    public UUID submitStep1(ShopRegistrationStep1Request request, UUID userId) {
        if (request == null) {
            throw new InvalidDataException("Step 1 request is required");
        }
        if (userId == null) {
            throw new InvalidDataException("User ID is required");
        }
        validateShopNameAvailable(request.getShopName());

        ShopRegistrationRequest registrationRequest = new ShopRegistrationRequest();
        registrationRequest.setUserId(userId);
        registrationRequest.setShopName(request.getShopName());
        registrationRequest.setLogoMediaId(request.getLogoMediaId());
        registrationRequest.setIntroduction(request.getIntroduction());
        registrationRequest.setShopType(request.getShopType());
        registrationRequest.setBusinessType(request.getBusinessType());
        registrationRequest.setStatus("DRAFT");

        ShopRegistrationRequest saved = shopRegistrationRequestRepository.save(registrationRequest);
        return saved.getRequestId();
    }

    @Transactional
    @Override
    public UUID submitStep2(ShopRegistrationStep2Request request, UUID userId) {
        if (request == null) {
            throw new InvalidDataException("Step 2 request is required");
        }
        if (userId == null) {
            throw new InvalidDataException("User ID is required");
        }

        ShopRegistrationRequest registrationRequest = getOwnedRequest(request.getRequestId(), userId);
        registrationRequest.setShopType(request.getShopType());
        shopRegistrationRequestRepository.save(registrationRequest);

        UserEkyc userEkyc = mapUserEkyc(request.getKyc(), userId, registrationRequest.getRequestId());
        userEkycRepository.save(userEkyc);

        BusinessDocDTO businessDoc = request.getBusinessDoc();
        if (businessDoc != null) {
            ShopBusinessDocument document = mapBusinessDoc(businessDoc, registrationRequest.getRequestId());
            shopBusinessDocumentRepository.save(document);
        }

        return registrationRequest.getRequestId();
    }

    @Transactional
    @Override
    public UUID submitStep3(ShopRegistrationStep3Request request, UUID userId) {
        if (request == null) {
            throw new InvalidDataException("Step 3 request is required");
        }
        if (userId == null) {
            throw new InvalidDataException("User ID is required");
        }

        ShopRegistrationRequest registrationRequest = getOwnedRequest(request.getRequestId(), userId);
        registrationRequest.setEmail(request.getEmail());
        registrationRequest.setPhoneNumber(request.getPhoneNumber());
        registrationRequest.setShopAddress(request.getShopAddress());
        registrationRequest.setCity(request.getCity());
        registrationRequest.setProvince(request.getProvince());
        registrationRequest.setPostalCode(request.getPostalCode());
        registrationRequest.setNationId(request.getNationId());
        registrationRequest.setActiveHours(request.getActiveHours());

        shopRegistrationRequestRepository.save(registrationRequest);
        return registrationRequest.getRequestId();
    }

    @Transactional
    @Override
    public UUID submitStep4(ShopRegistrationStep4Request request, UUID userId) {
        if (request == null) {
            throw new InvalidDataException("Step 4 request is required");
        }
        if (userId == null) {
            throw new InvalidDataException("User ID is required");
        }

        ShopRegistrationRequest registrationRequest = getOwnedRequest(request.getRequestId(), userId);
        registrationRequest.setTaxId(request.getTaxId());
        registrationRequest.setStatus("PENDING");
        shopRegistrationRequestRepository.save(registrationRequest);

        BankAccountRegistrationDTO bankAccount = request.getBankAccount();
        if (bankAccount != null) {
            validateBankAccountOwner(bankAccount, registrationRequest.getRequestId());
            UUID shopId = resolveShopId(registrationRequest);
            createBankAccount(shopId, bankAccount);
        }

        return registrationRequest.getRequestId();
    }

    private void validateShopNameAvailable(String shopName) {
        if (shopName == null || shopName.isBlank()) {
            throw new InvalidDataException("Shop name is required");
        }
        if (shopRepository.existsByShopNameIgnoreCase(shopName)) {
            throw new InvalidDataException("Shop name already exists");
        }
        List<String> activeStatuses = List.of("DRAFT", "PENDING", "APPROVED");
        if (shopRegistrationRequestRepository.existsByShopNameAndStatusIn(shopName, activeStatuses)) {
            throw new InvalidDataException("Shop name is already used in another registration request");
        }
    }

    private ShopRegistrationRequest mapRegistrationRequest(ShopRequest request, UUID userId) {
        ShopRegistrationRequest reg = new ShopRegistrationRequest();
        reg.setUserId(userId);
        reg.setShopName(request.getShopName());
        reg.setLogoMediaId(request.getLogoMediaId());
        reg.setIntroduction(request.getIntroduction());
        reg.setShopType(request.getShopType());
        reg.setBusinessType(request.getBusinessType());
        reg.setEmail(request.getEmail());
        reg.setPhoneNumber(request.getPhoneNumber());
        reg.setShopAddress(request.getShopAddress());
        reg.setCity(request.getCity());
        reg.setProvince(request.getProvince());
        reg.setPostalCode(request.getPostalCode());
        reg.setNationId(request.getNationId());
        reg.setActiveHours(request.getActiveHours());
        reg.setTaxId(request.getTaxId());
        return reg;
    }

    private UserEkyc mapUserEkyc(IndividualKycDTO kyc, UUID userId, UUID requestId) {
        if (kyc == null) {
            throw new InvalidDataException("KYC information is required");
        }

        UserEkyc userEkyc = new UserEkyc();
        userEkyc.setRequestId(requestId);
        userEkyc.setUserId(userId);
        userEkyc.setIdNumber(kyc.getIdNumber());
        userEkyc.setFullName(kyc.getFullName());
        userEkyc.setDateOfBirth(kyc.getDateOfBirth());
        userEkyc.setGender(kyc.getGender());
        userEkyc.setPermanentAddress(kyc.getPermanentAddress());
        userEkyc.setIssuedDate(kyc.getIssuedDate());
        userEkyc.setIssuedPlace(kyc.getIssuedPlace());
        userEkyc.setFrontImageMediaId(kyc.getFrontImageMediaId());
        userEkyc.setBackImageMediaId(kyc.getBackImageMediaId());
        userEkyc.setSelfieMediaId(kyc.getSelfieMediaId());
        userEkyc.setNfcVerified(Boolean.TRUE.equals(kyc.getNfcVerified()));
        userEkyc.setNfcRawData(kyc.getNfcRawData());
        userEkyc.setVerificationStatus("PENDING");
        return userEkyc;
    }

    private ShopBusinessDocument mapBusinessDoc(BusinessDocDTO businessDoc, UUID requestId) {
        ShopBusinessDocument doc = new ShopBusinessDocument();
        doc.setRequestId(requestId);
        doc.setCompanyName(businessDoc.getCompanyName());
        doc.setBusinessRegNumber(businessDoc.getBusinessRegNumber());
        doc.setLicenseMediaId(businessDoc.getLicenseMediaId());
        doc.setCompanyAddress(businessDoc.getCompanyAddress());
        doc.setVerificationStatus("PENDING");
        return doc;
    }

    private ShopRegistrationRequest getOwnedRequest(UUID requestId, UUID userId) {
        if (requestId == null) {
            throw new InvalidDataException("Request ID is required");
        }
        ShopRegistrationRequest request = shopRegistrationRequestRepository.findById(requestId)
            .orElseThrow(() -> new InvalidDataException("Shop registration request not found"));
        if (!userId.equals(request.getUserId())) {
            throw new InvalidDataException("Request does not belong to user");
        }
        return request;
    }

    private void validateBankAccountOwner(BankAccountRegistrationDTO bankAccount, UUID requestId) {
        if (bankAccount.getAccountHolderName() == null || bankAccount.getAccountHolderName().isBlank()) {
            throw new InvalidDataException("Account holder name is required");
        }
        UserEkyc userEkyc = userEkycRepository.findTopByRequestId(requestId)
            .orElseThrow(() -> new InvalidDataException("KYC information is required before adding bank account"));
        if (userEkyc.getFullName() != null && !userEkyc.getFullName().equalsIgnoreCase(bankAccount.getAccountHolderName())) {
            throw new InvalidDataException("Account holder name must match KYC full name");
        }
    }

    private void createBankAccount(UUID shopId, BankAccountRegistrationDTO bankAccount) {
        if (shopId == null) {
            throw new InvalidDataException("Shop ID is required before creating bank account");
        }
        PaymentClient.BankAccountCreateRequest request = PaymentClient.BankAccountCreateRequest.builder()
            .shopId(shopId)
            .bankName(bankAccount.getBankName())
            .accountNumber(bankAccount.getAccountNumber())
            .accountHolderName(bankAccount.getAccountHolderName())
            .build();
        var response = paymentClient.createBankAccount(request);
        if (response == null || !response.isSuccess()) {
            throw new InvalidDataException("Failed to create bank account");
        }
    }

    private UUID resolveShopId(ShopRegistrationRequest registrationRequest) {
        if (registrationRequest.getShopId() != null) {
            return registrationRequest.getShopId();
        }
        return shopRepository.findShopByOwnerId(registrationRequest.getUserId().toString())
            .map(shop -> shop.getShopId())
            .orElseThrow(() -> new InvalidDataException("Shop ID not found for user"));
    }
}
