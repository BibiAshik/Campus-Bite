package com.campusbite.dto.response;

import lombok.Data;

/**
 * Response DTO for a food item.
 */
@Data
public class FoodItemResponseDTO {
    private Long id;
    private String name;
    private String category;
    private Double price;
    private String imageUrl;
    private Integer quantityAvailable;
    private boolean isVeg;
    private String description;
    
    // Only populated when a student is logged in, false otherwise
    private boolean isFavorite;
}
