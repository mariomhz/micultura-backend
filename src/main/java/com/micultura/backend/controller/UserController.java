package com.micultura.backend.controller;

import com.micultura.backend.dto.EventoResponse;
import com.micultura.backend.dto.UpdateProfileRequest;
import com.micultura.backend.dto.UserResponse;
import com.micultura.backend.service.SavedEventService;
import com.micultura.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SavedEventService savedEventService;

    // ── Profile ───────────────────────────────────────────────────────────

    @GetMapping("/me")
    public UserResponse me() {
        return userService.findByEmail(currentEmail());
    }

    @PutMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(currentEmail(), request);
    }

    // ── Saved events ──────────────────────────────────────────────────────

    @GetMapping("/me/saved")
    public List<EventoResponse> listSaved() {
        return savedEventService.listForUserEmail(currentEmail());
    }

    @GetMapping("/me/saved/{eventoId}")
    public Map<String, Boolean> isSaved(@PathVariable Long eventoId) {
        return Map.of("saved", savedEventService.isSaved(currentEmail(), eventoId));
    }

    @PostMapping("/me/saved/{eventoId}")
    public ResponseEntity<Void> save(@PathVariable Long eventoId) {
        savedEventService.save(currentEmail(), eventoId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/me/saved/{eventoId}")
    public ResponseEntity<Void> remove(@PathVariable Long eventoId) {
        savedEventService.remove(currentEmail(), eventoId);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return auth.getName();
    }
}
