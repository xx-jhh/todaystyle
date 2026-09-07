package com.example.todaystyle.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.todaystyle.security.JwtTokenProvider;
import com.example.todaystyle.user.dto.LoginRequest;
import com.example.todaystyle.user.dto.PasswordResetConfirmRequest;
import com.example.todaystyle.user.dto.SignUpRequest;
import com.example.todaystyle.user.dto.TokenResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private PasswordResetTokenRepository resetTokenRepository;
    @Mock
    private PasswordResetMailSender resetMailSender;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, passwordEncoder, tokenProvider, resetTokenRepository, resetMailSender);
    }

    @Test
    void 이미_가입된_이메일이면_회원가입시_예외를_던진다() {
        SignUpRequest request = new SignUpRequest(
                "existing@todaystyle.com", "password123", "닉네임",
                null, null, null, null, null);
        when(userRepository.existsByEmail("existing@todaystyle.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void 회원가입_성공시_비밀번호를_암호화해_저장하고_토큰을_발급한다() {
        SignUpRequest request = new SignUpRequest(
                "new@todaystyle.com", "password123", "닉네임",
                null, null, null, BodyType.STRAIGHT, Set.of(StyleCategory.CASUAL));
        when(userRepository.existsByEmail("new@todaystyle.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(tokenProvider.createToken(1L, "new@todaystyle.com")).thenReturn("jwt-token");
        when(tokenProvider.getExpirationMs()).thenReturn(86_400_000L);

        TokenResponse response = authService.signUp(request);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getValue().getEmail()).isEqualTo("new@todaystyle.com");
        assertThat(savedUser.getValue().getPasswordChangedAt()).isNotNull();
        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void 존재하지_않는_이메일로_로그인하면_예외를_던진다() {
        LoginRequest request = new LoginRequest("nobody@todaystyle.com", "password123");
        when(userRepository.findByEmail("nobody@todaystyle.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void 비밀번호가_일치하지_않으면_로그인시_예외를_던진다() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@todaystyle.com");
        user.setPassword("encoded-password");
        LoginRequest request = new LoginRequest("user@todaystyle.com", "wrong-password");
        when(userRepository.findByEmail("user@todaystyle.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void 이메일과_비밀번호가_일치하면_로그인시_토큰을_발급한다() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@todaystyle.com");
        user.setPassword("encoded-password");
        LoginRequest request = new LoginRequest("user@todaystyle.com", "correct-password");
        when(userRepository.findByEmail("user@todaystyle.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "encoded-password")).thenReturn(true);
        when(tokenProvider.createToken(1L, "user@todaystyle.com")).thenReturn("jwt-token");
        when(tokenProvider.getExpirationMs()).thenReturn(86_400_000L);

        TokenResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        verify(tokenProvider).createToken(1L, "user@todaystyle.com");
    }

    @Test
    void 비밀번호_재설정에_성공하면_passwordChangedAt이_갱신된다() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@todaystyle.com");
        LocalDateTime beforeReset = LocalDateTime.now().minusDays(1);
        user.setPasswordChangedAt(beforeReset);

        PasswordResetToken resetToken = new PasswordResetToken(1L, "reset-token", LocalDateTime.now().plusMinutes(30));
        when(resetTokenRepository.findByToken("reset-token")).thenReturn(Optional.of(resetToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encoded-new-password");

        authService.confirmPasswordReset(new PasswordResetConfirmRequest("reset-token", "newPassword123"));

        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
        assertThat(user.getPasswordChangedAt()).isAfter(beforeReset);
        assertThat(resetToken.isUsed()).isTrue();
    }
}
