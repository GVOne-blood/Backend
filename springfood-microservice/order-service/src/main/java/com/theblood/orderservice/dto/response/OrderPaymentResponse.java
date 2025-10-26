package com.theblood.orderservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // Quan trọng: Các trường null sẽ không được serialize ra JSON
public class OrderPaymentResponse {

    private String transactionId;

    private String paymentUrl;

    private Long amount;

    private List<OrderDetailResponse> orderDetails;

    private BigDecimal totalShippingFee;

}
