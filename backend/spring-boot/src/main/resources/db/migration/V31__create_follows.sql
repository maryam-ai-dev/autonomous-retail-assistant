-- Sprint B8.3: user follows (graph).
--
-- Directed edge: (follower_user_id) -> (followee_user_id). One row per pair.
-- Self-follow is rejected by the application layer, not the DB, so we can
-- return a clean 400 instead of a SQL constraint error.

CREATE TABLE follows (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_user_id    UUID            NOT NULL REFERENCES users(id),
    followee_user_id    UUID            NOT NULL REFERENCES users(id),
    created_at          TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_follows_follower_followee
    ON follows (follower_user_id, followee_user_id);
CREATE INDEX idx_follows_follower ON follows (follower_user_id);
CREATE INDEX idx_follows_followee ON follows (followee_user_id);
