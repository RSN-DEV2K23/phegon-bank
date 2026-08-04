package com.example.phegonbank.notification.services;

import com.example.phegonbank.auth_users.entity.User;
import com.example.phegonbank.notification.dtos.NotificationDTO;

public interface NotificationService {
    void sendEmail(NotificationDTO notificationDTO, User user);

}
