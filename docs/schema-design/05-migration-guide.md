# 🔄 마이그레이션 가이드

## 📌 개요

이 문서는 기존 도메인 종속적 스키마에서 새로운 범용 스키마로 마이그레이션하는 가이드입니다.
Flyway를 활용한 점진적 마이그레이션 전략을 제공합니다.

## 🏗️ 마이그레이션 전략

### 1. 무중단 마이그레이션
- 기존 테이블 유지하며 신규 테이블 병행 운영
- 점진적 데이터 이전
- 완료 후 기존 테이블 제거

### 2. 단계별 접근
```
Phase 1: 신규 스키마 생성 (V10-V15)
Phase 2: 데이터 마이그레이션 (V16-V20)
Phase 3: 애플리케이션 전환
Phase 4: 기존 스키마 제거 (V21+)
```

## 📂 Flyway 마이그레이션 구조

```
adapter/adapter-out-persistence-jpa/src/main/resources/db/migration/
├── V1-V9__legacy_schema.sql          # 기존 스키마 (유지)
├── V10__create_flexible_core.sql     # 범용 코어 테이블
├── V11__create_flexible_workflow.sql # 워크플로우 테이블
├── V12__create_flexible_security.sql # 보안 테이블
├── V13__create_flexible_monitoring.sql # 모니터링 테이블
├── V14__create_flexible_indexes.sql  # 인덱스 생성
├── V15__create_flexible_functions.sql # 함수/프로시저
├── V16__migrate_sites_to_sources.sql # 사이트→소스 마이그레이션
├── V17__migrate_products_to_targets.sql # 상품→타겟 마이그레이션
├── V18__migrate_workflows.sql        # 워크플로우 마이그레이션
├── V19__migrate_security_data.sql    # 보안 데이터 마이그레이션
├── V20__verify_migration.sql         # 마이그레이션 검증
└── V21__drop_legacy_tables.sql      # 기존 테이블 제거 (선택적)
```

## 📝 Phase 1: 신규 스키마 생성

### V10__create_flexible_core.sql
```sql
-- =====================================================
-- V10: 범용 크롤링 코어 스키마
-- =====================================================

-- 크롤링 소스
CREATE TABLE IF NOT EXISTS crawling_sources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    base_url VARCHAR(500),
    api_base_url VARCHAR(500),
    source_type ENUM('WEB', 'API', 'RSS', 'FILE', 'DATABASE') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    UNIQUE KEY uk_source_code (source_code),
    INDEX idx_sources_status (status, deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 타겟 타입 정의
CREATE TABLE IF NOT EXISTS target_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_code VARCHAR(50) NOT NULL,
    type_name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_type_id BIGINT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_type_code (type_code),
    FOREIGN KEY fk_parent_type (parent_type_id) REFERENCES target_types(id),
    INDEX idx_types_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 타겟 속성
CREATE TABLE IF NOT EXISTS target_attributes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_id BIGINT NOT NULL,
    attribute_code VARCHAR(100) NOT NULL,
    attribute_name VARCHAR(200) NOT NULL,
    data_type ENUM('STRING', 'NUMBER', 'DATE', 'BOOLEAN', 'JSON', 'TEXT', 'URL', 'EMAIL') NOT NULL,
    is_required BOOLEAN DEFAULT FALSE,
    is_indexed BOOLEAN DEFAULT FALSE,
    validation_pattern VARCHAR(500),
    default_value TEXT,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY fk_attr_type (type_id) REFERENCES target_types(id),
    UNIQUE KEY uk_attribute (type_id, attribute_code),
    INDEX idx_attributes_type (type_id, display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 크롤링 타겟
CREATE TABLE IF NOT EXISTS crawling_targets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT NOT NULL,
    type_id BIGINT NOT NULL,
    target_code VARCHAR(200) NOT NULL,
    target_name VARCHAR(500),
    target_url VARCHAR(1000),
    parent_target_id BIGINT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'ARCHIVED') DEFAULT 'ACTIVE',
    crawl_priority INT DEFAULT 5,
    crawl_interval_hours INT DEFAULT 24,
    last_crawled_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY fk_target_source (source_id) REFERENCES crawling_sources(id),
    FOREIGN KEY fk_target_type (type_id) REFERENCES target_types(id),
    FOREIGN KEY fk_parent_target (parent_target_id) REFERENCES crawling_targets(id),
    UNIQUE KEY uk_target (source_id, type_id, target_code),
    INDEX idx_targets_crawl (status, last_crawled_at, crawl_interval_hours),
    INDEX idx_targets_hierarchy (parent_target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 크롤링 데이터 버전
CREATE TABLE IF NOT EXISTS crawling_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    crawled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version_hash VARCHAR(64) NOT NULL,
    data_size_bytes BIGINT DEFAULT 0,
    s3_path VARCHAR(500),
    is_current BOOLEAN DEFAULT TRUE,
    change_type ENUM('CREATE', 'UPDATE', 'NO_CHANGE') DEFAULT 'CREATE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY fk_data_target (target_id) REFERENCES crawling_targets(id),
    INDEX idx_data_target (target_id, is_current, crawled_at DESC),
    INDEX idx_data_version (version_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 크롤링 데이터 값 (EAV)
CREATE TABLE IF NOT EXISTS crawling_data_values (
    data_id BIGINT NOT NULL,
    attribute_id BIGINT NOT NULL,
    value_text TEXT,
    value_number DECIMAL(20,6),
    value_date DATETIME,
    value_boolean BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (data_id, attribute_id),
    FOREIGN KEY fk_value_data (data_id) REFERENCES crawling_data(id) ON DELETE CASCADE,
    FOREIGN KEY fk_value_attr (attribute_id) REFERENCES target_attributes(id),
    INDEX idx_values_attribute (attribute_id, data_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## 📝 Phase 2: 데이터 마이그레이션

### V16__migrate_sites_to_sources.sql
```sql
-- =====================================================
-- V16: 기존 sites → crawling_sources 마이그레이션
-- =====================================================

