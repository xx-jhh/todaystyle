package com.example.todaystyle.user;

import com.example.todaystyle.security.JwtTokenProvider;
import com.example.todaystyle.user.dto.LoginRequest;
import com.example.todaystyle.user.dto.SignUpRequest;
import com.example.todaystyle.user.dto.TokenResponse;
import com.example.todaystyle.user.dto.UpdateBodyMeasurementsRequest;
import com.example.todaystyle.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
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
        user.setPreferredStyle(request.preferredStyle());

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

    private TokenResponse issueToken(User user) {
        String token = tokenProvider.createToken(user.getId(), user.getEmail());
        return TokenResponse.bearer(token, tokenProvider.getExpirationMs());
    }
}
