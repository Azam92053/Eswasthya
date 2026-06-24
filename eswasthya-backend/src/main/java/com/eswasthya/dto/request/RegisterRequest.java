package com.eswasthya.dto.request;

import com.eswasthya.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Payload for POST /api/auth/register
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age must be realistic")
    private Integer age;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$",
             message = "Gender must be MALE, FEMALE or OTHER")
    private String gender;

    @NotNull(message = "Role is required")
    private UserRole role;
}
