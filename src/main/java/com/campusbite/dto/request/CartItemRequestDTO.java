package com.campusbite.dto.request;

import lombok.Data;

@Data
public class CartItemRequestDTO {
    private Long foodItemId;
    private Integer quantity;
}
