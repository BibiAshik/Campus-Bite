package com.campusbite.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request to update student's phone number.
 */
@Data
public class StudentPhoneUpdateRequestDTO {
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;
}
