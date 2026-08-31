package com.example.todaystyle.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 비밀번호 재설정 메일 발송. SMTP 실패는 계정 존재 여부를 응답으로 노출하지 않기 위해
 * 여기서 삼키고 로그만 남긴다 — 호출부(AuthService)는 항상 같은 응답을 내려준다.
 */
@Component
public class PasswordResetMailSender {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetMailSender.class);

    private final JavaMailSender mailSender;
    private final String appBaseUrl;

    public PasswordResetMailSender(
            JavaMailSender mailSender,
            @Value("${app.base-url}") String appBaseUrl
    ) {
        this.mailSender = mailSender;
        this.appBaseUrl = appBaseUrl;
    }

    public void send(String toEmail, String token) {
        String link = appBaseUrl + "/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[todaystyle] 비밀번호 재설정");
        message.setText(
                "todaystyle 비밀번호 재설정을 요청하셨습니다.\n\n"
                        + "아래 링크에서 새 비밀번호를 설정해주세요 (30분간 유효):\n"
                        + link
                        + "\n\n본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다."
        );
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("비밀번호 재설정 메일 발송 실패: {}", toEmail, e);
        }
    }
}