-- 사이트를 소스로 마이그레이션
INSERT INTO crawling_sources (source_code, name, base_url, api_base_url, source_type, status, created_at)
SELECT 
    code,
    name,
    base_url,
    api_base_url,
    'WEB',
    CASE status 
        WHEN 'ACTIVE' THEN 'ACTIVE'
        WHEN 'INACTIVE' THEN 'INACTIVE'
        ELSE 'MAINTENANCE'
    END,
    created_at
FROM sites
WHERE deleted_at IS NULL;

-- 타입 정의
INSERT INTO target_types (type_code, type_name) VALUES
('SELLER', '셀러'),
('PRODUCT', '상품'),
('PRODUCT_OPTION', '상품옵션')
ON DUPLICATE KEY UPDATE type_name = VALUES(type_name);

-- 속성 정의
SET @product_type_id = (SELECT id FROM target_types WHERE type_code = 'PRODUCT');
INSERT INTO target_attributes (type_id, attribute_code, attribute_name, data_type, is_required, is_indexed) VALUES
(@product_type_id, 'name', '상품명', 'STRING', TRUE, TRUE),
(@product_type_id, 'product_code', '상품코드', 'STRING', TRUE, TRUE),
(@product_type_id, 'price', '가격', 'NUMBER', FALSE, TRUE),
(@product_type_id, 'original_price', '정가', 'NUMBER', FALSE, FALSE),
(@product_type_id, 'brand', '브랜드', 'STRING', FALSE, TRUE),
(@product_type_id, 'category', '카테고리', 'STRING', FALSE, TRUE),
(@product_type_id, 'description', '설명', 'TEXT', FALSE, FALSE),
(@product_type_id, 'status', '상태', 'STRING', FALSE, TRUE);
```

### V17__migrate_products_to_targets.sql
```sql
-- =====================================================
-- V17: 기존 products → crawling_targets + data 마이그레이션
-- =====================================================

