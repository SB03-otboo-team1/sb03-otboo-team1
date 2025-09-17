package com.onepiece.otboo.domain.auth.service;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${mail.sender-email}")
    private String senderEmail;

    public boolean sendTemporaryPasswordEmail(String email, String temporaryPassword,
        LocalDateTime expirationTime) {
        try {
            Path templatePath = Path.of("src/main/resources/templates/reset-password-email.html");
            String html = Files.readString(templatePath, StandardCharsets.UTF_8);

            String formattedExpiration = expirationTime.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            html = html.replace("{{temporaryPassword}}", temporaryPassword)
                .replace("{{expirationTime}}", formattedExpiration);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail, "OTBOO");
            helper.setTo(email);
            helper.setSubject("임시 비밀번호 발급 - OTBOO");
            helper.setText(html, true);
            mailSender.send(mimeMessage);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
