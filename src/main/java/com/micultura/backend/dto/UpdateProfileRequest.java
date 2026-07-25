package com.micultura.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Partial profile update: every field is optional. The service treats null
 * as "leave alone" and only validates fields that the user actually provided.
 *
 * To change the password, both currentPassword and newPassword must be set;
 * the current one is verified before the new one is hashed in.
 */
public record UpdateProfileRequest(
        @Size(min = 1, max = 120, message = "El nombre debe tener entre 1 y 120 caracteres")
        String nombre,

        @Email(message = "Email no válido")
        @Size(max = 255)
        String email,

        String currentPassword,

        @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
        String newPassword
) {}
