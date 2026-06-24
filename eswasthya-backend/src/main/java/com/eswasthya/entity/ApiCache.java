package com.eswasthya.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Cached response body for external health reference requests. */
@Entity
@Table(name = "api_cache", indexes = {
        @Index(name = "idx_api_cache_endpoint", columnList = "endpoint")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "endpoint", nullable = false, unique = true, length = 512)
    private String endpoint;

    @Column(name = "response_data", nullable = false, columnDefinition = "LONGTEXT")
    private String responseData;

    @CreationTimestamp
    @Column(name = "fetched_at", nullable = false, updatable = false)
    private LocalDateTime fetchedAt;

    /** Returns true when the cached response has exceeded the configured TTL. */
    public boolean isExpired(int ttlHours) {
        return fetchedAt.plusHours(ttlHours).isBefore(LocalDateTime.now());
    }
}
