package com.micultura.backend.dto;

import java.util.List;

public record AISearchResponse(
        String reasoning,
        List<EventoResponse> events
) {}
