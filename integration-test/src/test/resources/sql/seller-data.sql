-- ============================================
-- Seller Test Data
-- ============================================

INSERT INTO seller (id, must_it_seller_name, seller_name, status, product_count, created_at, updated_at)
VALUES
    (1, 'test-must-it-seller-1', 'test-seller-1', 'ACTIVE', 100, NOW(), NOW()),
    (2, 'test-must-it-seller-2', 'test-seller-2', 'ACTIVE', 50, NOW(), NOW()),
    (3, 'test-must-it-seller-3', 'test-seller-3', 'INACTIVE', 0, NOW(), NOW());
