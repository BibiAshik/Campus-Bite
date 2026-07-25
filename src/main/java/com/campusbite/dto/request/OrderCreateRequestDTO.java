package com.campusbite.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequestDTO {
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequestDTO> items;

    private String pickupTime;
}
