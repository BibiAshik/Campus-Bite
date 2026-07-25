package com.campusbite.controller;

import com.campusbite.dto.request.FoodItemRequestDTO;
import com.campusbite.dto.response.FoodItemResponseDTO;
import com.campusbite.entity.FoodItem;
import com.campusbite.mapper.FoodItemMapper;
import com.campusbite.service.FoodItemService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/food")
public class FoodItemController {

    private final FoodItemService foodItemService;
    private final FoodItemMapper foodItemMapper;

    public FoodItemController(FoodItemService foodItemService, FoodItemMapper foodItemMapper) {
        this.foodItemService = foodItemService;
        this.foodItemMapper = foodItemMapper;
    }

    /**
     * Public endpoint for anyone to view the menu.
     */
    @GetMapping
    public List<FoodItemResponseDTO> getAllFood() {
        return foodItemService.getAllFoodItems().stream()
                .map(foodItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Admin creates a new food item with an optional image upload.
     * Consumes multipart/form-data.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodItemResponseDTO> addFoodItem(
            @Valid @ModelAttribute FoodItemRequestDTO dto,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        
        FoodItem foodItem = foodItemMapper.toEntity(dto);
        FoodItem created = foodItemService.addFoodItem(foodItem, image);
        return ResponseEntity.ok(foodItemMapper.toResponseDTO(created));
    }

    /**
     * Admin updates an existing food item. Optional new image upload.
     * Consumes multipart/form-data.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodItemResponseDTO> updateFoodItem(
            @PathVariable Long id,
            @Valid @ModelAttribute FoodItemRequestDTO dto,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        
        FoodItem incomingItem = foodItemMapper.toEntity(dto);
        FoodItem updated = foodItemService.updateFoodItem(id, incomingItem, image);
        return ResponseEntity.ok(foodItemMapper.toResponseDTO(updated));
    }

    /**
     * Admin deletes a food item.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFoodItem(@PathVariable Long id) {
        foodItemService.deleteFoodItem(id);
        return ResponseEntity.ok().build();
    }
}
