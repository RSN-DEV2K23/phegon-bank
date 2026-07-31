package com.example.phegonbank.notification.repo;

import com.example.phegonbank.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NotificationRepo extends JpaRepository<Notification, Long> {

}
