package com.micultura.backend.service;

import com.micultura.backend.dto.AuthResponse;

public record AuthResult(AuthResponse response, String rawRefreshToken, long refreshTtlMs) {}
