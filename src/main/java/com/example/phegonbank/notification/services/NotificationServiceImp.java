package com.example.phegonbank.notification.services;

import java.nio.charset.StandardCharsets;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.phegonbank.auth_users.entity.User;
import com.example.phegonbank.enums.NotificationType;
import com.example.phegonbank.notification.dtos.NotificationDTO;
import com.example.phegonbank.notification.entity.Notification;
import com.example.phegonbank.notification.repo.NotificationRepo;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor

public class NotificationServiceImp implements NotificationService{

    private final NotificationRepo notificationRepo;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void sendEmail(NotificationDTO notificationDTO, User user){

        try {
            MimeMessage mimeMessage=mailSender.createMimeMessage();

            MimeMessageHelper helper=new MimeMessageHelper(mimeMessage,MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,StandardCharsets.UTF_8.name());

            helper.setTo(notificationDTO.getRecipient());
            helper.setSubject(notificationDTO.getSubject());

            // Use Template if provided
            if(notificationDTO.getTemplateName() !=null){
                Context context = new Context();
                context.setVariable(notificationDTO.getTemplateVariables());
                String htmlContent = templateEngine.process(notificationDTO.getTemplateName(), context);
                helper.setText(notificationDTO.getBody(), html:true);
            }else{

                //if no template send text body directly
                helper.setText(notificationDTO.getBody(),html:true);
            }
            
            mailSender.send(mimeMessage);

            //Save to our database table
            Notification notificationToSave = Notification.builder()
                    .recipient(notificationDTO.getRecipient())
                    .Subject(notificationDTO.getSubject())
                    .body(notificationDTO.getBody())
                    .type(NotificationType.EMAIL())
                    .user(user)
                    .build()
            notificationRepo.save(notificationToSave)

        } catch (MessagingException e) {
            log.error(e.getMessage());
            // TODO: handle exception
        }

    }

}
