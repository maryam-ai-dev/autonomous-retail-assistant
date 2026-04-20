-- Sprint B12.3: clothing size preference.
--
-- Adds a size_preference column to taste_profiles so applySizeFilter() can
-- honour "return one size up / one size down" as well as exact matches.
-- Size field columns (top_size, bottom_size, shoe_size_uk, dress_size)
-- already exist from V34.

ALTER TABLE taste_profiles
    ADD COLUMN IF NOT EXISTS size_preference VARCHAR(20) DEFAULT 'EXACT';

ALTER TABLE taste_profiles
    ADD CONSTRAINT size_preference_valid
        CHECK (size_preference IN ('EXACT', 'SIZE_UP', 'SIZE_DOWN'));
