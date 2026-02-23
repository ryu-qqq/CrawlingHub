-- V20__seed_seller_data.sql
-- Seller 시드 데이터
-- 운영 환경에서 추출한 기본 셀러 데이터 (8건)

INSERT INTO seller (id, must_it_seller_name, seller_name, status, product_count, created_at, updated_at) VALUES (1, 'italiagom', 'italiagom', 'ACTIVE', 0, NOW(), NOW());
INSERT INTO seller (id, must_it_seller_name, seller_name, status, product_count, created_at, updated_at) VALUES (2, 'LIKEASTAR', 'LIKEASTAR', 'ACTIVE', 0, NOW(), NOW());
INSERT INTO seller (id, must_it_seller_name, seller_name, status, product_count, created_at, updated_at) VALUES (3, 'thefactor2', 'thefactor2', 'ACTIVE', 0, NOW(), NOW());
INSERT INTO seller (id, must_it_seller_name, seller_name, status, product_count, created_at, updated_at) VALUES (4, 'fixedone', 'fixedone', 'ACTIVE', 0, NOW(), NOW());
INSERT INTO seller (id, must_it_seller_name, seller_name, status, product_count, created_at, updated_at) VALUES (5, 'bino2345', 'bino2345', 'ACTIVE', 0, NOW(), NOW());
INSERT INTO seller (id, must_it_seller_name, seller_name, status, product_count, created_at, updated_at) VALUES (6, 'wdrobe', 'wdrobe', 'ACTIVE', 0, NOW(), NOW());
INSERT INTO seller (id, must_it_seller_name, seller_name, status, product_count, created_at, updated_at) VALUES (7, 'viaitalia', 'viaitalia', 'ACTIVE', 0, NOW(), NOW());
INSERT INTO seller (id, must_it_seller_name, seller_name, status, product_count, created_at, updated_at) VALUES (8, 'ccapsule1', 'ccapsule1', 'ACTIVE', 0, NOW(), NOW());

-- AUTO_INCREMENT 복원
ALTER TABLE seller AUTO_INCREMENT = 9;
