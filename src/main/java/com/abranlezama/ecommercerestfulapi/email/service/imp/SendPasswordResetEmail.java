package com.abranlezama.ecommercerestfulapi.email.service.imp;

import com.abranlezama.ecommercerestfulapi.authentication.event.ResetPasswordEvent;
import com.abranlezama.ecommercerestfulapi.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class SendPasswordResetEmail implements EmailService<ResetPasswordEvent> {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${custom.password-reset.url}")
    private String passwordResetUrl;

    @Override
    @EventListener
    public void sendEmail(ResetPasswordEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("project@abranlezama.com");
        message.setTo(event.email());
        message.setSubject("E-commerce Store, Password Reset");
        message.setText(generateEmailContent(event));
        javaMailSender.send(message);
    }

    private String generateEmailContent(ResetPasswordEvent event) {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariable("link", passwordResetUrl + "?token=" + event.token());

        return templateEngine.process("password-reset", thymeleafContext);
    }
}
