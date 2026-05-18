package com.micultura.backend.service;

import com.micultura.backend.dto.UpdateProfileRequest;
import com.micultura.backend.dto.UserResponse;
import com.micultura.backend.entity.User;
import com.micultura.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse findByEmail(String email) {
        return UserResponse.from(userOrUnauthorized(email));
    }

    @Transactional
    public UserResponse updateProfile(String currentEmail, UpdateProfileRequest req) {
        User user = userOrUnauthorized(currentEmail);

        if (req.nombre() != null && !req.nombre().isBlank()) {
            user.setNombre(req.nombre().trim());
        }

        if (req.email() != null && !req.email().isBlank()
                && !req.email().equalsIgnoreCase(user.getEmail())) {
            String newEmail = req.email().trim().toLowerCase();
            if (userRepository.existsByEmail(newEmail)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Ya existe una cuenta con este email");
            }
            user.setEmail(newEmail);
        }

        if (req.newPassword() != null && !req.newPassword().isBlank()) {
            if (req.currentPassword() == null || req.currentPassword().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Debes indicar tu contraseña actual");
            }
            if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Contraseña actual incorrecta");
            }
            user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    private User userOrUnauthorized(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }
}
