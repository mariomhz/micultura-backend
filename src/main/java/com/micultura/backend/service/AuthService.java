package com.micultura.backend.service;

import com.micultura.backend.dto.AuthResponse;
import com.micultura.backend.dto.LoginRequest;
import com.micultura.backend.dto.RegisterRequest;
import com.micultura.backend.entity.RefreshToken;
import com.micultura.backend.entity.Role;
import com.micultura.backend.entity.User;
import com.micultura.backend.repository.UserRepository;
import com.micultura.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResult register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con este email");
        }

        User user = User.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(Role.USER)
                .build();

        user = userRepository.save(user);
        return issueTokens(user, false);
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        return issueTokens(user, request.isRememberMe());
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        RefreshToken existing = refreshTokenService.findValid(rawRefreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión expirada"));

        User user = userRepository.findById(existing.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión expirada"));

        RefreshTokenService.IssuedToken rotated = refreshTokenService.rotate(existing);
        String accessToken = generateAccessToken(user);

        AuthResponse body = AuthResponse.builder()
                .token(accessToken)
                .id(user.getId().toString())
                .nombre(user.getNombre())
                .email(user.getEmail())
                .rol(user.getRol().name())
                .build();

        return new AuthResult(body, rotated.rawToken(), rotated.ttlMs());
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    private AuthResult issueTokens(User user, boolean rememberMe) {
        String accessToken = generateAccessToken(user);
        RefreshTokenService.IssuedToken refresh = refreshTokenService.issue(user.getId(), rememberMe);

        AuthResponse body = AuthResponse.builder()
                .token(accessToken)
                .id(user.getId().toString())
                .nombre(user.getNombre())
                .email(user.getEmail())
                .rol(user.getRol().name())
                .build();

        return new AuthResult(body, refresh.rawToken(), refresh.ttlMs());
    }

    private String generateAccessToken(User user) {
        return jwtService.generateToken(user.getEmail(), Map.of(
                "id", user.getId().toString(),
                "nombre", user.getNombre(),
                "rol", user.getRol().name()
        ));
    }
}
