package com.theblood.statisticalreport.service.carbone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class CarboneBody {
    Object fileName;
    CarboneOption options;
    Object data;
    String convertTo;
    Object dataset;
}
