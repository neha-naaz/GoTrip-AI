package com.tripflow.payment.dto;

import com.tripflow.payment.entity.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookRequest {

    @NotBlank
    private String providerRef;

    @NotNull
    private PaymentStatus status;
}
