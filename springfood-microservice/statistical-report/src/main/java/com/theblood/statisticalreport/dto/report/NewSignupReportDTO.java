package com.theblood.statisticalreport.dto.report;

import java.time.LocalDateTime;

/**
 * Số liệu user/shop mới đăng ký theo từng bucket thời gian.
 *
 * @param period    Mốc bắt đầu bucket.
 * @param newUsers  Số user tạo mới trong bucket.
 * @param newShops  Số shop tạo mới trong bucket (đã có record trong springfood_shop.shops).
 */
public record NewSignupReportDTO(
    LocalDateTime period,
    long newUsers,
    long newShops
) {}
