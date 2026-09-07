package com.example.todaystyle.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 액세스 토큰 발급/검증. 서명 방식은 HS256이며 subject에 사용자 id를,
 * 클레임에 email을 담는다.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public String createToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** 사용자 id와 발급시각. 발급시각은 비밀번호 재설정 이후 발급된 토큰인지 판단하는 데 쓰인다. */
    public record TokenClaims(Long userId, Date issuedAt) {
    }

    /**
     * 토큰이 유효하면(서명/만료) claims를 반환하고, 위조/만료 등으로 유효하지 않으면 null을 반환한다.
     */
    public TokenClaims parseClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new TokenClaims(Long.valueOf(claims.getSubject()), claims.getIssuedAt());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
