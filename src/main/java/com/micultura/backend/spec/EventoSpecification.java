package com.micultura.backend.spec;

import com.micultura.backend.entity.Evento;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reusable Spring Data JPA Specifications for {@link Evento}.
 * Each method returns {@code null} when the filter value is absent,
 * so Specification.where(…).and(null) is a safe no-op.
 */
public final class EventoSpecification {

    private EventoSpecification() {}

    /** Only return logically active events (not soft-deleted). */
    public static Specification<Evento> activo() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    /** Filter by exact category id. */
    public static Specification<Evento> byCategoria(Long categoriaId) {
        if (categoriaId == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    /** Case-insensitive LIKE search on título and descripción (OR). */
    public static Specification<Evento> searchText(String query) {
        if (query == null || query.isBlank()) return null;
        return (root, q, cb) -> {
            String pattern = "%" + query.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("titulo")),      pattern),
                    cb.like(cb.lower(root.get("descripcion")), pattern)
            );
        };
    }

    /** Events on or after {@code from}. */
    public static Specification<Evento> fechaDesde(LocalDate from) {
        if (from == null) return null;
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("fecha"), from);
    }

    /** Events on or before {@code to}. */
    public static Specification<Evento> fechaHasta(LocalDate to) {
        if (to == null) return null;
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("fecha"), to);
    }

    /** Events with precio >= min. */
    public static Specification<Evento> precioMin(BigDecimal min) {
        if (min == null) return null;
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("precio"), min);
    }

    /** Events with precio <= max. */
    public static Specification<Evento> precioMax(BigDecimal max) {
        if (max == null) return null;
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("precio"), max);
    }
}
