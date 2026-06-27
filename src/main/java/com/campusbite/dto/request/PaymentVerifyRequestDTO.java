package com.campusbite.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request to verify a Razorpay payment signature.
 */
@Data
public class PaymentVerifyRequestDTO {
    @NotBlank(message = "Razorpay order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;
}
