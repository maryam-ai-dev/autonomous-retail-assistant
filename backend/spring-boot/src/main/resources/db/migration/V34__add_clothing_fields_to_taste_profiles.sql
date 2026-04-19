ALTER TABLE taste_profiles
    ADD COLUMN top_size             VARCHAR(8),
    ADD COLUMN bottom_size          VARCHAR(8),
    ADD COLUMN shoe_size_uk         DECIMAL(3,1),
    ADD COLUMN dress_size           VARCHAR(8),
    ADD COLUMN clothing_preferences TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[];
