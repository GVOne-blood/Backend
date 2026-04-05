package com.theblood.statisticalreport.service.carbone.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CarboneOption {
    String convertTo;
    String responseType;
    String reportName;
    String timezone;
    String lang;
    Boolean hardRefresh;
    Boolean saveTarget;
}
