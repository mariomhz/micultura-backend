package com.micultura.backend.dto;

import java.math.BigDecimal;

public record EventoResponse(
        Long id,
        String titulo,
        String descripcion,
        String fecha,          // ISO-8601: "2026-06-15"
        String hora,           // "HH:mm" or null
        String ubicacion,
        Double latitud,
        Double longitud,
        CategoriaResponse categoria,
        String imagenUrl,
        BigDecimal precio,
        String enlaceCompra
) {}
