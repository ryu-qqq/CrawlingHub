-- =====================================================
-- V11: 유연한 스키마 초기 데이터
-- Author: Crawling Hub Team  
-- Date: 2024-01-20
-- Description: 범용 크롤링 시스템 초기 데이터
-- =====================================================

-- =====================================================
-- 1. 기본 타입 정의
-- =====================================================

-- 크롤링 대상 타입 정의
INSERT INTO target_types (type_code, type_name, description) VALUES 
('PRODUCT', '상품', '판매 상품 정보'),
('BRAND', '브랜드', '브랜드 정보'),
('CATEGORY', '카테고리', '상품 카테고리'),
('SELLER', '판매자', '판매자/셀러 정보'),
('REVIEW', '리뷰', '상품 리뷰'),
('NEWS', '뉴스', '뉴스 기사'),
('PRICE', '가격', '가격 정보'),
('STOCK', '재고', '재고 정보'),
('PROMOTION', '프로모션', '할인/이벤트 정보'),
('IMAGE', '이미지', '이미지 정보'),
('VIDEO', '동영상', '동영상 정보'),
('DOCUMENT', '문서', '문서/PDF 정보'),
('SOCIAL_POST', '소셜포스트', 'SNS 게시물'),
('COMMENT', '댓글', '댓글/커멘트'),
('FAQ', 'FAQ', '자주 묻는 질문'),
('SPECIFICATION', '사양', '제품 사양 정보'),
('STORE', '매장', '오프라인 매장 정보'),
('EVENT', '이벤트', '이벤트 정보');

-- =====================================================
-- 2. 타입별 속성 정의
-- =====================================================

-- 상품 타입 속성
SET @product_type_id = (SELECT id FROM target_types WHERE type_code = 'PRODUCT');
INSERT INTO target_attributes (type_id, attribute_code, attribute_name, data_type, is_required, is_indexed, display_order) VALUES
(@product_type_id, 'name', '상품명', 'STRING', TRUE, TRUE, 1),
(@product_type_id, 'sku', 'SKU', 'STRING', FALSE, TRUE, 2),
(@product_type_id, 'price', '판매가', 'NUMBER', TRUE, TRUE, 3),
(@product_type_id, 'original_price', '정가', 'NUMBER', FALSE, TRUE, 4),
(@product_type_id, 'discount_rate', '할인율', 'NUMBER', FALSE, FALSE, 5),
(@product_type_id, 'brand', '브랜드', 'STRING', FALSE, TRUE, 6),
(@product_type_id, 'category', '카테고리', 'STRING', FALSE, TRUE, 7),
(@product_type_id, 'sub_category', '서브카테고리', 'STRING', FALSE, FALSE, 8),
(@product_type_id, 'description', '상품설명', 'TEXT', FALSE, FALSE, 9),
(@product_type_id, 'short_description', '간단설명', 'STRING', FALSE, FALSE, 10),
(@product_type_id, 'image_urls', '이미지URL', 'JSON', FALSE, FALSE, 11),
(@product_type_id, 'thumbnail_url', '썸네일URL', 'URL', FALSE, FALSE, 12),
(@product_type_id, 'stock_status', '재고상태', 'STRING', FALSE, TRUE, 13),
(@product_type_id, 'stock_quantity', '재고수량', 'NUMBER', FALSE, FALSE, 14),
(@product_type_id, 'rating', '평점', 'NUMBER', FALSE, TRUE, 15),
(@product_type_id, 'review_count', '리뷰수', 'NUMBER', FALSE, FALSE, 16),
(@product_type_id, 'tags', '태그', 'JSON', FALSE, FALSE, 17),
(@product_type_id, 'options', '옵션', 'JSON', FALSE, FALSE, 18),
(@product_type_id, 'shipping_fee', '배송비', 'NUMBER', FALSE, FALSE, 19),
(@product_type_id, 'weight', '무게', 'NUMBER', FALSE, FALSE, 20);

