-- Sprint B8.1: social posts.
--
-- Posts are authored by users and shown in the social feed. A post can be a
-- free-text entry, a basket share, or a product review — the 'post_type'
-- column discriminates.
--
-- Moderation (v1): there is no automated moderation. The 'reported' flag lets
-- users flag a post for review; admin reads the column directly via pgAdmin.

CREATE TABLE posts (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL REFERENCES users(id),
    post_type           VARCHAR(30)     NOT NULL,
    basket_id           UUID            REFERENCES baskets(id),
    external_product_id VARCHAR(255),
    body                TEXT            NOT NULL DEFAULT '',
    image_url           VARCHAR(1000),
    reported            BOOLEAN         NOT NULL DEFAULT false,
    reported_count      INTEGER         NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_posts_user_id    ON posts (user_id);
CREATE INDEX idx_posts_created_at ON posts (created_at DESC);
CREATE INDEX idx_posts_type       ON posts (post_type);
CREATE INDEX idx_posts_basket_id  ON posts (basket_id);
