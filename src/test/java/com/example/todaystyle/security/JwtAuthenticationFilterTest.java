package com.example.todaystyle.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.todaystyle.user.UserRepository;
import jakarta.servlet.FilterChain;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(tokenProvider, userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 비밀번호_재설정_이전에_발급된_토큰은_인증되지_않는다() throws Exception {
        LocalDateTime passwordChangedAt = LocalDateTime.now();
        Date issuedBeforeReset = toDate(passwordChangedAt.minusMinutes(5));
        when(tokenProvider.parseClaims("stale-token"))
                .thenReturn(new JwtTokenProvider.TokenClaims(1L, issuedBeforeReset));
        when(userRepository.findPasswordChangedAtById(1L)).thenReturn(Optional.of(passwordChangedAt));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer stale-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void 비밀번호_재설정_이후에_발급된_토큰은_정상_인증된다() throws Exception {
        LocalDateTime passwordChangedAt = LocalDateTime.now().minusMinutes(5);
        Date issuedAfterReset = toDate(LocalDateTime.now());
        when(tokenProvider.parseClaims("fresh-token"))
                .thenReturn(new JwtTokenProvider.TokenClaims(1L, issuedAfterReset));
        when(userRepository.findPasswordChangedAtById(1L)).thenReturn(Optional.of(passwordChangedAt));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer fresh-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(1L);
    }

    @Test
    void 이미_탈퇴한_유저의_토큰은_인증되지_않는다() throws Exception {
        when(tokenProvider.parseClaims("orphan-token"))
                .thenReturn(new JwtTokenProvider.TokenClaims(99L, new Date()));
        when(userRepository.findPasswordChangedAtById(99L)).thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer orphan-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private static Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