-- 브랜드 타입 속성
SET @brand_type_id = (SELECT id FROM target_types WHERE type_code = 'BRAND');
INSERT INTO target_attributes (type_id, attribute_code, attribute_name, data_type, is_required, is_indexed, display_order) VALUES
(@brand_type_id, 'brand_name', '브랜드명', 'STRING', TRUE, TRUE, 1),
(@brand_type_id, 'brand_name_en', '브랜드명(영문)', 'STRING', FALSE, TRUE, 2),
(@brand_type_id, 'country', '국가', 'STRING', FALSE, TRUE, 3),
(@brand_type_id, 'founded_year', '설립연도', 'NUMBER', FALSE, FALSE, 4),
(@brand_type_id, 'description', '브랜드 설명', 'TEXT', FALSE, FALSE, 5),
(@brand_type_id, 'logo_url', '로고 URL', 'URL', FALSE, FALSE, 6),
(@brand_type_id, 'official_website', '공식 웹사이트', 'URL', FALSE, FALSE, 7),
(@brand_type_id, 'category_focus', '주력 카테고리', 'STRING', FALSE, TRUE, 8);

-- 리뷰 타입 속성
SET @review_type_id = (SELECT id FROM target_types WHERE type_code = 'REVIEW');
INSERT INTO target_attributes (type_id, attribute_code, attribute_name, data_type, is_required, is_indexed, display_order) VALUES
(@review_type_id, 'product_id', '상품ID', 'STRING', TRUE, TRUE, 1),
(@review_type_id, 'reviewer_name', '리뷰어', 'STRING', FALSE, FALSE, 2),
(@review_type_id, 'rating', '평점', 'NUMBER', TRUE, TRUE, 3),
(@review_type_id, 'title', '제목', 'STRING', FALSE, FALSE, 4),
(@review_type_id, 'content', '내용', 'TEXT', TRUE, FALSE, 5),
(@review_type_id, 'pros', '장점', 'TEXT', FALSE, FALSE, 6),
(@review_type_id, 'cons', '단점', 'TEXT', FALSE, FALSE, 7),
(@review_type_id, 'review_date', '작성일', 'DATE', TRUE, TRUE, 8),
(@review_type_id, 'verified_purchase', '구매인증', 'BOOLEAN', FALSE, TRUE, 9),
(@review_type_id, 'helpful_count', '도움된수', 'NUMBER', FALSE, FALSE, 10),
(@review_type_id, 'images', '리뷰이미지', 'JSON', FALSE, FALSE, 11);

-- 셀러 타입 속성
SET @seller_type_id = (SELECT id FROM target_types WHERE type_code = 'SELLER');
INSERT INTO target_attributes (type_id, attribute_code, attribute_name, data_type, is_required, is_indexed, display_order) VALUES
(@seller_type_id, 'seller_name', '셀러명', 'STRING', TRUE, TRUE, 1),
(@seller_type_id, 'seller_code', '셀러코드', 'STRING', TRUE, TRUE, 2),
(@seller_type_id, 'shop_url', '샵URL', 'URL', FALSE, FALSE, 3),
(@seller_type_id, 'rating', '평점', 'NUMBER', FALSE, TRUE, 4),
(@seller_type_id, 'product_count', '상품수', 'NUMBER', FALSE, FALSE, 5),
(@seller_type_id, 'follower_count', '팔로워수', 'NUMBER', FALSE, FALSE, 6),
(@seller_type_id, 'description', '셀러설명', 'TEXT', FALSE, FALSE, 7),
(@seller_type_id, 'joined_date', '가입일', 'DATE', FALSE, FALSE, 8),
(@seller_type_id, 'location', '위치', 'STRING', FALSE, FALSE, 9),
(@seller_type_id, 'business_type', '사업자유형', 'STRING', FALSE, TRUE, 10);

-- 가격 타입 속성
SET @price_type_id = (SELECT id FROM target_types WHERE type_code = 'PRICE');
INSERT INTO target_attributes (type_id, attribute_code, attribute_name, data_type, is_required, is_indexed, display_order) VALUES
(@price_type_id, 'product_id', '상품ID', 'STRING', TRUE, TRUE, 1),
(@price_type_id, 'price', '가격', 'NUMBER', TRUE, TRUE, 2),
(@price_type_id, 'currency', '통화', 'STRING', FALSE, TRUE, 3),
(@price_type_id, 'price_type', '가격유형', 'STRING', FALSE, TRUE, 4),
(@price_type_id, 'effective_date', '적용일', 'DATE', TRUE, TRUE, 5),
(@price_type_id, 'min_quantity', '최소수량', 'NUMBER', FALSE, FALSE, 6),
(@price_type_id, 'max_quantity', '최대수량', 'NUMBER', FALSE, FALSE, 7);

