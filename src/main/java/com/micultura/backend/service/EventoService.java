package com.micultura.backend.service;

import com.micultura.backend.dto.EventoRequest;
import com.micultura.backend.dto.EventoResponse;
import com.micultura.backend.dto.PagedResponse;
import com.micultura.backend.entity.Categoria;
import com.micultura.backend.entity.Evento;
import com.micultura.backend.exception.ResourceNotFoundException;
import com.micultura.backend.repository.CategoriaRepository;
import com.micultura.backend.repository.EventoRepository;
import com.micultura.backend.spec.EventoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository    eventoRepository;
    private final CategoriaRepository categoriaRepository;

    public PagedResponse<EventoResponse> findAll(
            Long       categoriaId,
            String     search,
            LocalDate  fechaDesde,
            LocalDate  fechaHasta,
            BigDecimal precioMin,
            BigDecimal precioMax,
            Pageable   pageable
    ) {
        Specification<Evento> spec = Specification
                .where(EventoSpecification.activo())
                .and(EventoSpecification.noFinalizado())
                .and(EventoSpecification.byCategoria(categoriaId))
                .and(EventoSpecification.searchText(search))
                .and(EventoSpecification.fechaDesde(fechaDesde))
                .and(EventoSpecification.fechaHasta(fechaHasta))
                .and(EventoSpecification.precioMin(precioMin))
                .and(EventoSpecification.precioMax(precioMax));

        Page<EventoResponse> page = eventoRepository.findAll(spec, pageable)
                .map(this::toResponse);

        return PagedResponse.of(page);
    }

    public EventoResponse findById(Long id) {
        Evento evento = eventoRepository.findById(id)
                .filter(Evento::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + id));
        return toResponse(evento);
    }

    public EventoResponse create(EventoRequest req) {
        Categoria categoria = categoriaOrThrow(req.categoriaId());
        Evento evento = Evento.builder()
                .titulo(req.titulo())
                .descripcion(req.descripcion())
                .fecha(req.fecha())
                .hora(req.hora())
                .ubicacion(req.ubicacion())
                .latitud(req.latitud())
                .longitud(req.longitud())
                .categoria(categoria)
                .imagenUrl(req.imagenUrl())
                .precio(req.precio())
                .enlaceCompra(req.enlaceCompra())
                .build();
        return toResponse(eventoRepository.save(evento));
    }

    public EventoResponse update(Long id, EventoRequest req) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + id));
        Categoria categoria = categoriaOrThrow(req.categoriaId());

        evento.setTitulo(req.titulo());
        evento.setDescripcion(req.descripcion());
        evento.setFecha(req.fecha());
        evento.setHora(req.hora());
        evento.setUbicacion(req.ubicacion());
        evento.setLatitud(req.latitud());
        evento.setLongitud(req.longitud());
        evento.setCategoria(categoria);
        evento.setImagenUrl(req.imagenUrl());
        evento.setPrecio(req.precio());
        evento.setEnlaceCompra(req.enlaceCompra());

        return toResponse(eventoRepository.save(evento));
    }

    public void delete(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + id));
        evento.setActivo(false);
        eventoRepository.save(evento);
    }

    private Categoria categoriaOrThrow(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + id));
    }

    private EventoResponse toResponse(Evento e) {
        return new EventoResponse(
                e.getId(),
                e.getTitulo(),
                e.getDescripcion(),
                e.getFecha().toString(),
                e.getHora() != null ? e.getHora().toString() : null,
                e.getUbicacion(),
                e.getLatitud(),
                e.getLongitud(),
                CategoriaService.toResponse(e.getCategoria()),
                e.getImagenUrl(),
                e.getPrecio(),
                e.getEnlaceCompra()
        );
    }
}
