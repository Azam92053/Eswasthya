package com.eswasthya.dto.response;

import com.eswasthya.entity.User;
import com.eswasthya.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String name;
    private Integer age;
    private String gender;
    private UserRole role;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .age(user.getAge())
                .gender(user.getGender())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
