package com.campusbite.controller;

import com.campusbite.entity.FoodItem;
import com.campusbite.service.FoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    // Public endpoint for students
    @GetMapping
    public List<FoodItem> getAllFood() {
        return foodService.getAllFoodItems();
    }

    // Admin endpoints
    @PostMapping
    public ResponseEntity<FoodItem> addFoodItem(@RequestBody FoodItem foodItem) {
        return ResponseEntity.ok(foodService.saveFoodItem(foodItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodItem> updateFoodItem(@PathVariable Long id, @RequestBody FoodItem foodItem) {
        FoodItem existing = foodService.getFoodItemById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        
        foodItem.setId(id);
        return ResponseEntity.ok(foodService.saveFoodItem(foodItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFoodItem(@PathVariable Long id) {
        foodService.deleteFoodItem(id);
        return ResponseEntity.ok().build();
    }
}