-- 셀러를 타겟으로 마이그레이션
INSERT INTO crawling_targets (source_id, type_id, target_code, target_name, target_url, crawl_priority, crawl_interval_hours, created_at)
SELECT 
    cs.id,
    (SELECT id FROM target_types WHERE type_code = 'SELLER'),
    s.seller_code,
    s.name,
    s.shop_url,
    CASE s.priority 
        WHEN 'HIGH' THEN 9
        WHEN 'MEDIUM' THEN 5
        ELSE 3
    END,
    s.crawling_interval_hours,
    s.created_at
FROM sellers s
JOIN sites si ON s.site_id = si.id
JOIN crawling_sources cs ON si.code = cs.source_code
WHERE s.deleted_at IS NULL;

-- 상품을 타겟으로 마이그레이션
INSERT INTO crawling_targets (source_id, type_id, target_code, target_name, target_url, parent_target_id, created_at)
SELECT 
    cs.id,
    (SELECT id FROM target_types WHERE type_code = 'PRODUCT'),
    p.product_code,
    p.name,
    p.detail_url,
    ct.id,
    p.created_at
FROM products p
JOIN sellers s ON p.seller_id = s.id
JOIN sites si ON s.site_id = si.id
JOIN crawling_sources cs ON si.code = cs.source_code
JOIN crawling_targets ct ON s.seller_code = ct.target_code AND ct.source_id = cs.id
WHERE p.deleted_at IS NULL;

-- 상품 데이터를 EAV로 마이그레이션
INSERT INTO crawling_data (target_id, job_id, version_hash, is_current, created_at)
SELECT 
    ct.id,
    1, -- 임시 job_id
    MD5(CONCAT(p.name, p.sale_price, p.status)),
    TRUE,
    p.last_updated_at
FROM products p
JOIN crawling_targets ct ON p.product_code = ct.target_code
WHERE p.deleted_at IS NULL;

-- 상품 속성값 마이그레이션
INSERT INTO crawling_data_values (data_id, attribute_id, value_text, value_number)
SELECT 
    cd.id,
    ta.id,
    CASE ta.attribute_code
        WHEN 'name' THEN p.name
        WHEN 'product_code' THEN p.product_code
        WHEN 'brand' THEN p.brand
        WHEN 'category' THEN p.category
        WHEN 'description' THEN p.description
        WHEN 'status' THEN p.status
        ELSE NULL
    END,
    CASE ta.attribute_code
        WHEN 'price' THEN p.sale_price
        WHEN 'original_price' THEN p.original_price
        ELSE NULL
    END
FROM products p
JOIN crawling_targets ct ON p.product_code = ct.target_code
JOIN crawling_data cd ON ct.id = cd.target_id AND cd.is_current = TRUE
CROSS JOIN target_attributes ta
WHERE ta.type_id = (SELECT id FROM target_types WHERE type_code = 'PRODUCT')
  AND p.deleted_at IS NULL;
```

## 🔍 Phase 3: 검증

### V20__verify_migration.sql
```sql
-- =====================================================
-- V20: 마이그레이션 검증
-- =====================================================

-- 데이터 일관성 검증
SELECT 
    'products' as entity,
    COUNT(*) as legacy_count,
    (SELECT COUNT(*) FROM crawling_targets WHERE type_id = (SELECT id FROM target_types WHERE type_code = 'PRODUCT')) as new_count,
    COUNT(*) = (SELECT COUNT(*) FROM crawling_targets WHERE type_id = (SELECT id FROM target_types WHERE type_code = 'PRODUCT')) as is_matched
FROM products
WHERE deleted_at IS NULL

UNION ALL

SELECT 
    'sellers' as entity,
    COUNT(*) as legacy_count,
    (SELECT COUNT(*) FROM crawling_targets WHERE type_id = (SELECT id FROM target_types WHERE type_code = 'SELLER')) as new_count,
    COUNT(*) = (SELECT COUNT(*) FROM crawling_targets WHERE type_id = (SELECT id FROM target_types WHERE type_code = 'SELLER')) as is_matched
FROM sellers
WHERE deleted_at IS NULL;

-- 데이터 샘플 비교
SELECT 
    'Sample Comparison' as check_type,
    p.product_code as legacy_code,
    ct.target_code as new_code,
    p.name as legacy_name,
    (SELECT value_text FROM crawling_data_values cdv 
     JOIN crawling_data cd ON cdv.data_id = cd.id
     JOIN target_attributes ta ON cdv.attribute_id = ta.id
     WHERE cd.target_id = ct.id AND ta.attribute_code = 'name' AND cd.is_current = TRUE) as new_name
