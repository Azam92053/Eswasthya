package com.eswasthya.entity;

import com.eswasthya.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an eSwasthya system user.
 *
 * <p>Schema:
 * <pre>
 *   id            BIGINT PK AUTO_INCREMENT
 *   username      VARCHAR(50) UNIQUE NOT NULL
 *   email         VARCHAR(100) UNIQUE NOT NULL
 *   password_hash VARCHAR(255) NOT NULL
 *   name          VARCHAR(100) NOT NULL
 *   age           INT
 *   gender        VARCHAR(10)
 *   role          ENUM(STUDENT, EMPLOYEE, ADMIN)
 *   created_at    TIMESTAMP
 *   updated_at    TIMESTAMP
 * </pre>
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"passwordHash", "healthRecords", "alerts"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender", length = 10)
    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<HealthRecord> healthRecords = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Alert> alerts = new ArrayList<>();
}
