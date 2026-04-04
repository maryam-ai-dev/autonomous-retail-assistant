CREATE TABLE checkout_orders (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id),
    cart_id         UUID NOT NULL REFERENCES carts(id),
    approval_id     UUID NOT NULL REFERENCES approval_requests(id),
    status          VARCHAR(50) NOT NULL DEFAULT 'INITIATED',
    executor_type   VARCHAR(50),
    merchant_order_ref VARCHAR(255),
    total_amount    DECIMAL(10,2),
    currency        VARCHAR(10),
    created_at      TIMESTAMP DEFAULT now(),
    completed_at    TIMESTAMP,
    error_message   TEXT
);

CREATE INDEX idx_checkout_orders_user_id ON checkout_orders(user_id);
CREATE INDEX idx_checkout_orders_cart_id ON checkout_orders(cart_id);
CREATE INDEX idx_checkout_orders_created_at ON checkout_orders(created_at);
