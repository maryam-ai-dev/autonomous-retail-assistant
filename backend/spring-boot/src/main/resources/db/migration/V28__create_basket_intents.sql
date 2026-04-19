-- Sprint B6.3: basket_intents + baskets + basket_items.
--
-- basket_intents captures the raw + parsed user intent once per submission.
-- baskets are the Spring-authoritative result of the generation flow.
-- basket_items hold the individual NormalizedProduct references and flags.

CREATE TABLE basket_intents (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL REFERENCES users(id),
    raw_text        TEXT            NOT NULL,
    budget          DECIMAL(10,2),
    currency        VARCHAR(3)      NOT NULL DEFAULT 'GBP',
    category        VARCHAR(40)     NOT NULL DEFAULT 'GROCERY',
    tags            TEXT[]          NOT NULL DEFAULT ARRAY[]::TEXT[],
    halal_required  BOOLEAN         NOT NULL DEFAULT false,
    created_at      TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_basket_intents_user_id ON basket_intents (user_id);
CREATE INDEX idx_basket_intents_created_at ON basket_intents (created_at DESC);

CREATE TABLE baskets (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL REFERENCES users(id),
    basket_intent_id    UUID            NOT NULL REFERENCES basket_intents(id),
    status              VARCHAR(30)     NOT NULL DEFAULT 'DRAFT',
    total_cost          DECIMAL(10,2)   NOT NULL DEFAULT 0,
    currency            VARCHAR(3)      NOT NULL DEFAULT 'GBP',
    retailers_used      TEXT[]          NOT NULL DEFAULT ARRAY[]::TEXT[],
    trimmed             BOOLEAN         NOT NULL DEFAULT false,
    trimmed_item_count  INTEGER         NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_baskets_user_id ON baskets (user_id);
CREATE INDEX idx_baskets_status ON baskets (status);
CREATE INDEX idx_baskets_intent ON baskets (basket_intent_id);

CREATE TABLE basket_items (
    id                   UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    basket_id            UUID           NOT NULL REFERENCES baskets(id) ON DELETE CASCADE,
    external_product_id  VARCHAR(255)   NOT NULL,
    retailer             VARCHAR(40)    NOT NULL,
    canonical_name       VARCHAR(500)   NOT NULL,
    display_name         VARCHAR(500)   NOT NULL,
    brand                VARCHAR(255),
    price                DECIMAL(10,2)  NOT NULL,
    quantity             INTEGER        NOT NULL DEFAULT 1,
    image_url            VARCHAR(1000),
    product_url          VARCHAR(1000),
    reasoning            TEXT,
    dietary_tags         TEXT[]         NOT NULL DEFAULT ARRAY[]::TEXT[],
    substitution_flag_type   VARCHAR(50),
    substitution_flag_reason TEXT,
    substitution_flag_resolved BOOLEAN NOT NULL DEFAULT false,
    created_at           TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE INDEX idx_basket_items_basket_id ON basket_items (basket_id);
