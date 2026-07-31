package com.example.phegonbank.notification.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String subject;
    @NotBlank(message = "Recipient is required")
    private String recipient;
    private String body;
    private String type;
    private Long userId;
    private LocalDateTime createdAt;
    // For values/variables to be used in the template, if applicable
    private String templateName;
    private Map<String, Object> templateVariables;

}
