package com.micultura.backend.dto;

public record CategoriaResponse(
        Long id,
        String nombre,
        String descripcion,
        String icono
) {}
