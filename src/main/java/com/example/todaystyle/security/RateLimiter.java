package com.example.todaystyle.security;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 키(예: IP+경로) 단위 고정 윈도우 방식의 인메모리 rate limiter. 로그인/회원가입
 * 브루트포스 방지용 — 지인 베타 규모라 Redis 같은 외부 저장소 없이 단일 인스턴스
 * 메모리로 충분하다고 판단(다중 인스턴스로 스케일아웃하면 재검토 필요).
 */
@Component
public class RateLimiter {

    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final long WINDOW_MILLIS = Duration.ofMinutes(1).toMillis();

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key) {
        Window window = windows.computeIfAbsent(key, k -> new Window());
        synchronized (window) {
            long now = System.currentTimeMillis();
            if (now - window.startMillis >= WINDOW_MILLIS) {
                window.startMillis = now;
                window.count = 0;
            }
            if (window.count >= MAX_REQUESTS_PER_WINDOW) {
                return false;
            }
            window.count++;
            return true;
        }
    }

    /** 오래 방치된 윈도우를 정리해 장시간 운영 시 메모리가 계속 늘어나는 걸 막는다. */
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void cleanupStaleWindows() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(entry -> now - entry.getValue().startMillis >= WINDOW_MILLIS * 2);
    }

    private static final class Window {
        long startMillis = System.currentTimeMillis();
        int count = 0;
    }
}
