package com.eswasthya.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Payload for PATCH /api/users/profile
 * All fields optional — only non-null fields are applied.
 */
@Data
public class UpdateUserRequest {

    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Min(value = 1) @Max(value = 120)
    private Integer age;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE or OTHER")
    private String gender;

    @Email(message = "Email must be valid")
    private String email;
}
