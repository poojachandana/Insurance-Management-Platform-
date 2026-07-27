package com.insurance.platform.repository;

import com.insurance.platform.entity.Notification;
import com.insurance.platform.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);
    long countByRecipientEmailAndReadFalse(String recipientEmail);
    Optional<Notification> findByTypeAndRelatedEntityIdAndRecipientEmail(NotificationType type, Long relatedEntityId, String recipientEmail);
}
