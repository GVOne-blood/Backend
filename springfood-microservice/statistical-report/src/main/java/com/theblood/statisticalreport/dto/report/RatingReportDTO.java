package com.theblood.statisticalreport.dto.report;

public record RatingReportDTO(
    Double averageRating,
    Long totalFeedbacks,
    Long star1,
    Long star2,
    Long star3,
    Long star4,
    Long star5
) {}
