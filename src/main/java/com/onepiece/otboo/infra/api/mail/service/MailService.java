package com.onepiece.otboo.infra.api.mail.service;

import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;

    private static final String RESET_PASSWORD_TEMPLATE_PATH =
        "templates/reset-password-email.html";

    @Value("${spring.mail.sender-email}")
    private String senderEmail;

    @Async("mailTaskExecutor")
    public CompletableFuture<Boolean> sendTemporaryPasswordEmail(String email,
        String temporaryPassword, Instant expirationTime) {
        try {
            String html = loadTemplateHtml();

            String formattedExpiration = LocalDateTime.ofInstant(expirationTime,
                    ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            html = html.replace("{{temporaryPassword}}", temporaryPassword)
                .replace("{{expirationTime}}", formattedExpiration);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail, "OTBOO");
            helper.setTo(email);
            helper.setSubject("임시 비밀번호 발급 - OTBOO");
            helper.setText(html, true);
            mailSender.send(mimeMessage);

            log.info("임시 비밀번호 이메일 발송 성공 - 수신자: {}", email);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("임시 비밀번호 이메일 발송 실패 - 수신자: {}", email, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    private String loadTemplateHtml() throws IOException {
        try {
            return loadTemplateWithClassPathResource(RESET_PASSWORD_TEMPLATE_PATH);
        } catch (IOException exception) {
            log.debug("ClassPathResource로 템플릿 로드에 실패했습니다. ResourceLoader 방식으로 재시도합니다.",
                exception);
            return loadTemplateWithResourceLoader(RESET_PASSWORD_TEMPLATE_PATH);
        }
    }

    private String loadTemplateWithClassPathResource(String location) throws IOException {
        ClassPathResource resource = new ClassPathResource(location);
        return readResource(resource);
    }

    private String loadTemplateWithResourceLoader(String location) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + location);
        return readResource(resource);
    }

    private String readResource(Resource resource) throws IOException {
        if (!resource.exists()) {
            throw new IOException("Resource not found: " + resource.getDescription());
        }

        try (Reader reader = new InputStreamReader(resource.getInputStream(),
            StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
