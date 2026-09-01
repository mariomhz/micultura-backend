package com.micultura.backend.repository;

import com.micultura.backend.entity.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EventoRepository
        extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {

    /**
     * Eager-load the categoria association in a single join to avoid the N+1
     * select that would otherwise fire once Evento.categoria is LAZY.
     */
    @Override
    @EntityGraph(attributePaths = "categoria")
    Page<Evento> findAll(Specification<Evento> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "categoria")
    Optional<Evento> findById(Long id);

    /** How many events have not happened yet, used to decide whether to reseed. */
    long countByFechaGreaterThanEqual(LocalDate fecha);
}
