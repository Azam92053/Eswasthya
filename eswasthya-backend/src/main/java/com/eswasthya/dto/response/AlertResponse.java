package com.eswasthya.dto.response;

import com.eswasthya.entity.Alert;
import com.eswasthya.enums.AlertType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlertResponse {

    private Long id;
    private Long userId;
    private Long recordId;
    private AlertType alertType;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static AlertResponse from(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .userId(alert.getUser().getId())
                .recordId(alert.getRecord().getId())
                .alertType(alert.getAlertType())
                .message(alert.getMessage())
                .isRead(alert.getIsRead())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
