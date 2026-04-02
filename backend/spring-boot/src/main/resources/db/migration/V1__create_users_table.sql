CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    username      VARCHAR(100) UNIQUE NOT NULL,
    display_name  VARCHAR(255),
    status        VARCHAR(50)  DEFAULT 'ACTIVE',
    created_at    TIMESTAMP    DEFAULT now(),
    updated_at    TIMESTAMP    DEFAULT now()
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_created_at ON users (created_at);
