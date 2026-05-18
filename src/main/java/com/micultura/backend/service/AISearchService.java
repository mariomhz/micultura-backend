package com.micultura.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.micultura.backend.dto.AISearchResponse;
import com.micultura.backend.dto.CategoriaResponse;
import com.micultura.backend.dto.EventoResponse;
import com.micultura.backend.entity.Evento;
import com.micultura.backend.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Orchestrates the AI-powered event search:
 *
 *   1. Pulls upcoming events from the database (next ~120 days).
 *   2. Hands Gemini the event catalog plus the user's natural-language query
 *      and asks for a JSON response with matched IDs + a one-line reasoning.
 *   3. Looks the IDs back up locally so the frontend gets full event objects
 *      (with categoria, ubicación, etc.) and not whatever Gemini hallucinates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AISearchService {

    private static final int LOOKAHEAD_DAYS = 120;
    private static final int MAX_RESULTS    = 4;
    private static final Locale ES_LOCALE   = new Locale("es", "ES");

    private final EventoRepository eventoRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public AISearchResponse search(String userQuery) {
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(LOOKAHEAD_DAYS);

        List<Evento> candidates = eventoRepository.findAll().stream()
                .filter(Evento::isActivo)
                .filter(e -> !e.getFecha().isBefore(today))
                .filter(e -> !e.getFecha().isAfter(horizon))
                .sorted((a, b) -> a.getFecha().compareTo(b.getFecha()))
                .toList();

        if (candidates.isEmpty()) {
            return new AISearchResponse(
                    "No hay eventos próximos en el catálogo.",
                    List.of()
            );
        }

        String prompt = buildPrompt(userQuery, candidates, today);
        String json = geminiService.generateJson(prompt);

        ParsedResponse parsed = parse(json);
        Set<Long> wantedIds = new HashSet<>(parsed.eventIds);

        List<EventoResponse> matched = candidates.stream()
                .filter(e -> wantedIds.contains(e.getId()))
                .limit(MAX_RESULTS)
                .map(AISearchService::toResponse)
                .toList();

        return new AISearchResponse(parsed.reasoning, matched);
    }

    private String buildPrompt(String userQuery, List<Evento> events, LocalDate today) {
        StringBuilder sb = new StringBuilder(2048);

        sb.append("Eres un asistente que recomienda eventos culturales en Tenerife. ");
        sb.append("Hoy es ").append(today).append(" (")
          .append(today.getDayOfWeek().getDisplayName(TextStyle.FULL, ES_LOCALE))
          .append("). ");
        sb.append("Devuelve solo eventos del listado de abajo que coincidan con la consulta del usuario. ");
        sb.append("Considera fechas relativas (hoy, mañana, esta semana, este finde, este mes), ");
        sb.append("precio (gratis, barato), categoría, ubicación y temática.\n\n");

        sb.append("EVENTOS DISPONIBLES:\n");
        for (Evento e : events) {
            sb.append("- id=").append(e.getId())
              .append(" | título: ").append(e.getTitulo())
              .append(" | fecha: ").append(e.getFecha())
              .append(" (").append(e.getFecha().getDayOfWeek().getDisplayName(TextStyle.FULL, ES_LOCALE)).append(")")
              .append(" | categoría: ").append(e.getCategoria() != null ? e.getCategoria().getNombre() : "?")
              .append(" | ubicación: ").append(e.getUbicacion())
              .append(" | precio: ").append(formatPrice(e.getPrecio()))
              .append("\n");
        }

        sb.append("\nCONSULTA: \"").append(userQuery.replace("\"", "'")).append("\"\n\n");
        sb.append("Responde EN ESPAÑOL con JSON con esta forma exacta: ");
        sb.append("{\"eventIds\":[<hasta ").append(MAX_RESULTS).append(" ids del listado, en orden de relevancia>],");
        sb.append("\"reasoning\":\"<una frase breve explicando por qué son buena coincidencia>\"}");
        sb.append("\nSi ningún evento encaja, devuelve eventIds vacío y explica brevemente por qué.");

        return sb.toString();
    }

    private static String formatPrice(java.math.BigDecimal precio) {
        if (precio == null || precio.signum() == 0) return "GRATIS";
        return precio + "€";
    }

    private ParsedResponse parse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode idsNode = root.path("eventIds");
            String reasoning = root.path("reasoning").asText("");

            List<Long> ids = idsNode.isArray()
                    ? java.util.stream.StreamSupport
                            .stream(idsNode.spliterator(), false)
                            .filter(JsonNode::isNumber)
                            .map(JsonNode::asLong)
                            .toList()
                    : List.of();

            return new ParsedResponse(ids, reasoning);
        } catch (Exception ex) {
            log.warn("Could not parse Gemini JSON: {}", json, ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "La IA devolvió una respuesta que no se pudo interpretar");
        }
    }

    private record ParsedResponse(List<Long> eventIds, String reasoning) {}

    private static EventoResponse toResponse(Evento e) {
        return new EventoResponse(
                e.getId(),
                e.getTitulo(),
                e.getDescripcion(),
                e.getFecha().toString(),
                e.getHora() != null ? e.getHora().toString() : null,
                e.getUbicacion(),
                e.getLatitud(),
                e.getLongitud(),
                e.getCategoria() != null
                        ? new CategoriaResponse(
                                e.getCategoria().getId(),
                                e.getCategoria().getNombre(),
                                e.getCategoria().getDescripcion(),
                                e.getCategoria().getIcono())
                        : null,
                e.getImagenUrl(),
                e.getPrecio(),
                e.getEnlaceCompra()
        );
    }
}
