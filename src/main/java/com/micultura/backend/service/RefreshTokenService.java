package com.micultura.backend.service;

import com.micultura.backend.entity.RefreshToken;
import com.micultura.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final RefreshTokenRepository repository;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long defaultRefreshExpirationMs;

    @Value("${app.jwt.refresh-remember-expiration-ms}")
    private long rememberRefreshExpirationMs;

    public IssuedToken issue(UUID userId, boolean remembered) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String rawToken = URL_ENCODER.encodeToString(bytes);

        long ttlMs = remembered ? rememberRefreshExpirationMs : defaultRefreshExpirationMs;

        RefreshToken entity = RefreshToken.builder()
                .userId(userId)
                .tokenHash(hash(rawToken))
                .expiresAt(Instant.now().plusMillis(ttlMs))
                .revoked(false)
                .remembered(remembered)
                .createdAt(Instant.now())
                .build();

        repository.save(entity);
        return new IssuedToken(rawToken, ttlMs);
    }

    public Optional<RefreshToken> findValid(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return repository.findByTokenHash(hash(rawToken))
                .filter(rt -> !rt.isRevoked())
                .filter(rt -> rt.getExpiresAt().isAfter(Instant.now()));
    }

    public IssuedToken rotate(RefreshToken existing) {
        existing.setRevoked(true);
        repository.save(existing);
        return issue(existing.getUserId(), existing.isRemembered());
    }

    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        repository.findByTokenHash(hash(rawToken)).ifPresent(rt -> {
            rt.setRevoked(true);
            repository.save(rt);
        });
    }

    private static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] out = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return URL_ENCODER.encodeToString(out);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public record IssuedToken(String rawToken, long ttlMs) {}
}
