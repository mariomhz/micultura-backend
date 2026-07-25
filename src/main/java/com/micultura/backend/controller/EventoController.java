package com.micultura.backend.controller;

import com.micultura.backend.dto.EventoRequest;
import com.micultura.backend.dto.EventoResponse;
import com.micultura.backend.dto.PagedResponse;
import com.micultura.backend.service.EventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    /** Fields the client is allowed to sort by. Anything else falls back to fecha,asc. */
    private static final Set<String> SORTABLE_FIELDS = Set.of("fecha", "precio", "titulo");
    private static final String DEFAULT_SORT_FIELD = "fecha";


    /**
     * GET /api/events
     *
     * Query params:
     *   category  : categoria id
     *   search    : text search in titulo/descripcion
     *   fechaDesde: ISO date lower bound
     *   fechaHasta: ISO date upper bound
     *   precioMin : minimum price (inclusive)
     *   precioMax : maximum price (inclusive)
     *   page      : 0-indexed page number (default 0)
     *   size      : page size (default 20, max 100)
     *   sort      : field,direction e.g. "fecha,asc" (default "fecha,asc")
     */
    @GetMapping
    public ResponseEntity<PagedResponse<EventoResponse>> getAll(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha,asc") String sort
    ) {
        size = Math.min(size, 100);                // cap page size
        Pageable pageable = buildPageable(page, size, sort);

        return ResponseEntity.ok(eventoService.findAll(
                category, search, fechaDesde, fechaHasta, precioMin, precioMax, pageable));
    }

    /** GET /api/events/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<EventoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.findById(id));
    }


    /** POST /api/events: requires ADMIN role */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventoResponse> create(@Valid @RequestBody EventoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventoService.create(request));
    }

    /** PUT /api/events/{id}: requires ADMIN role */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventoResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EventoRequest request
    ) {
        return ResponseEntity.ok(eventoService.update(id, request));
    }

    /** DELETE /api/events/{id}: soft delete, requires ADMIN role */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventoService.delete(id);
        return ResponseEntity.noContent().build();
    }


    private Pageable buildPageable(int page, int size, String sort) {
        String[] parts = sort == null ? new String[0] : sort.split(",");
        String requestedField = parts.length > 0 ? parts[0].trim() : "";
        String field = SORTABLE_FIELDS.contains(requestedField) ? requestedField : DEFAULT_SORT_FIELD;
        Sort.Direction dir = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(dir, field));
    }
}
