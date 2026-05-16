package com.theblood.statisticalreport.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlatformCommissionDTO(
    LocalDateTime period,
    BigDecimal commission,
    Long orderCount
) {}
