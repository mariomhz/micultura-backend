package com.micultura.backend.controller;

import com.micultura.backend.dto.CategoriaResponse;
import com.micultura.backend.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    /** GET /api/categories — list all categories */
    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> getAll() {
        return ResponseEntity.ok(categoriaService.findAll());
    }

    /** GET /api/categories/{id} — single category */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.findById(id));
    }
}
