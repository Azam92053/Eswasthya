package com.eswasthya.entity;

import com.eswasthya.enums.ActivityLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores one health-metric entry for a user on a given date.
 *
 * <p>Schema:
 * <pre>
 *   id              BIGINT PK AUTO_INCREMENT
 *   user_id         BIGINT FK → users.id
 *   record_date     DATE  NOT NULL
 *   bmi             DECIMAL(5,2)
 *   systolic_bp     INT
 *   diastolic_bp    INT
 *   glucose         DECIMAL(6,2)   — fasting blood glucose mg/dL
 *   activity_level  ENUM(LOW, MODERATE, HIGH)
 *   notes           TEXT
 *   created_at      TIMESTAMP
 *   updated_at      TIMESTAMP
 * </pre>
 */
@Entity
@Table(name = "health_records", indexes = {
        @Index(name = "idx_hr_user_date", columnList = "user_id, record_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "alerts"})
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK to User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    // Metrics

    /**
     * Body Mass Index — weight(kg) / height(m)².
     * Stored as-is (calculated by client or provided by user).
     */
    @Column(name = "bmi", columnDefinition = "DECIMAL(5,2)")
    private Double bmi;

    /** Systolic blood pressure in mmHg (upper number). */
    @Column(name = "systolic_bp")
    private Integer systolicBp;

    /** Diastolic blood pressure in mmHg (lower number). */
    @Column(name = "diastolic_bp")
    private Integer diastolicBp;

    /** Fasting blood glucose in mg/dL. */
    @Column(name = "glucose", columnDefinition = "DECIMAL(6,2)")
    private Double glucose;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", length = 20)
    private ActivityLevel activityLevel;

    /** Optional free-text notes from the user. */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Audit

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Alert> alerts = new ArrayList<>();

    // Helper

    /** Returns blood pressure as "systolic/diastolic" string, or null if not recorded. */
    @Transient
    public String getBloodPressureString() {
        if (systolicBp == null || diastolicBp == null) return null;
        return systolicBp + "/" + diastolicBp;
    }
}
