package com.example.phegonbank;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import com.example.phegonbank.auth_users.entity.User;
import com.example.phegonbank.enums.NotificationType;
import com.example.phegonbank.notification.dtos.NotificationDTO;
import com.example.phegonbank.notification.services.NotificationService;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor

public class PhegonbankApplication {
	//private final NotificationService notificationService;

	public static void main(String[] args) {
		SpringApplication.run(PhegonbankApplication.class, args);
	}

	//@Bean
	//CommandLineRunner runner(){
	//	return String[] args -> {
	//		NotificationDTO notificationDTO = NotificationDTO.builder()
	//						.recipient("apptech2020@gmail.com")
	//						.subject("Hello")
	//						.body("Hi Testing")
	//						.type(NotificationType.EMAIL)
	//						.build();
	//		notificationService.sendEmail(notificationDTO, new User());
	//	};
	}

}
