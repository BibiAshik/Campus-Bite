package com.campusbite.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for a student profile.
 */
@Data
public class StudentResponseDTO {
    private Long id;
    private String email;
    private String name;
    private String profileImageUrl;
    private String phone;
    private LocalDateTime createdAt;
}
