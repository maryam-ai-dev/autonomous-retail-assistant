CREATE TABLE taste_profiles (
    id                       UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                  UUID         NOT NULL UNIQUE REFERENCES users(id),
    halal_only               BOOLEAN      NOT NULL DEFAULT false,
    vegan_only               BOOLEAN      NOT NULL DEFAULT false,
    vegetarian_only          BOOLEAN      NOT NULL DEFAULT false,
    gluten_free              BOOLEAN      NOT NULL DEFAULT false,
    dairy_free               BOOLEAN      NOT NULL DEFAULT false,
    organic_preferred        BOOLEAN      NOT NULL DEFAULT false,
    retailer_allow_list      TEXT[]       NOT NULL DEFAULT ARRAY[]::TEXT[],
    retailer_deny_list       TEXT[]       NOT NULL DEFAULT ARRAY[]::TEXT[],
    preferred_brands         TEXT[]       NOT NULL DEFAULT ARRAY[]::TEXT[],
    household_size           INT          NOT NULL DEFAULT 1,
    has_children             BOOLEAN      NOT NULL DEFAULT false,
    has_pets                 BOOLEAN      NOT NULL DEFAULT false,
    cleaning_scent_sensitive BOOLEAN      NOT NULL DEFAULT false,
    created_at               TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_taste_profiles_user_id ON taste_profiles (user_id);
