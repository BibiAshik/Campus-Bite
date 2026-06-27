package com.campusbite.dto.response;

import lombok.Data;

/**
 * Response DTO for an item within an order.
 */
@Data
public class OrderItemResponseDTO {
    private Long id;
    private String foodItemName;
    private Integer quantity;
    private Double price;
}
