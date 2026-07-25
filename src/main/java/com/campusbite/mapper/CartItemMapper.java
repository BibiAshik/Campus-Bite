package com.campusbite.mapper;

import com.campusbite.dto.response.CartItemResponseDTO;
import com.campusbite.entity.CartItem;
import com.campusbite.entity.FoodItem;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {

    public CartItemResponseDTO toResponseDTO(CartItem item) {
        if (item == null) {
            return null;
        }
        FoodItem food = item.getFoodItem();
        return CartItemResponseDTO.builder()
                .id(item.getId())
                .foodItemId(food.getId())
                .name(food.getName())
                .description(food.getDescription())
                .category(food.getCategory())
                .price(food.getPrice())
                .imageUrl(food.getImageUrl())
                .isVeg(food.getIsVeg())
                .quantity(item.getQuantity())
                .totalItemPrice(food.getPrice() * item.getQuantity())
                .build();
    }
}
