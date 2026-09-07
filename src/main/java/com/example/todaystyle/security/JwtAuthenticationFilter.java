package com.example.todaystyle.security;

import com.example.todaystyle.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 요청의 Authorization 헤더에서 Bearer 토큰을 읽어 유효하면 SecurityContext에
 * 인증 정보를 채운다. principal 로는 사용자 id(Long)를 저장한다.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            JwtTokenProvider.TokenClaims claims = tokenProvider.parseClaims(token);
            if (claims != null && issuedAfterLastPasswordChange(claims)) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        claims.userId(), null, List.of());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 비밀번호 재설정 이전에 발급된 토큰(탈취된 세션 포함)을 무효 처리한다. JWT의 iat는 초 단위
     * 정밀도라 passwordChangedAt도 초 단위로 맞춰 비교해야, 같은 순간에 발급된 토큰이 sub-second
     * 오차로 잘못 걸리지 않는다. 유저가 이미 삭제됐으면(당연히) 무효로 취급한다.
     */
    private boolean issuedAfterLastPasswordChange(JwtTokenProvider.TokenClaims claims) {
        return userRepository.findPasswordChangedAtById(claims.userId())
                .map(passwordChangedAt -> {
                    Date changedAt = Date.from(passwordChangedAt
                            .truncatedTo(ChronoUnit.SECONDS)
                            .atZone(ZoneId.systemDefault())
                            .toInstant());
                    return !claims.issuedAt().before(changedAt);
                })
                .orElse(false);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
