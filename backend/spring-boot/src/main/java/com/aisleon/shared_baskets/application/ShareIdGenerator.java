package com.aisleon.shared_baskets.application;

import com.aisleon.shared_baskets.infrastructure.SharedBasketRepository;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * Generates 8-character alphanumeric share IDs (uppercase + digits) with
 * repository-backed collision checking. 62^8 ≈ 2.18×10^14 unique IDs, so
 * collisions are vanishingly unlikely, but we check anyway.
 */
@Component
public class ShareIdGenerator {

    private static final String ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789";
    private static final int LENGTH = 8;
    private static final int MAX_ATTEMPTS = 10;

    private final SharedBasketRepository repository;
    private final SecureRandom random;

    public ShareIdGenerator(SharedBasketRepository repository) {
        this.repository = repository;
        this.random = new SecureRandom();
    }

    public String generate() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = randomToken();
            if (!repository.existsByShareId(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "failed to generate unique share_id after " + MAX_ATTEMPTS + " attempts");
    }

    private String randomToken() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
