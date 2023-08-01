package com.abranlezama.ecommercerestfulapi.email.service.imp;

import com.abranlezama.ecommercerestfulapi.authentication.event.ActivateAccountEvent;
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
public class SendAccountActivationEmail implements EmailService<ActivateAccountEvent> {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${custom.account-activation.url}")
    private String accountActivationUrl;


    @Override
    @EventListener
    public void sendEmail(ActivateAccountEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("project@abranlezama.com");
        message.setTo(event.email());
        message.setSubject("E-commerce Store Account Activation");
        message.setText(generateEmailContent(event));
        javaMailSender.send(message);
    }

    private String generateEmailContent(ActivateAccountEvent event) {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariable("firstName", event.firstName());
        thymeleafContext.setVariable("lastName", event.lastName());
        thymeleafContext.setVariable("link", accountActivationUrl + "?token=" + event.token());

        return templateEngine.process("user-account-activation-email", thymeleafContext);
    }
}
