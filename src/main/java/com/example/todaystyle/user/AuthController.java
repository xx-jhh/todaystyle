package com.example.todaystyle.user;

import com.example.todaystyle.user.dto.LoginRequest;
import com.example.todaystyle.user.dto.PasswordResetConfirmRequest;
import com.example.todaystyle.user.dto.PasswordResetRequest;
import com.example.todaystyle.user.dto.SignUpRequest;
import com.example.todaystyle.user.dto.TokenResponse;
import com.example.todaystyle.user.dto.UpdateBodyMeasurementsRequest;
import com.example.todaystyle.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/api/auth/signup")
    public ResponseEntity<TokenResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
    }

    @PostMapping("/api/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** 가입된 이메일이면 재설정 링크를 메일로 보낸다. 계정 존재 여부와 무관하게 항상 204. */
    @PostMapping("/api/auth/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/auth/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/users/me")
    public UserResponse me(@AuthenticationPrincipal Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(InvalidCredentialsException::new);
        return UserResponse.from(user);
    }

    /** 마이페이지에서 키/몸무게/허리인치를 수정한다. */
    @PatchMapping("/api/users/me")
    public UserResponse updateMe(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateBodyMeasurementsRequest request
    ) {
        return authService.updateBodyMeasurements(userId, request);
    }
}
