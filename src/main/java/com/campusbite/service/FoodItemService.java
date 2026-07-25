package com.campusbite.service;

import com.campusbite.entity.FoodItem;
import com.campusbite.exception.ResourceNotFoundException;
import com.campusbite.repository.FoodItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service for managing food items.
 * Methods are ordered from simplest (read/find) to most complex (create/update with image upload).
 */
@Service
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;
    private final ImageUploadService imageUploadService;

    public FoodItemService(FoodItemRepository foodItemRepository, ImageUploadService imageUploadService) {
        this.foodItemRepository = foodItemRepository;
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
     * Purpose: Fetch a single food item by ID as a raw entity (kept for backward compatibility).
     */
    public FoodItem getFoodItemById(Long id) {
        return foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + id));
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
     */
    public FoodItem addFoodItem(FoodItem foodItem, MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            // Admin uploaded an image — save it and store the URL
            String imageUrl = imageUploadService.saveImage(image);
            foodItem.setImageUrl(imageUrl);
        } else {
            // No image uploaded — use a placeholder path
            foodItem.setImageUrl("/images/food/placeholder.jpg");
        }

        return foodItemRepository.save(foodItem);
    }

    /**
     * Purpose: Updates an existing food item. If a new image is uploaded, replaces the old one.
     */
    public FoodItem updateFoodItem(Long id, FoodItem incomingItem, MultipartFile image) throws IOException {
        FoodItem existing = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + id));

        // Update all text fields
        existing.setName(incomingItem.getName());
        existing.setCategory(incomingItem.getCategory());
        existing.setPrice(incomingItem.getPrice());
        existing.setIsVeg(incomingItem.getIsVeg());
        existing.setDescription(incomingItem.getDescription());
        existing.setQuantityAvailable(incomingItem.getQuantityAvailable());

        if (image != null && !image.isEmpty()) {
            // Admin uploaded a new image — delete the old uploaded image (if it was one)
            // then save the new one
            imageUploadService.deleteImageIfUploaded(existing.getImageUrl());
            String newImageUrl = imageUploadService.saveImage(image);
            existing.setImageUrl(newImageUrl);
            // If no new image -> keep existing.getImageUrl() unchanged — do nothing
        }

        return foodItemRepository.save(existing);
    }
}
