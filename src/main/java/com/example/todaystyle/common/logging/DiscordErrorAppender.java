package com.example.todaystyle.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ERROR 레벨 로그가 찍히면 Discord 웹훅으로 알림을 보낸다. 지인 베타 규모에서 "누가
 * 말해주기 전엔 서버가 죽어도 모른다"는 문제를 해결하기 위한 최소한의 모니터링.
 *
 * <p>webhookUrl이 비어 있으면(미설정) 아무 것도 하지 않는다 — Cloudinary/Gemini 등
 * 나머지 외부 연동과 동일하게, 설정 안 해도 앱은 정상 동작해야 한다.
 *
 * <p>이 appender 안에서 로깅을 하거나 예외를 던지면 안 된다 — ERROR 로그를 보내다가
 * 실패했다고 또 ERROR 로그를 남기면 무한 재귀로 이어질 수 있어서, 모든 실패는 조용히 삼킨다.
 */
public class DiscordErrorAppender extends AppenderBase<ILoggingEvent> {

    /** 장애 폭주 시 Discord로 초당 수십 건씩 나가는 걸 막는 최소 발송 간격. */
    private static final Duration MIN_INTERVAL = Duration.ofSeconds(30);

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final AtomicReference<Instant> LAST_SENT = new AtomicReference<>(Instant.EPOCH);

    private String webhookUrl;

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        if (!shouldSendNow()) {
            return;
        }
        try {
            send(buildContent(event));
        } catch (RuntimeException ignored) {
            // 알림 발송 실패로 또 로그를 남기면 재귀 위험이 있어 조용히 무시한다.
        }
    }

    private boolean shouldSendNow() {
        Instant now = Instant.now();
        Instant last = LAST_SENT.get();
        if (Duration.between(last, now).compareTo(MIN_INTERVAL) < 0) {
            return false;
        }
        return LAST_SENT.compareAndSet(last, now);
    }

    private String buildContent(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        String throwable = event.getThrowableProxy() == null
                ? "" : "\n```" + truncate(event.getThrowableProxy().getClassName()
                + ": " + event.getThrowableProxy().getMessage(), 300) + "```";
        String content = "🚨 **todaystyle 에러**\n" + truncate(message, 500) + throwable;
        return "{\"content\":" + jsonQuote(content) + "}";
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private String jsonQuote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    private void send(String jsonBody) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();
        // fire-and-forget: 응답을 기다리지 않고, 실패해도 재귀 로깅 없이 조용히 무시한다.
        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .exceptionally(ex -> null);
    }
}
