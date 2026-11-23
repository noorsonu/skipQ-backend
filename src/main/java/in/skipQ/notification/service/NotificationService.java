package in.skipQ.notification.service;

import in.skipQ.notification.dto.NotificationResponseDto;
import in.skipQ.notification.dto.RegisterDeviceRequest;
import in.skipQ.notification.model.NotificationTemplate;
import in.skipQ.queue.model.Token;
import in.skipQ.user.model.User;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void registerDevice(RegisterDeviceRequest request, User currentUser);

    List<NotificationResponseDto> getMyNotifications(User currentUser);

    long getUnreadCount(User currentUser);

    NotificationResponseDto markAsRead(UUID notificationId, User currentUser);

    void markAllAsRead(User currentUser);

    void sendQueueNotification(Token token, NotificationTemplate template, String customNote);
}

