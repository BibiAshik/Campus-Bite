package com.campusbite.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequestDTO {
    private Long foodItemId;
    private Integer quantity;
}
