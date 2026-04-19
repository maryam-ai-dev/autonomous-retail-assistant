-- Sprint B8.1: reactions.
--
-- Reactions are the v1 replacement for star ratings — three reaction types
-- (TRIED_THIS, BETTER_ALT, WOULDNT_RECOMMEND) and one reaction per user per
-- post (toggle semantics enforced by the unique index).

CREATE TABLE post_reactions (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id         UUID            NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id         UUID            NOT NULL REFERENCES users(id),
    reaction_type   VARCHAR(40)     NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_post_reactions_post_user ON post_reactions (post_id, user_id);
CREATE INDEX idx_post_reactions_user ON post_reactions (user_id);
