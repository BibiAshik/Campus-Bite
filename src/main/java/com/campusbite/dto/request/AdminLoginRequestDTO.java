package com.campusbite.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Carries admin credentials for login.
 */
@Data
public class AdminLoginRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
