package com.theblood.shopservice.service.impl;

import com.theblood.shopservice.domain.ShopMember;
import com.theblood.shopservice.dto.request.StaffRequest;
import com.theblood.shopservice.dto.response.StaffResponse;
import com.theblood.shopservice.repository.ShopMemberRepository;
import com.theblood.shopservice.service.StaffService;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffServiceImpl implements StaffService {

    private final ShopMemberRepository shopMemberRepository;

    @Override
    public Page<StaffResponse> getStaffByShopId(String shopId, Pageable pageable) {
        Page<ShopMember> members = shopMemberRepository.findByShopId(shopId, pageable);
        return members.map(this::toResponse);
    }

    @Override
    public StaffResponse getStaffDetail(String shopId, String staffId) {
        ShopMember member = shopMemberRepository.findByShopIdAndMemberId(shopId, staffId)
                .orElseThrow(() -> new InvalidDataException("Staff not found"));
        return toResponse(member);
    }

    @Override
    @Transactional
    public StaffResponse createStaff(String shopId, StaffRequest request) {
        ShopMember member = new ShopMember();
        member.setShopMemberId(UUID.randomUUID().toString());
        member.setShopId(shopId);
        member.setUserId(request.getUserId());
        member.setRoleName(request.getRoleName());
        member.setDepartment(request.getDepartment());
        member.setJoinDate(request.getJoinDate());
        member.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        member.setEndDate(request.getEndDate());
        member.setWorkSchedule(request.getWorkSchedule());
        member.setSalaryType(request.getSalaryType());
        member.setBaseSalary(request.getBaseSalary());
        member.setCommission(request.getCommission());
        member.setCreatedAt(Instant.now());
        member.setUpdatedAt(Instant.now());
        shopMemberRepository.save(member);
        return toResponse(member);
    }

    @Override
    @Transactional
    public StaffResponse updateStaff(String shopId, String staffId, StaffRequest request) {
        ShopMember member = shopMemberRepository.findByShopIdAndMemberId(shopId, staffId)
                .orElseThrow(() -> new InvalidDataException("Staff not found"));

        if (request.getRoleName() != null) member.setRoleName(request.getRoleName());
        if (request.getDepartment() != null) member.setDepartment(request.getDepartment());
        if (request.getStatus() != null) member.setStatus(request.getStatus());
        if (request.getEndDate() != null) member.setEndDate(request.getEndDate());
        if (request.getWorkSchedule() != null) member.setWorkSchedule(request.getWorkSchedule());
        if (request.getSalaryType() != null) member.setSalaryType(request.getSalaryType());
        if (request.getBaseSalary() != null) member.setBaseSalary(request.getBaseSalary());
        if (request.getCommission() != null) member.setCommission(request.getCommission());
        member.setUpdatedAt(Instant.now());
        shopMemberRepository.save(member);
        return toResponse(member);
    }

    @Override
    @Transactional
    public void deleteStaff(String shopId, String staffId) {
        ShopMember member = shopMemberRepository.findByShopIdAndMemberId(shopId, staffId)
                .orElseThrow(() -> new InvalidDataException("Staff not found"));
        shopMemberRepository.delete(member);
    }

    private StaffResponse toResponse(ShopMember member) {
        StaffResponse res = new StaffResponse();
        res.setShopMemberId(member.getShopMemberId());
        res.setShopId(member.getShopId());
        res.setUserId(member.getUserId());
        res.setRoleName(member.getRoleName());
        res.setCreatedAt(member.getCreatedAt());
        res.setUpdatedAt(member.getUpdatedAt());
        res.setDepartment(member.getDepartment());
        res.setJoinDate(member.getJoinDate());
        res.setStatus(member.getStatus());
        res.setEndDate(member.getEndDate());
        res.setWorkSchedule(member.getWorkSchedule());
        res.setSalaryType(member.getSalaryType());
        res.setBaseSalary(member.getBaseSalary());
        res.setCommission(member.getCommission());
        return res;
    }
}
