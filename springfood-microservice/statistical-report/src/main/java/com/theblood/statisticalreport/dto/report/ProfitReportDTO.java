package com.theblood.statisticalreport.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProfitReportDTO(
    LocalDateTime period,
    BigDecimal grossRevenue,
    BigDecimal totalShippingFee,
    BigDecimal totalDiscount,
    BigDecimal netProfit
) {}