FROM products p
JOIN crawling_targets ct ON p.product_code = ct.target_code
WHERE p.deleted_at IS NULL
LIMIT 10;
```

## 🔄 롤백 전략

### 롤백 시나리오
```sql
-- Phase별 롤백
-- Phase 1 롤백: 신규 테이블만 제거
DROP TABLE IF EXISTS crawling_data_values;
DROP TABLE IF EXISTS crawling_data;
DROP TABLE IF EXISTS crawling_targets;
DROP TABLE IF EXISTS target_attributes;
DROP TABLE IF EXISTS target_types;
DROP TABLE IF EXISTS crawling_sources;

-- Phase 2 롤백: 마이그레이션된 데이터만 제거
DELETE FROM crawling_data_values WHERE created_at >= '2024-01-20';
DELETE FROM crawling_data WHERE created_at >= '2024-01-20';
DELETE FROM crawling_targets WHERE created_at >= '2024-01-20';

-- Phase 4 롤백: 기존 테이블 복구 (백업 필수)
```

## 📊 마이그레이션 모니터링

### 진행상황 추적
```sql
CREATE TABLE migration_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phase VARCHAR(50),
    entity VARCHAR(100),
    total_records BIGINT,
    migrated_records BIGINT,
    progress_percent DECIMAL(5,2),
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED'),
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 진행상황 업데이트
CREATE PROCEDURE update_migration_progress(
    IN p_phase VARCHAR(50),
    IN p_entity VARCHAR(100),
    IN p_migrated BIGINT,
    IN p_total BIGINT
)
BEGIN
    INSERT INTO migration_progress (phase, entity, migrated_records, total_records, progress_percent, status)
    VALUES (p_phase, p_entity, p_migrated, p_total, (p_migrated / p_total) * 100, 'RUNNING')
    ON DUPLICATE KEY UPDATE
        migrated_records = p_migrated,
        progress_percent = (p_migrated / p_total) * 100,
        status = IF(p_migrated >= p_total, 'COMPLETED', 'RUNNING');
END;
```

## 🚀 애플리케이션 전환

### Repository 레이어 수정
```java
// 기존 ProductRepository
@Repository
public class LegacyProductRepository {
    public Product findByCode(String code) {
        // products 테이블 조회
    }
}

// 새로운 FlexibleRepository
@Repository
public class FlexibleTargetRepository {
    public Map<String, Object> findByCode(String code) {
        // EAV 패턴으로 조회
    }
    
    public <T> T findByCode(String code, Class<T> type) {
        // EAV를 엔티티로 매핑
    }
}

// 전환기 Facade
@Service
public class ProductService {
    @Autowired
    private LegacyProductRepository legacy;
    
    @Autowired
    private FlexibleTargetRepository flexible;
    
    @Value("${migration.use-flexible:false}")
    private boolean useFlexible;
    
    public Product findByCode(String code) {
        if (useFlexible) {
            return flexible.findByCode(code, Product.class);
        }
        return legacy.findByCode(code);
    }
}
```

## ✅ 체크리스트

### Pre-Migration
- [ ] 전체 데이터베이스 백업
- [ ] 마이그레이션 스크립트 리뷰
- [ ] 테스트 환경 검증
- [ ] 롤백 계획 수립

### During Migration
- [ ] Phase 1: 스키마 생성 완료
- [ ] Phase 2: 데이터 마이그레이션 완료
- [ ] 데이터 일관성 검증
- [ ] 성능 테스트

### Post-Migration
- [ ] 애플리케이션 전환
- [ ] 모니터링 설정
- [ ] 성능 최적화
- [ ] 기존 테이블 제거 (선택적)

## 📚 관련 문서
- [00-overview.md](00-overview.md) - 전체 개요
- [01-target-domain.md](01-target-domain.md) - 타겟 도메인
- [06-implementation-guide.md](06-implementation-guide.md) - 구현 가이드
