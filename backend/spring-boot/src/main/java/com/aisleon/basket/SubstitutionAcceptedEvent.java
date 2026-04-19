package com.aisleon.basket;

import java.time.Instant;
import java.util.UUID;

public record SubstitutionAcceptedEvent(
        UUID basketId,
        UUID basketItemId,
        UUID userId,
        String flagType,
        Instant acceptedAt) {}
