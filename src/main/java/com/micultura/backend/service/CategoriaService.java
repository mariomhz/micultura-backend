package com.micultura.backend.service;

import com.micultura.backend.dto.CategoriaResponse;
import com.micultura.backend.entity.Categoria;
import com.micultura.backend.exception.ResourceNotFoundException;
import com.micultura.backend.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<CategoriaResponse> findAll() {
        return categoriaRepository.findAll()
                .stream()
                .map(CategoriaService::toResponse)
                .toList();
    }

    public CategoriaResponse findById(Long id) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + id));
        return toResponse(cat);
    }

    // ── Mapper ──────────────────────────────────────────────────────────────

    public static CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.getIcono());
    }
}
