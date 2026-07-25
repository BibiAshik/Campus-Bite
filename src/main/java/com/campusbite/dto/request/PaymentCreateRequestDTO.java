package com.campusbite.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateRequestDTO {
    @NotNull(message = "Order ID is required")
    private Long orderId;
}
