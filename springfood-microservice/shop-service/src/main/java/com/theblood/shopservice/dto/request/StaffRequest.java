package com.theblood.shopservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffRequest {
    String shopMemberId;

    @NotBlank(message = "User ID is required")
    String userId;

    @NotBlank(message = "Role name is required")
    String roleName;

    String department;
    String joinDate;
    String status;
    String endDate;
    String workSchedule;
    String salaryType;
    BigDecimal baseSalary;
    BigDecimal commission;
}
