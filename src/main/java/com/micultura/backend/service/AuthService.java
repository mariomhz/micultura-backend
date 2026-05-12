package com.micultura.backend.service;

import com.micultura.backend.dto.AuthResponse;
import com.micultura.backend.dto.LoginRequest;
import com.micultura.backend.dto.RegisterRequest;
import com.micultura.backend.entity.Role;
import com.micultura.backend.entity.User;
import com.micultura.backend.repository.UserRepository;
import com.micultura.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
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

        String token = jwtService.generateToken(user.getEmail(), Map.of(
                "id", user.getId().toString(),
                "nombre", user.getNombre(),
                "rol", user.getRol().name()
        ));

        return AuthResponse.builder()
                .token(token)
                .id(user.getId().toString())
                .nombre(user.getNombre())
                .email(user.getEmail())
                .rol(user.getRol().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = jwtService.generateToken(user.getEmail(), Map.of(
                "id", user.getId().toString(),
                "nombre", user.getNombre(),
                "rol", user.getRol().name()
        ));

        return AuthResponse.builder()
                .token(token)
                .id(user.getId().toString())
                .nombre(user.getNombre())
                .email(user.getEmail())
                .rol(user.getRol().name())
                .build();
    }
}
