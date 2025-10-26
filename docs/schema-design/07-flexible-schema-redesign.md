# 🔄 유연한 크롤링 시스템 스키마 재설계

## 📌 문제점 분석

### 기존 스키마의 한계
1. **도메인 종속적**: 상품/셀러에 특화되어 다른 데이터 타입 수용 어려움
2. **JSON 남용**: 구조화되지 않은 데이터로 쿼리/인덱싱 제약
3. **확장성 부족**: 새로운 크롤링 타겟 추가 시 스키마 변경 필요
4. **타입 안정성 부족**: JSON 필드는 타입 체크 불가능

## 🏗️ 새로운 설계 원칙

### 1. 범용 크롤링 대상 추상화
- 모든 크롤링 대상을 "Target"으로 추상화
- 타입별 속성은 EAV(Entity-Attribute-Value) 패턴 적용
- 메타데이터 기반 동적 스키마

### 2. 정규화 강화
- JSON 필드를 정규화된 테이블로 분리
- 관계형 데이터베이스의 장점 최대 활용
- 인덱싱과 쿼리 성능 개선

## 📊 새로운 스키마 구조

```mermaid
erDiagram
    crawling_sources {
        bigint id PK
        varchar name "사이트/소스 이름"
        varchar base_url
        varchar source_type "WEB, API, FILE"
        enum status
        timestamp created_at
    }
    
    target_types {
        bigint id PK
        varchar type_code "PRODUCT, BRAND, REVIEW, NEWS"
        varchar type_name
        varchar description
        boolean is_active
    }
    
    crawling_targets {
        bigint id PK
        bigint source_id FK
        bigint type_id FK
        varchar target_code "고유 식별자"
        varchar target_url
        varchar parent_target_id "계층 구조"
        enum status
        int crawl_priority
        int crawl_interval_hours
        timestamp last_crawled_at
        timestamp created_at
    }
    
    target_attributes {
        bigint id PK
        bigint type_id FK
        varchar attribute_code
        varchar attribute_name
        varchar data_type "STRING, NUMBER, DATE, BOOLEAN"
        boolean is_required
        int display_order
    }
    
    crawling_data {
        bigint id PK
        bigint target_id FK
        bigint job_id FK
        timestamp crawled_at
        varchar version_hash
        timestamp created_at
    }
    
    crawling_data_values {
        bigint data_id FK
        bigint attribute_id FK
        text value
        PRIMARY KEY(data_id, attribute_id)
    }
    
    crawling_sources ||--o{ crawling_targets : contains
    target_types ||--o{ crawling_targets : defines
    target_types ||--o{ target_attributes : has
    crawling_targets ||--o{ crawling_data : produces
    crawling_data ||--o{ crawling_data_values : contains
    target_attributes ||--o{ crawling_data_values : defines
```

## 📋 테이블 상세 설명

### 1. crawling_sources (크롤링 소스)
범용 크롤링 소스 정의
```sql
CREATE TABLE crawling_sources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    base_url VARCHAR(500),
    api_base_url VARCHAR(500),
    source_type ENUM('WEB', 'API', 'RSS', 'FILE') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    INDEX idx_sources_status (status, deleted_at)
);
```

### 2. target_types (대상 타입 정의)
크롤링 대상의 타입 정의
```sql
CREATE TABLE target_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_code VARCHAR(50) UNIQUE NOT NULL,
    type_name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_type_id BIGINT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_type_id) REFERENCES target_types(id),
    INDEX idx_types_active (is_active)
);
```

### 3. crawling_targets (크롤링 대상)
실제 크롤링할 대상
```sql
CREATE TABLE crawling_targets (
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
    FOREIGN KEY (source_id) REFERENCES crawling_sources(id),
    FOREIGN KEY (type_id) REFERENCES target_types(id),
    FOREIGN KEY (parent_target_id) REFERENCES crawling_targets(id),
    UNIQUE KEY uk_target (source_id, type_id, target_code),
    INDEX idx_targets_crawl (status, last_crawled_at, crawl_interval_hours),
    INDEX idx_targets_hierarchy (parent_target_id)
);
```

