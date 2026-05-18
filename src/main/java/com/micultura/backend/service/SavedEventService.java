package com.micultura.backend.service;

import com.micultura.backend.dto.CategoriaResponse;
import com.micultura.backend.dto.EventoResponse;
import com.micultura.backend.entity.Evento;
import com.micultura.backend.entity.SavedEvent;
import com.micultura.backend.entity.User;
import com.micultura.backend.exception.ResourceNotFoundException;
import com.micultura.backend.repository.EventoRepository;
import com.micultura.backend.repository.SavedEventRepository;
import com.micultura.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedEventService {

    private final SavedEventRepository savedEventRepository;
    private final EventoRepository eventoRepository;
    private final UserRepository userRepository;

    public List<EventoResponse> listForUserEmail(String email) {
        User user = userOrThrow(email);
        return savedEventRepository.findByUserOrderBySavedAtDesc(user).stream()
                .map(SavedEvent::getEvento)
                .filter(Evento::isActivo)
                .map(SavedEventService::toResponse)
                .toList();
    }

    @Transactional
    public void save(String email, Long eventoId) {
        User user = userOrThrow(email);
        Evento evento = eventoRepository.findById(eventoId)
                .filter(Evento::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + eventoId));

        if (savedEventRepository.existsByUserAndEvento(user, evento)) {
            return;
        }

        savedEventRepository.save(SavedEvent.builder()
                .user(user)
                .evento(evento)
                .build());
    }

    @Transactional
    public void remove(String email, Long eventoId) {
        User user = userOrThrow(email);
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + eventoId));
        savedEventRepository.deleteByUserAndEvento(user, evento);
    }

    public boolean isSaved(String email, Long eventoId) {
        User user = userOrThrow(email);
        Evento evento = eventoRepository.findById(eventoId).orElse(null);
        if (evento == null) return false;
        return savedEventRepository.existsByUserAndEvento(user, evento);
    }

    private User userOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

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
