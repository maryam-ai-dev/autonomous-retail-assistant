-- Sprint B9.1: shared baskets.
--
-- A SharedBasket is a public, read-only snapshot of an APPROVED basket. The
-- share_id is an 8-char alphanumeric string used in the public URL; it is
-- generated application-side with collision checks.

CREATE TABLE shared_baskets (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    basket_id       UUID            NOT NULL REFERENCES baskets(id),
    owner_user_id   UUID            NOT NULL REFERENCES users(id),
    share_id        VARCHAR(8)      NOT NULL UNIQUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_shared_baskets_basket_id ON shared_baskets (basket_id);
CREATE INDEX idx_shared_baskets_owner ON shared_baskets (owner_user_id);
