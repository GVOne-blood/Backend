package com.theblood.statisticalreport.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RevenueReportDTO(
    LocalDateTime period,
    BigDecimal revenue,
    Long orderCount,
    BigDecimal avgOrderValue
) {}
