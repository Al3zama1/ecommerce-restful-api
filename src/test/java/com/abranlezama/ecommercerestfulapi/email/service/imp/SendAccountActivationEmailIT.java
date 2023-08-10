package com.abranlezama.ecommercerestfulapi.email.service.imp;

import com.abranlezama.ecommercerestfulapi.ContainersConfig;
import com.abranlezama.ecommercerestfulapi.authentication.event.ActivateAccountEvent;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@DisplayName("send account activation email service")
@Import(ContainersConfig.class)
class SendAccountActivationEmailIT {

    @Autowired
    private SendAccountActivationEmail cut;

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "test"))
            .withPerMethodLifecycle(false);

    @Test
    @DisplayName("receive account activation email")
    void shouldReceiveAccountActivationEmail() {
        // Given
        ActivateAccountEvent event = new ActivateAccountEvent("John", "Last", UUID.randomUUID().toString(), "john.last@gmail.com");

        // When
        cut.sendEmail(event);

        // Then
        await().atMost(2, SECONDS).untilAsserted(() -> {
            MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
            assertThat(receivedMessages.length).isEqualTo(1);

            MimeMessage receivedMessage = receivedMessages[0];
            assertThat(receivedMessage.getAllRecipients().length).isEqualTo(1);
            assertThat(receivedMessage.getAllRecipients()[0].toString()).isEqualTo(event.email());
        });
    }

}
