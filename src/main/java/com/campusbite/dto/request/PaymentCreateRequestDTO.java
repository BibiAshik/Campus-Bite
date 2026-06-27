package com.campusbite.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request to create a Razorpay order for an existing system order.
 */
@Data
public class PaymentCreateRequestDTO {
    @NotNull(message = "Order ID is required")
    private Long orderId;
}
