-- Sprint B8.3: polymorphic comments.
--
-- Comments are attached to either a POST or a SHARED_BASKET. target_type is
-- the discriminator. The 500-char body limit is enforced both here and in the
-- application layer (the service rejects long comments before persisting).

CREATE TABLE comments (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    target_type     VARCHAR(30)     NOT NULL,
    target_id       UUID            NOT NULL,
    author_user_id  UUID            NOT NULL REFERENCES users(id),
    body            VARCHAR(500)    NOT NULL,
    reported        BOOLEAN         NOT NULL DEFAULT false,
    reported_count  INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_comments_target       ON comments (target_type, target_id, created_at DESC);
CREATE INDEX idx_comments_author       ON comments (author_user_id);
