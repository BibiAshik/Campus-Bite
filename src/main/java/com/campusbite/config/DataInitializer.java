package com.campusbite.config;

import com.campusbite.entity.AdminUser;
import com.campusbite.entity.FoodItem;
import com.campusbite.repository.AdminUserRepository;
import com.campusbite.repository.FoodItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final FoodItemRepository foodItemRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AdminUserRepository adminUserRepository, FoodItemRepository foodItemRepository,
            PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.foodItemRepository = foodItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize Admin if not exists
        if (adminUserRepository.count() == 0) {
            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            adminUserRepository.save(admin);
        }

        // Initialize Menu if empty
        if (foodItemRepository.count() == 0) {
            List<FoodItem> items = List.of(
                    new FoodItem(null, "Classic Burger", "Non-Veg", 120.0, "burger.jpg", 50, false,
                            "Juicy chicken patty with fresh lettuce and cheese."),
                    new FoodItem(null, "Veggie Sandwich", "Veg", 60.0, "sandwich.jpg", 30, true,
                            "Healthy sandwich with fresh cucumber, tomato, and mayo."),
                    new FoodItem(null, "Chicken Fried Rice", "Non-Veg", 150.0, "fried-rice.jpg", 40, false,
                            "Wok-tossed rice with chicken chunks and veggies."),
                    new FoodItem(null, "Chicken Shawarma", "Non-Veg", 100.0, "shawarma.jpg", 60, false,
                            "Authentic Arabic style chicken shawarma wrap."),
                    new FoodItem(null, "French Fries", "Veg", 80.0, "fries.jpg", 100, true,
                            "Crispy golden french fries with peri-peri seasoning."),
                    new FoodItem(null, "Student Combo Meal", "Combos", 200.0, "combo-meal.jpg", 20, false,
                            "Burger, Fries, and a Coke."),
                    new FoodItem(null, "Coca Cola", "Beverages", 40.0, "coke.jpg", 100, true,
                            "Chilled Coca Cola 300ml."),
                    new FoodItem(null, "Fresh Lemon Juice", "Beverages", 30.0, "lemon-juice.jpg", 50, true,
                            "Freshly squeezed sweet and salt lemon juice."),
                    new FoodItem(null, "Veg Noodles", "Veg", 110.0, "noodles.jpg", 40, true,
                            "Hakka style veg noodles with spring onions."));
            foodItemRepository.saveAll(items);
        }
    }
}