### 4. target_attributes (타겟 속성 정의)
각 타입별 속성 메타데이터
```sql
CREATE TABLE target_attributes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_id BIGINT NOT NULL,
    attribute_code VARCHAR(100) NOT NULL,
    attribute_name VARCHAR(200) NOT NULL,
    data_type ENUM('STRING', 'NUMBER', 'DATE', 'BOOLEAN', 'JSON', 'TEXT') NOT NULL,
    is_required BOOLEAN DEFAULT FALSE,
    is_indexed BOOLEAN DEFAULT FALSE,
    validation_rules TEXT,
    default_value TEXT,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (type_id) REFERENCES target_types(id),
    UNIQUE KEY uk_attribute (type_id, attribute_code),
    INDEX idx_attributes_type (type_id, display_order)
);
```

### 5. crawling_data (크롤링 데이터 버전)
크롤링된 데이터의 버전 관리
```sql
CREATE TABLE crawling_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    crawled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version_hash VARCHAR(64) NOT NULL,
    s3_path VARCHAR(500),
    is_current BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (target_id) REFERENCES crawling_targets(id),
    FOREIGN KEY (job_id) REFERENCES crawling_jobs(id),
    INDEX idx_data_target (target_id, is_current, crawled_at DESC),
    INDEX idx_data_version (version_hash)
);
```

### 6. crawling_data_values (크롤링 데이터 값)
실제 크롤링된 값 저장 (EAV)
```sql
CREATE TABLE crawling_data_values (
    data_id BIGINT NOT NULL,
    attribute_id BIGINT NOT NULL,
    value_text TEXT,
    value_number DECIMAL(20,6),
    value_date DATETIME,
    value_boolean BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (data_id, attribute_id),
    FOREIGN KEY (data_id) REFERENCES crawling_data(id) ON DELETE CASCADE,
    FOREIGN KEY (attribute_id) REFERENCES target_attributes(id),
    INDEX idx_values_attribute (attribute_id, data_id),
    INDEX idx_values_number (attribute_id, value_number),
    INDEX idx_values_date (attribute_id, value_date)
);
```

## 🔧 설정 테이블 정규화

### 크롤링 설정 분리
```sql
-- 소스별 인증 설정
CREATE TABLE source_auth_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT NOT NULL,
    auth_type ENUM('NONE', 'TOKEN', 'OAUTH', 'COOKIE', 'CUSTOM') NOT NULL,
    auth_key VARCHAR(100),
    auth_value TEXT, -- 암호화 저장
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (source_id) REFERENCES crawling_sources(id),
    INDEX idx_auth_source (source_id, auth_type)
);

-- HTTP 헤더 설정
CREATE TABLE crawling_headers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT NOT NULL,
    header_name VARCHAR(100) NOT NULL,
    header_value TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_id) REFERENCES crawling_sources(id),
    INDEX idx_headers_source (source_id, is_active)
);

-- 크롤링 규칙 설정
CREATE TABLE crawling_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT NOT NULL,
    rule_type ENUM('DELAY', 'RETRY', 'TIMEOUT', 'RATE_LIMIT') NOT NULL,
    rule_name VARCHAR(100),
    rule_value VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_id) REFERENCES crawling_sources(id),
    INDEX idx_rules_source (source_id, rule_type, is_active)
);
```

## 📊 사용 예시

