package com.micultura.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Thin client around Google Gemini's generateContent endpoint. Sends a prompt,
 * gets back the generated text. We constrain Gemini to respond with JSON via
 * responseMimeType so the caller can deserialize without parsing prose.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private static final String ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String model;

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    public String generateJson(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Búsqueda inteligente no disponible: GEMINI_API_KEY no configurado");
        }

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "temperature", 0.3
                )
        );

        String url = String.format(ENDPOINT_TEMPLATE, model, apiKey);

        try {
            String raw = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            JsonNode text = root
                    .path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text");
            if (text.isMissingNode() || text.isNull()) {
                log.warn("Gemini response missing text: {}", raw);
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Respuesta de IA vacía");
            }
            return text.asText();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Gemini call failed", ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Error al consultar la IA");
        }
    }
}
