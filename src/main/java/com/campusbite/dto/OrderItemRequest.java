package com.campusbite.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long foodItemId;
    private Integer quantity;
}
