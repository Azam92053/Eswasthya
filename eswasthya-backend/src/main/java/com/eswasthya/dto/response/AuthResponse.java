package com.eswasthya.dto.response;

import com.eswasthya.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Returned on successful login.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String username;
    private String name;
    private UserRole role;
    private long expiresInMs;
}
