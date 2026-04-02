CREATE TABLE audit_events (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         REFERENCES users(id),
    event_type  VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id   VARCHAR(255),
    payload     JSONB,
    created_at  TIMESTAMP    DEFAULT now()
);

CREATE INDEX idx_audit_events_user_created ON audit_events (user_id, created_at DESC);
