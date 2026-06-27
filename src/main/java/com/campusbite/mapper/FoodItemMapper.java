package com.campusbite.mapper;

import com.campusbite.dto.request.FoodItemRequestDTO;
import com.campusbite.dto.response.FoodItemResponseDTO;
import com.campusbite.entity.FoodItem;
import org.springframework.stereotype.Component;

@Component
public class FoodItemMapper {

    public FoodItem toEntity(FoodItemRequestDTO dto) {
        FoodItem entity = new FoodItem();
        entity.setName(dto.getName());
        entity.setCategory(dto.getCategory());
        entity.setPrice(dto.getPrice());
        entity.setQuantityAvailable(dto.getQuantityAvailable());
        entity.setIsVeg(dto.isVeg());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    public FoodItemResponseDTO toResponseDTO(FoodItem entity) {
        FoodItemResponseDTO dto = new FoodItemResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCategory(entity.getCategory());
        dto.setPrice(entity.getPrice());
        dto.setImageUrl(entity.getImageUrl());
        dto.setQuantityAvailable(entity.getQuantityAvailable());
        dto.setVeg(entity.getIsVeg() != null ? entity.getIsVeg() : false);
        dto.setDescription(entity.getDescription());
        dto.setFavorite(false); // Default to false
        return dto;
    }
}
