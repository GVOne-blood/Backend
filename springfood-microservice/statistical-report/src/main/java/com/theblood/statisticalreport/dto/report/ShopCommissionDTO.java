package com.theblood.statisticalreport.dto.report;

import java.math.BigDecimal;
import java.util.UUID;

public record ShopCommissionDTO(
    UUID shopId,
    String shopName,
    BigDecimal commission,
    Long orderCount
) {}
