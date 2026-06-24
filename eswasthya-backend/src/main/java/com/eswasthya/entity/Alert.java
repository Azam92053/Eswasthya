package com.eswasthya.entity;

import com.eswasthya.enums.AlertType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A health alert automatically generated when a health-record metric
 * falls outside the clinically safe range.
 *
 * <p>Schema:
 * <pre>
 *   id          BIGINT PK AUTO_INCREMENT
 *   user_id     BIGINT FK → users.id
 *   record_id   BIGINT FK → health_records.id
 *   alert_type  ENUM
 *   message     VARCHAR(500) NOT NULL
 *   is_read     BOOLEAN DEFAULT FALSE
 *   created_at  TIMESTAMP
 * </pre>
 */
@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alerts_user_id", columnList = "user_id"),
        @Index(name = "idx_alerts_record_id", columnList = "record_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "record"})
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private HealthRecord record;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 30)
    private AlertType alertType;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    /**
     * Whether the user has acknowledged/seen this alert.
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
