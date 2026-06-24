package com.eswasthya.service;

import com.eswasthya.dto.response.AdminStatsResponse;
import com.eswasthya.dto.response.HealthRecordResponse;
import com.eswasthya.entity.User;
import com.eswasthya.enums.UserRole;
import com.eswasthya.repository.AlertRepository;
import com.eswasthya.repository.HealthRecordRepository;
import com.eswasthya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Aggregates platform statistics for admin views. */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final AlertRepository alertRepository;

    // Platform-wide statistics

    @Transactional(readOnly = true)
    public AdminStatsResponse getPlatformStats() {

        // User counts by role
        Map<String, Long> usersByRole = new HashMap<>();
        for (Object[] row : userRepository.countByRole()) {
            usersByRole.put(((UserRole) row[0]).name(), (Long) row[1]);
        }

        // Activity level distribution
        Map<String, Long> activityDist = new HashMap<>();
        for (Object[] row : healthRecordRepository.countByActivityLevel()) {
            activityDist.put(row[0] != null ? row[0].toString() : "NOT_SET", (Long) row[1]);
        }

        // Alert counts by type
        Map<String, Long> alertsByType = new HashMap<>();
        for (Object[] row : alertRepository.countByAlertType()) {
            alertsByType.put(row[0].toString(), (Long) row[1]);
        }

        long totalAlerts  = alertRepository.count();
        long totalUnread  = alertRepository.countByIsReadFalse();

        // Per-user summaries
        List<User> allUsers = userRepository.findAll();
        List<AdminStatsResponse.UserHealthSummary> summaries = allUsers.stream()
                .map(u -> buildUserSummary(u))
                .collect(Collectors.toList());

        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .usersByRole(usersByRole)
                .totalHealthRecords(healthRecordRepository.count())
                .averageBmi(round(healthRecordRepository.findAverageBmi()))
                .averageGlucose(round(healthRecordRepository.findAverageGlucose()))
                .averageSystolicBp(round(healthRecordRepository.findAverageSystolicBp()))
                .recordsByActivityLevel(activityDist)
                .totalAlerts(totalAlerts)
                .unreadAlerts(totalUnread)
                .alertsByType(alertsByType)
                .userSummaries(summaries)
                .build();
    }

    // All health records (admin view)

    @Transactional(readOnly = true)
    public List<HealthRecordResponse> getAllHealthRecords() {
        return healthRecordRepository.findAllWithUsers()
                .stream()
                .map(HealthRecordResponse::from)
                .collect(Collectors.toList());
    }

    // Internal

    private AdminStatsResponse.UserHealthSummary buildUserSummary(User user) {
        long recordCount = healthRecordRepository.countByUser(user);
        long unreadAlerts = alertRepository.countByUserAndIsReadFalse(user);

        Double latestBmi = healthRecordRepository
                .findTopByUserOrderByRecordDateDesc(user)
                .map(r -> r.getBmi())
                .orElse(null);

        return AdminStatsResponse.UserHealthSummary.builder()
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .recordCount(recordCount)
                .latestBmi(latestBmi)
                .latestBmiCategory(bmiCategory(latestBmi))
                .unreadAlertCount(unreadAlerts)
                .build();
    }

    private Double round(Double val) {
        if (val == null) return null;
        return Math.round(val * 100.0) / 100.0;
    }

    private String bmiCategory(Double bmi) {
        if (bmi == null) return null;
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }
}
