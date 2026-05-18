package com.micultura.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AISearchRequest(
        @NotBlank(message = "La consulta no puede estar vacía")
        @Size(max = 500, message = "La consulta es demasiado larga")
        String query
) {}
