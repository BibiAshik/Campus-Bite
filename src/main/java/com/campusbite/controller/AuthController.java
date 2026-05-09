package com.campusbite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/status")
    public ResponseEntity<?> checkAuthStatus() {
        // If the user reaches this point, they are authenticated (due to Spring Security rules)
        // Note: For this to work cleanly for the frontend to just check if admin is logged in,
        // we might want to configure Spring Security to allow /api/auth/status and return a different response
        // if not logged in. But since we use formLogin, it might redirect.
        // For simplicity, we just return true.
        return ResponseEntity.ok(Map.of("authenticated", true));
    }
}
