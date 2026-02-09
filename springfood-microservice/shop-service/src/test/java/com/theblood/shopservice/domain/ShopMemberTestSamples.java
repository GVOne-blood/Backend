package com.theblood.shopservice.domain;

import java.util.UUID;

public class ShopMemberTestSamples {

    public static ShopMember getShopMemberSample1() {
        return new ShopMember()
            .shopMemberId("shopMemberId1")
            .shopId("shopId1")
            .userId("userId1")
            .roleName("roleName1")
            .department("department1")
            .joinDate("joinDate1")
            .status("status1")
            .endDate("endDate1")
            .workSchedule("workSchedule1")
            .salaryType("salaryType1");
    }

    public static ShopMember getShopMemberSample2() {
        return new ShopMember()
            .shopMemberId("shopMemberId2")
            .shopId("shopId2")
            .userId("userId2")
            .roleName("roleName2")
            .department("department2")
            .joinDate("joinDate2")
            .status("status2")
            .endDate("endDate2")
            .workSchedule("workSchedule2")
            .salaryType("salaryType2");
    }

    public static ShopMember getShopMemberRandomSampleGenerator() {
        return new ShopMember()
            .shopMemberId(UUID.randomUUID().toString())
            .shopId(UUID.randomUUID().toString())
            .userId(UUID.randomUUID().toString())
            .roleName(UUID.randomUUID().toString())
            .department(UUID.randomUUID().toString())
            .joinDate(UUID.randomUUID().toString())
            .status(UUID.randomUUID().toString())
            .endDate(UUID.randomUUID().toString())
            .workSchedule(UUID.randomUUID().toString())
            .salaryType(UUID.randomUUID().toString());
    }
}
