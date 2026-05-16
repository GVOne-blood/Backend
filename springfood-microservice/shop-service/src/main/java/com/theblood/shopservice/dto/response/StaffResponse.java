package com.theblood.shopservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffResponse {
    String shopMemberId;
    String shopId;
    String userId;
    String roleName;
    Instant createdAt;
    Instant updatedAt;
    String department;
    String joinDate;
    String status;
    String endDate;
    String workSchedule;
    String salaryType;
    BigDecimal baseSalary;
    BigDecimal commission;
}
