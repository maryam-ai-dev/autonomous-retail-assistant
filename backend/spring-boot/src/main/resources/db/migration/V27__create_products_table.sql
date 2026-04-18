CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id TEXT NOT NULL,
    retailer TEXT NOT NULL,
    canonical_name TEXT NOT NULL,
    display_name TEXT NOT NULL,
    brand TEXT,
    category TEXT,
    subcategory TEXT,
    price NUMERIC(10, 2) NOT NULL,
    unit_price NUMERIC(10, 4),
    unit_basis TEXT,
    size_text TEXT,
    image_url TEXT,
    product_url TEXT,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    is_basketable BOOLEAN NOT NULL DEFAULT TRUE,
    dietary_tags TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    certification_tags TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    offer_flags TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    confidence_score DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    normalization_warnings TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    cross_retailer_product_ids TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    source_fetched_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (retailer, external_id)
);

CREATE INDEX IF NOT EXISTS idx_products_retailer_canonical_name
    ON products (retailer, canonical_name);

CREATE INDEX IF NOT EXISTS idx_products_subcategory
    ON products (subcategory);
