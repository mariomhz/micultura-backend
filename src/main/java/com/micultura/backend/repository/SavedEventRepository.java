package com.micultura.backend.repository;

import com.micultura.backend.entity.Evento;
import com.micultura.backend.entity.SavedEvent;
import com.micultura.backend.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedEventRepository extends JpaRepository<SavedEvent, UUID> {

    /** Eager-loads evento + its categoria so the controller can map without N+1. */
    @EntityGraph(attributePaths = {"evento", "evento.categoria"})
    List<SavedEvent> findByUserOrderBySavedAtDesc(User user);

    Optional<SavedEvent> findByUserAndEvento(User user, Evento evento);

    boolean existsByUserAndEvento(User user, Evento evento);

    long deleteByUserAndEvento(User user, Evento evento);
}
