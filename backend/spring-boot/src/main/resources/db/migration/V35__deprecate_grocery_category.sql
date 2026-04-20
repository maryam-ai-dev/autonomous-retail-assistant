-- Sprint B12.1: grocery category deprecated.
--
-- Aisleon is now non-food retail only (health/beauty, general merchandise,
-- fashion, electronics). Grocery is handled by NourishOS. Existing rows with
-- category='GROCERY' are retained but never surfaced via the API — see
-- ProductCategory.java @Deprecated annotation and CandidateSelectionService
-- filter. Do not re-enable here.

COMMENT ON TABLE products IS 'GROCERY category rows are deprecated as of v1.1 (sprint B12.1). Never surfaced via API — see ProductCategory @Deprecated and CandidateSelectionService grocery guard.';