-- 재고 타입 속성
SET @stock_type_id = (SELECT id FROM target_types WHERE type_code = 'STOCK');
INSERT INTO target_attributes (type_id, attribute_code, attribute_name, data_type, is_required, is_indexed, display_order) VALUES
(@stock_type_id, 'product_id', '상품ID', 'STRING', TRUE, TRUE, 1),
(@stock_type_id, 'quantity', '재고수량', 'NUMBER', TRUE, TRUE, 2),
(@stock_type_id, 'status', '재고상태', 'STRING', TRUE, TRUE, 3),
(@stock_type_id, 'location', '위치', 'STRING', FALSE, TRUE, 4),
(@stock_type_id, 'last_updated', '최종업데이트', 'DATE', TRUE, TRUE, 5);

-- =====================================================
-- 3. MUSTIT 소스 설정
-- =====================================================

-- MUSTIT 소스 등록
INSERT INTO crawling_sources (source_code, name, base_url, api_base_url, source_type, status) VALUES
('MUSTIT', '머스트잇', 'https://mustit.co.kr', 'https://api.mustit.co.kr', 'API', 'ACTIVE');

-- MUSTIT 크롤링 규칙
SET @mustit_id = (SELECT id FROM crawling_sources WHERE source_code = 'MUSTIT');

INSERT INTO crawling_rules (source_id, rule_type, rule_name, rule_value, rule_unit) VALUES
(@mustit_id, 'DELAY', '요청 간격', '1000', 'ms'),
(@mustit_id, 'RETRY', '재시도 횟수', '3', 'count'),
(@mustit_id, 'TIMEOUT', '타임아웃', '30', 'seconds'),
(@mustit_id, 'RATE_LIMIT', '분당 요청 제한', '60', 'requests/minute'),
(@mustit_id, 'PARALLELISM', '동시 요청 수', '5', 'count');

-- MUSTIT HTTP 헤더
INSERT INTO crawling_headers (source_id, header_name, header_value) VALUES
(@mustit_id, 'Accept', 'application/json'),
(@mustit_id, 'Accept-Language', 'ko-KR,ko;q=0.9,en;q=0.8'),
(@mustit_id, 'Cache-Control', 'no-cache'),
(@mustit_id, 'Referer', 'https://mustit.co.kr/');

-- MUSTIT 인증 설정 (비회원 토큰)
INSERT INTO source_auth_configs (source_id, auth_type, auth_name, auth_key, auth_location) VALUES
(@mustit_id, 'BEARER', '비회원 토큰', 'X-Guest-Token', 'HEADER');

-- =====================================================
-- 4. 기타 소스 예시
-- =====================================================

-- 다른 소스 예시 (확장 가능성 시연)
INSERT INTO crawling_sources (source_code, name, base_url, source_type, status) VALUES
('NAVER_NEWS', '네이버 뉴스', 'https://news.naver.com', 'WEB', 'ACTIVE'),
('INSTAGRAM', '인스타그램', 'https://www.instagram.com', 'WEB', 'INACTIVE'),
('COUPANG', '쿠팡', 'https://www.coupang.com', 'WEB', 'INACTIVE');

-- =====================================================
-- 5. 샘플 타겟 데이터
-- =====================================================

-- MUSTIT 셀러 타겟 예시
INSERT INTO crawling_targets (source_id, type_id, target_code, target_name, target_url, crawl_priority, crawl_interval_hours) VALUES
(@mustit_id, @seller_type_id, 'MUSTIT_SELLER_001', '프리미엄 셀러 A', 'https://mustit.co.kr/shop/seller001', 9, 6),
(@mustit_id, @seller_type_id, 'MUSTIT_SELLER_002', '프리미엄 셀러 B', 'https://mustit.co.kr/shop/seller002', 8, 12),
(@mustit_id, @seller_type_id, 'MUSTIT_SELLER_003', '일반 셀러 C', 'https://mustit.co.kr/shop/seller003', 5, 24);