### 1. 다양한 타입 지원
```sql
-- 타입 정의
INSERT INTO target_types (type_code, type_name) VALUES 
('PRODUCT', '상품'),
('BRAND', '브랜드'),
('REVIEW', '리뷰'),
('NEWS', '뉴스'),
('PRICE', '가격정보'),
('STOCK', '재고정보');

-- 상품 타입의 속성 정의
INSERT INTO target_attributes (type_id, attribute_code, attribute_name, data_type) VALUES
(1, 'name', '상품명', 'STRING'),
(1, 'price', '가격', 'NUMBER'),
(1, 'brand', '브랜드', 'STRING'),
(1, 'description', '설명', 'TEXT'),
(1, 'stock_count', '재고수량', 'NUMBER'),
(1, 'images', '이미지URL목록', 'JSON');

-- 브랜드 타입의 속성 정의
INSERT INTO target_attributes (type_id, attribute_code, attribute_name, data_type) VALUES
(2, 'brand_name', '브랜드명', 'STRING'),
(2, 'country', '원산지', 'STRING'),
(2, 'founded_year', '설립연도', 'NUMBER'),
(2, 'description', '브랜드 설명', 'TEXT');
```

### 2. 계층 구조 지원
```sql
-- MUSTIT > 셀러 > 상품 계층 구조
INSERT INTO crawling_targets (source_id, type_id, target_code, parent_target_id) VALUES
(1, 1, 'MUSTIT', NULL),                    -- 최상위
(1, 2, 'SELLER_001', 1),                   -- 셀러 (MUSTIT의 하위)
(1, 3, 'PRODUCT_001', 2);                  -- 상품 (셀러의 하위)
```

### 3. 데이터 조회
```sql
-- 특정 타겟의 최신 데이터 조회
SELECT 
    ta.attribute_name,
    COALESCE(cdv.value_text, 
             CAST(cdv.value_number AS CHAR), 
             CAST(cdv.value_date AS CHAR),
             CAST(cdv.value_boolean AS CHAR)) as value
FROM crawling_data cd
JOIN crawling_data_values cdv ON cd.id = cdv.data_id
JOIN target_attributes ta ON cdv.attribute_id = ta.id
WHERE cd.target_id = ? 
  AND cd.is_current = TRUE
ORDER BY ta.display_order;
```

## 🎯 장점

### 1. 유연성
- 새로운 크롤링 타입 추가 시 스키마 변경 불필요
- 타입별 속성을 동적으로 정의 가능
- 계층 구조 지원으로 복잡한 관계 표현

### 2. 확장성
- EAV 패턴으로 무제한 속성 추가 가능
- 다양한 데이터 타입 지원
- 버전 관리로 이력 추적

### 3. 정규화
- JSON 필드 최소화
- 인덱싱 가능한 구조
- 쿼리 성능 개선

### 4. 타입 안정성
- 데이터 타입별 컬럼 분리
- 유효성 검사 규칙 적용 가능
- 스키마 레벨 제약조건

## 🚀 마이그레이션 전략

### 기존 데이터 이전
```sql
-- 기존 products 테이블 데이터를 새 구조로 이전
INSERT INTO crawling_targets (source_id, type_id, target_code, target_name)
SELECT 
    s.id,
    (SELECT id FROM target_types WHERE type_code = 'PRODUCT'),
    p.product_code,
    p.name
FROM products p
JOIN sellers sel ON p.seller_id = sel.id
JOIN sites s ON sel.site_id = s.id;

-- 상품 속성 데이터 이전
INSERT INTO crawling_data_values (data_id, attribute_id, value_text)
SELECT 
    cd.id,
    ta.id,
    CASE ta.attribute_code
        WHEN 'name' THEN p.name
        WHEN 'brand' THEN p.brand
        WHEN 'description' THEN p.description
    END
FROM products p
-- ... (조인 및 매핑 로직)
```

## 📝 성능 고려사항

### 1. 인덱싱 전략
- EAV 조회 최적화를 위한 복합 인덱스
- 자주 조회되는 속성은 Materialized View 고려
- 파티셔닝으로 대용량 처리

### 2. 캐싱
- 타입/속성 메타데이터는 애플리케이션 레벨 캐싱
- 자주 변경되지 않는 데이터는 Redis 캐싱

### 3. 집계 최적화
- 통계용 요약 테이블 별도 관리
- 배치 작업으로 사전 계산
