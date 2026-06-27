package com.campusbite.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for a payment.
 */
@Data
public class PaymentResponseDTO {
    private Long id;
    private Long orderId;
    private String orderToken;
    private String studentEmail;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String status;
    private Double amount;
    private LocalDateTime createdAt;
}
