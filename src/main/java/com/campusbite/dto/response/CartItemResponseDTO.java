package com.campusbite.dto.response;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class CartItemResponseDTO {
    private Long id;
    private Long foodItemId;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private Boolean isVeg;
    private Integer quantity;
    private Double totalItemPrice;
}
