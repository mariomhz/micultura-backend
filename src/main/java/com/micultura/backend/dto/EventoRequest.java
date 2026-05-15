package com.micultura.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventoRequest(

        @NotBlank(message = "El título es obligatorio")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,

        LocalTime hora,

        @NotBlank(message = "La ubicación es obligatoria")
        String ubicacion,

        Double latitud,
        Double longitud,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId,

        String imagenUrl,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        BigDecimal precio,

        String enlaceCompra
) {}
