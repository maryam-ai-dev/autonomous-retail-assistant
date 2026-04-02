CREATE TABLE carts (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        REFERENCES users(id) UNIQUE,
    status     VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP   DEFAULT now(),
    updated_at TIMESTAMP   DEFAULT now()
);

CREATE INDEX idx_carts_user_id ON carts (user_id);

CREATE TABLE cart_items (
    id                   UUID              PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id              UUID              REFERENCES carts(id),
    external_product_id  VARCHAR(255),
    title                VARCHAR(500),
    price                DECIMAL(10,2),
    currency             VARCHAR(10),
    merchant_id          UUID              REFERENCES merchants(id),
    merchant_name        VARCHAR(255)      NOT NULL,
    merchant_rating      DOUBLE PRECISION,
    source_type          VARCHAR(50),
    source_name          VARCHAR(100),
    product_url          VARCHAR(1000),
    image_url            VARCHAR(1000),
    is_substitution      BOOLEAN           DEFAULT false,
    original_product_id  VARCHAR(255),
    added_at             TIMESTAMP         DEFAULT now()
);

CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);

CREATE TABLE approval_requests (
    id                   UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID          REFERENCES users(id),
    cart_id              UUID          REFERENCES carts(id),
    status               VARCHAR(50)   DEFAULT 'PENDING',
    trigger_reason       VARCHAR(255),
    total_amount         DECIMAL(10,2),
    requires_user_action BOOLEAN       DEFAULT true,
    created_at           TIMESTAMP     DEFAULT now(),
    decided_at           TIMESTAMP,
    decision             VARCHAR(50)
);

CREATE INDEX idx_approval_requests_user_id ON approval_requests (user_id);
CREATE INDEX idx_approval_requests_cart_id ON approval_requests (cart_id);
CREATE INDEX idx_approval_requests_created_at ON approval_requests (created_at);
