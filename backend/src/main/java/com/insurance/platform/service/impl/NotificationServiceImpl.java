package com.insurance.platform.service.impl;

import com.insurance.platform.dto.NotificationResponse;
import com.insurance.platform.entity.Notification;
import com.insurance.platform.entity.NotificationType;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.repository.NotificationRepository;
import com.insurance.platform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void notify(String recipientEmail, String title, String message, NotificationType type, Long relatedEntityId) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }
        Notification notification = Notification.builder()
                .recipientEmail(recipientEmail)
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityId(relatedEntityId)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void notifyOnce(String recipientEmail, String title, String message, NotificationType type, Long relatedEntityId) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }
        boolean exists = notificationRepository
                .findByTypeAndRelatedEntityIdAndRecipientEmail(type, relatedEntityId, recipientEmail)
                .isPresent();
        if (!exists) {
            notify(recipientEmail, title, message, type, relatedEntityId);
        }
    }

    @Override
    public List<NotificationResponse> getForUser(String email) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public long getUnreadCount(String email) {
        return notificationRepository.countByRecipientEmailAndReadFalse(email);
    }

    @Override
    @Transactional
    public void markRead(Long id, String email) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        if (!notification.getRecipientEmail().equalsIgnoreCase(email)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllRead(String email) {
        List<Notification> notifications = notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .relatedEntityId(notification.getRelatedEntityId())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
