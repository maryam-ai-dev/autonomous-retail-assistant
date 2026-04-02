CREATE TABLE retail_preferences (
    id                            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                       UUID          REFERENCES users(id) UNIQUE,
    budget_cap                    DECIMAL(10,2),
    preferred_brands              TEXT[],
    blocked_brands                TEXT[],
    blocked_categories            TEXT[],
    allow_substitutions           BOOLEAN       DEFAULT true,
    approval_threshold            DECIMAL(10,2),
    max_substitution_price_delta  DECIMAL(10,2) DEFAULT 10.00,
    created_at                    TIMESTAMP     DEFAULT now(),
    updated_at                    TIMESTAMP     DEFAULT now()
);

CREATE INDEX idx_retail_preferences_user_id ON retail_preferences (user_id);

CREATE TABLE merchants (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    source_type VARCHAR(50),
    is_approved BOOLEAN      DEFAULT false,
    trust_score DECIMAL(3,2),
    api_key_ref VARCHAR(255),
    base_url    VARCHAR(500),
    created_at  TIMESTAMP    DEFAULT now()
);
