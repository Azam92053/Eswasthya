package com.eswasthya.repository;

import com.eswasthya.entity.Alert;
import com.eswasthya.entity.HealthRecord;
import com.eswasthya.entity.User;
import com.eswasthya.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /** All alerts for a user, newest first. */
    List<Alert> findByUserOrderByCreatedAtDesc(User user);

    /** Unread alerts for a user. */
    List<Alert> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    /** Count all unread alerts platform-wide — admin use. */
    long countByIsReadFalse();

    /** Count unread alerts for a user. */
    long countByUserAndIsReadFalse(User user);

    /** All alerts linked to a specific health record. */
    List<Alert> findByRecord(HealthRecord record);

    /** Mark all alerts for a user as read. */
    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.user = :user")
    void markAllAsReadForUser(User user);

    /** Alerts by type across all users — admin use. */
    List<Alert> findByAlertType(AlertType alertType);

    /** Count of alerts grouped by type — admin stats. */
    @Query("SELECT a.alertType, COUNT(a) FROM Alert a GROUP BY a.alertType")
    List<Object[]> countByAlertType();
}
