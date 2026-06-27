package com.campusbite.service;

import com.campusbite.dto.request.FoodItemRequestDTO;
import com.campusbite.dto.response.FoodItemResponseDTO;
import com.campusbite.entity.FoodItem;
import com.campusbite.exception.ResourceNotFoundException;
import com.campusbite.mapper.FoodItemMapper;
import com.campusbite.repository.FoodItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing food items.
 * Methods are ordered from simplest (read/find) to most complex (create/update with image upload).
 */
@Service
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;
    private final FoodItemMapper foodItemMapper;
    private final ImageUploadService imageUploadService;

    public FoodItemService(FoodItemRepository foodItemRepository, FoodItemMapper foodItemMapper, ImageUploadService imageUploadService) {
        this.foodItemRepository = foodItemRepository;
        this.foodItemMapper = foodItemMapper;
        this.imageUploadService = imageUploadService;
    }

    // --- Simple Read Operations ---

    /**
     * Purpose: Fetch all food items as raw entities (kept for backward compatibility where needed).
     * Output: List of FoodItem entities.
     */
    public List<FoodItem> getAllFoodItems() {
        return foodItemRepository.findAll();
    }

    /**
     * Purpose: Fetch all food items as DTOs for the frontend.
     * Output: List of FoodItemResponseDTO.
     */
    public List<FoodItemResponseDTO> getAllFoodItemsAsDTO() {
        return foodItemRepository.findAll().stream()
                .map(foodItemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Purpose: Fetch a single food item by ID as a raw entity (kept for backward compatibility).
     */
    public FoodItem getFoodItemById(Long id) {
        return foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + id));
    }

    /**
     * Purpose: Fetch a single food item by ID as DTO.
     * Output: FoodItemResponseDTO.
     */
    public FoodItemResponseDTO getFoodItemByIdAsDTO(Long id) {
        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + id));
        return foodItemMapper.toResponseDTO(foodItem);
    }

    // --- Simple Write Operations ---

    /**
     * Purpose: Save a raw food item (kept for backward compatibility).
     */
    public FoodItem saveFoodItem(FoodItem foodItem) {
        return foodItemRepository.save(foodItem);
    }

    /**
     * Purpose: Delete a food item by ID. Also deletes uploaded image if present.
     */
    public void deleteFoodItem(Long id) {
        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + id));
        imageUploadService.deleteImageIfUploaded(foodItem.getImageUrl());
        foodItemRepository.deleteById(id);
    }

    // --- Complex Write Operations (with image upload handling) ---

    /**
     * Purpose: Saves a new food item with an optional uploaded image.
     * Input: FoodItemRequestDTO — the food item details from the admin form.
     *        MultipartFile — the uploaded image file, nullable (admin may not upload one).
     * Output: FoodItemResponseDTO of the saved food item.
     */
    public FoodItemResponseDTO addFoodItem(FoodItemRequestDTO dto, MultipartFile image) throws IOException {
        FoodItem foodItem = foodItemMapper.toEntity(dto);

        if (image != null && !image.isEmpty()) {
            // Admin uploaded an image — save it and store the URL
            String imageUrl = imageUploadService.saveImage(image);
            foodItem.setImageUrl(imageUrl);
        } else {
            // No image uploaded — use a placeholder path
            foodItem.setImageUrl("/images/food/placeholder.jpg");
        }

        FoodItem saved = foodItemRepository.save(foodItem);
        return foodItemMapper.toResponseDTO(saved);
    }

    /**
     * Purpose: Updates an existing food item. If a new image is uploaded, replaces the old one.
     *          If no new image is uploaded, keeps the existing image unchanged.
     * Input: id — the food item ID to update.
     *        FoodItemRequestDTO — updated details.
     *        MultipartFile — new image file, nullable.
     * Output: FoodItemResponseDTO of the updated food item.
     */
    public FoodItemResponseDTO updateFoodItem(Long id, FoodItemRequestDTO dto, MultipartFile image) throws IOException {
        FoodItem existing = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + id));

        // Update all text fields
        existing.setName(dto.getName());
        existing.setCategory(dto.getCategory());
        existing.setPrice(dto.getPrice());
        existing.setIsVeg(dto.isVeg());
        existing.setDescription(dto.getDescription());
        existing.setQuantityAvailable(dto.getQuantityAvailable());

        if (image != null && !image.isEmpty()) {
            // Admin uploaded a new image — delete the old uploaded image (if it was one)
            // then save the new one
            imageUploadService.deleteImageIfUploaded(existing.getImageUrl());
            String newImageUrl = imageUploadService.saveImage(image);
            existing.setImageUrl(newImageUrl);
            // If no new image -> keep existing.getImageUrl() unchanged — do nothing
        }

        FoodItem saved = foodItemRepository.save(existing);
        return foodItemMapper.toResponseDTO(saved);
    }
}
