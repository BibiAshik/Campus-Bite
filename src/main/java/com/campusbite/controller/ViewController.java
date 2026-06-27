package com.campusbite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to handle routing for HTML pages.
 * Maps URLs to static HTML files.
 */
@Controller
public class ViewController {

    // --- Admin Portal Routes ---
    
    @GetMapping({"/", "/admin/login"})
    public String adminLogin() {
        return "forward:/admin/login.html";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "forward:/admin/dashboard.html";
    }

    @GetMapping("/admin/menu")
    public String adminMenu() {
        return "forward:/admin/menu.html";
    }

    // --- Student Portal Routes ---
    
    @GetMapping("/student/login")
    public String studentLogin() {
        return "forward:/student/login.html";
    }

    @GetMapping("/student/menu")
    public String studentMenu() {
        return "forward:/student/menu.html";
    }

    @GetMapping("/student/cart")
    public String studentCart() {
        return "forward:/student/cart.html";
    }
    
    @GetMapping("/student/checkout")
    public String studentCheckout() {
        return "forward:/student/checkout.html";
    }

    @GetMapping("/student/my-orders")
    public String studentOrders() {
        return "forward:/student/my-orders.html";
    }

    @GetMapping("/student/my-payments")
    public String studentPayments() {
        return "forward:/student/my-payments.html";
    }

    @GetMapping("/student/favorites")
    public String studentFavorites() {
        return "forward:/student/favorites.html";
    }

    @GetMapping("/student/profile")
    public String studentProfile() {
        return "forward:/student/profile.html";
    }
}
