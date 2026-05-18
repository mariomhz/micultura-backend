package com.micultura.backend.controller;

import com.micultura.backend.dto.AISearchRequest;
import com.micultura.backend.dto.AISearchResponse;
import com.micultura.backend.service.AISearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class AISearchController {

    private final AISearchService aiSearchService;

    @PostMapping("/ai")
    public AISearchResponse aiSearch(@Valid @RequestBody AISearchRequest request) {
        return aiSearchService.search(request.query());
    }
}
