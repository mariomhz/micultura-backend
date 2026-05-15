package com.micultura.backend.repository;

import com.micultura.backend.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoRepository
        extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {
}
