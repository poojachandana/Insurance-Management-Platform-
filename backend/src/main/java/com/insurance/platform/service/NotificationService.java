package com.insurance.platform.service;

import com.insurance.platform.dto.NotificationResponse;
import com.insurance.platform.entity.NotificationType;

import java.util.List;

public interface NotificationService {
    void notify(String recipientEmail, String title, String message, NotificationType type, Long relatedEntityId);
    /** Creates the notification only if one of the same type/entity/recipient doesn't already exist (avoids duplicate spam). */
    void notifyOnce(String recipientEmail, String title, String message, NotificationType type, Long relatedEntityId);
    List<NotificationResponse> getForUser(String email);
    long getUnreadCount(String email);
    void markRead(Long id, String email);
    void markAllRead(String email);
}
