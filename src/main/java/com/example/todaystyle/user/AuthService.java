package com.example.todaystyle.user;

import com.example.todaystyle.security.JwtTokenProvider;
import com.example.todaystyle.user.dto.LoginRequest;
import com.example.todaystyle.user.dto.PasswordResetConfirmRequest;
import com.example.todaystyle.user.dto.PasswordResetRequest;
import com.example.todaystyle.user.dto.SignUpRequest;
import com.example.todaystyle.user.dto.TokenResponse;
import com.example.todaystyle.user.dto.UpdateBodyMeasurementsRequest;
import com.example.todaystyle.user.dto.UserResponse;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    /** 재설정 링크 유효 시간. */
    private static final long RESET_TOKEN_TTL_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordResetMailSender resetMailSender;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            PasswordResetTokenRepository resetTokenRepository,
            PasswordResetMailSender resetMailSender
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.resetTokenRepository = resetTokenRepository;
        this.resetMailSender = resetMailSender;
    }

    @Transactional
    public TokenResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setHeight(request.height());
        user.setWeight(request.weight());
        user.setWaistInch(request.waistInch());
        user.setBodyType(request.bodyType());
        user.setPreferredStyles(request.preferredStyles() == null
                ? new HashSet<>() : new HashSet<>(request.preferredStyles()));

        User saved = userRepository.save(user);
        return issueToken(saved);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);
        return issueToken(user);
    }

    /** 마이페이지에서 신체 치수를 수정한다. 필드를 비우면(null) 해당 값이 지워진다. */
    @Transactional
    public UserResponse updateBodyMeasurements(Long userId, UpdateBodyMeasurementsRequest request) {
        User user = userRepository.findById(userId).orElseThrow(InvalidCredentialsException::new);
        user.setHeight(request.height());
        user.setWeight(request.weight());
        user.setWaistInch(request.waistInch());
        return UserResponse.from(user);
    }

    /**
     * 가입된 이메일이면 재설정 토큰을 발급해 메일로 보낸다. 계정 존재 여부가 응답으로
     * 드러나지 않도록, 이메일이 없어도 예외 없이 조용히 끝낸다.
     */
    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            resetTokenRepository.deleteByUserId(user.getId());
            String token = UUID.randomUUID().toString();
            resetTokenRepository.save(new PasswordResetToken(
                    user.getId(), token, LocalDateTime.now().plusMinutes(RESET_TOKEN_TTL_MINUTES)));
            resetMailSender.send(user.getEmail(), token);
        });
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.token())
                .filter(PasswordResetToken::isValid)
                .orElseThrow(InvalidResetTokenException::new);
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(InvalidResetTokenException::new);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        resetToken.setUsed(true);
    }

    private TokenResponse issueToken(User user) {
        String token = tokenProvider.createToken(user.getId(), user.getEmail());
        return TokenResponse.bearer(token, tokenProvider.getExpirationMs());
    }
}
