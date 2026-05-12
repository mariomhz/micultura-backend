package com.micultura.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String id;
    private String nombre;
    private String email;
    private String rol;
}
