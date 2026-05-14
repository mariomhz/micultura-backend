package com.micultura.backend.dto;

import com.micultura.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class UserResponse {

    private String id;
    private String nombre;
    private String email;
    private String rol;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId().toString())
                .nombre(user.getNombre())
                .email(user.getEmail())
                .rol(user.getRol().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
