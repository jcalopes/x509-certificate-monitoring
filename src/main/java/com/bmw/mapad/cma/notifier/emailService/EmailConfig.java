package com.bmw.mapad.cma.notifier.emailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configuration file to define the different beans which implement several ways to implement a notification service based in email.
 */
@Configuration
public class EmailConfig {
    @Value("${cma.notifier.email.smtp.host:host}")
    private String host;
    @Value("${cma.notifier.email.smtp.port:587}")
    private int port;
    @Value("${cma.notifier.email.smtp.username:user}")
    private String user;
    @Value("${cma.notifier.email.smtp.password:pass}")
    private String pass;

    /**
     * Return the configuration of the SMTP server hosted by SendGrid platform.
     * Currently using free tier so be carefully to not exceed the 100 daily free emails .
     * @return SendGrid SMTP server.
     */
    @ConditionalOnProperty(prefix = "cma.notifier.email.smtp", name = "provider", havingValue = "sendgrid")
    @Bean
    public JavaMailSender getLocalSMTPServer() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(user);
        mailSender.setPassword(pass);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
