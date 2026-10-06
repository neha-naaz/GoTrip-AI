package com.tripflow.payment.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmCheckoutRequest {

    @NotBlank
    @JsonAlias("razorpay_order_id")
    private String orderId;

    @NotBlank
    @JsonAlias("razorpay_payment_id")
    private String paymentId;

    @NotBlank
    @JsonAlias("razorpay_signature")
    private String signature;
}
