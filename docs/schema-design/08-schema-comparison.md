# 📊 스키마 설계 비교 분석

## 🔄 개선 사항 요약

### 기존 스키마의 문제점
1. **도메인 종속성**: `products`, `sellers` 등 특정 비즈니스 도메인에 고정
2. **JSON 남용**: 구조화되지 않은 JSON 필드 과다 사용
3. **확장성 부족**: 새로운 데이터 타입 추가 시 테이블 생성 필요
4. **유연성 부족**: 속성 변경 시 스키마 변경 필요

### 새로운 스키마의 개선점
1. **범용성**: 모든 크롤링 대상을 `crawling_targets`로 추상화
2. **정규화**: JSON을 정규화된 테이블로 분리
3. **확장성**: EAV 패턴으로 무제한 속성 추가 가능
4. **유연성**: 런타임에 새로운 타입/속성 추가 가능

## 📋 상세 비교

### 1. 엔티티 모델링

#### 🔴 기존 방식 (도메인 특화)
```sql
-- 각 도메인마다 별도 테이블
CREATE TABLE products (
    id BIGINT PRIMARY KEY,
    seller_id BIGINT,
    product_code VARCHAR(100),
    name VARCHAR(500),
    price DECIMAL(15,2),
    -- 고정된 컬럼들...
    images JSON,  -- JSON 의존
    metadata JSON -- JSON 의존
);

CREATE TABLE sellers (
    id BIGINT PRIMARY KEY,
    site_id BIGINT,
    seller_code VARCHAR(100),
    -- 셀러 전용 컬럼들...
    metadata JSON -- JSON 의존
);
```

#### ✅ 새로운 방식 (범용적)
```sql
-- 모든 크롤링 대상을 하나의 테이블로
CREATE TABLE crawling_targets (
    id BIGINT PRIMARY KEY,
    source_id BIGINT,
    type_id BIGINT,  -- 타입으로 구분 (상품, 셀러, 뉴스 등)
    target_code VARCHAR(200),
    -- 공통 속성만 포함
);

-- 타입별 속성은 메타데이터로 정의
CREATE TABLE target_attributes (
    type_id BIGINT,
    attribute_code VARCHAR(100),
    data_type ENUM('STRING', 'NUMBER', 'DATE', ...),
    -- 속성 정의
);

-- 실제 값은 EAV 패턴으로 저장
CREATE TABLE crawling_data_values (
    data_id BIGINT,
    attribute_id BIGINT,
    value_text TEXT,
    value_number DECIMAL(20,6),
    -- 타입별 값 컬럼
);
```

### 2. 설정 관리

#### 🔴 기존 방식 (JSON 의존)
```sql
CREATE TABLE sites (
    crawling_config JSON,  -- 모든 설정을 JSON으로
    authentication_config JSON,
    -- {"headers": {...}, "timeout": 30000, ...}
);
```

#### ✅ 새로운 방식 (정규화)
```sql
-- 설정을 개별 테이블로 분리
CREATE TABLE crawling_headers (
    source_id BIGINT,
    header_name VARCHAR(100),
    header_value TEXT
);

CREATE TABLE crawling_rules (
    source_id BIGINT,
    rule_type ENUM('DELAY', 'RETRY', ...),
    rule_value VARCHAR(500)
);

CREATE TABLE source_auth_configs (
    source_id BIGINT,
    auth_type ENUM('BEARER', 'OAUTH', ...),
    auth_key VARCHAR(100),
    auth_value VARBINARY(1000)  -- 암호화
);
```

### 3. 데이터 타입 지원

#### 🔴 기존 방식
- 상품, 셀러만 지원
- 새로운 타입 추가 시 테이블 생성 필요
- ALTER TABLE로 컬럼 추가

#### ✅ 새로운 방식
```sql
-- 타입 추가는 INSERT만으로 가능
INSERT INTO target_types (type_code, type_name) VALUES 
('PRODUCT', '상품'),
('BRAND', '브랜드'),
('NEWS', '뉴스'),
('SOCIAL_POST', 'SNS 게시물'),
('DOCUMENT', '문서'),
-- 무제한 추가 가능...
```

## 🎯 실제 사용 예시

### 새로운 데이터 타입 추가 시나리오

#### 예시: 부동산 정보 크롤링 추가

```sql
-- 1. 타입 정의 (스키마 변경 없음)
INSERT INTO target_types (type_code, type_name) VALUES 
('REAL_ESTATE', '부동산');

-- 2. 속성 정의 (스키마 변경 없음)
SET @type_id = LAST_INSERT_ID();
INSERT INTO target_attributes (type_id, attribute_code, attribute_name, data_type) VALUES
(@type_id, 'address', '주소', 'STRING'),
(@type_id, 'price', '가격', 'NUMBER'),
(@type_id, 'area_sqm', '면적', 'NUMBER'),
(@type_id, 'rooms', '방 개수', 'NUMBER'),
(@type_id, 'floor', '층수', 'NUMBER'),
(@type_id, 'built_year', '건축연도', 'NUMBER');

-- 3. 크롤링 대상 추가
INSERT INTO crawling_targets (source_id, type_id, target_code, target_url) VALUES
(1, @type_id, 'APT_001', 'https://example.com/property/001');

-- 바로 사용 가능! 코드 변경 없음
```

## 📈 성능 비교

### Query 성능

#### 🔴 기존: JSON 필드 검색
```sql
-- JSON 검색은 느림 (인덱스 불가)
SELECT * FROM products 
WHERE JSON_EXTRACT(metadata, '$.brand') = 'Prada';
```

#### ✅ 개선: 인덱싱 가능
```sql
-- 인덱스 활용 가능
SELECT cd.* 
FROM crawling_data cd
JOIN crawling_data_values cdv ON cd.id = cdv.data_id
JOIN target_attributes ta ON cdv.attribute_id = ta.id
WHERE ta.attribute_code = 'brand' 
  AND cdv.value_text = 'Prada';  -- 인덱스 사용
```

## 🚀 마이그레이션 영향도

### 장점
1. **무중단 확장**: 새 타입 추가 시 다운타임 없음
2. **버전 관리**: 모든 데이터 버전 추적 가능
3. **유연한 스키마**: 속성 추가/삭제가 자유로움
4. **표준 SQL**: JSON 함수 의존도 감소

### 단점 및 대응
1. **복잡도 증가**: EAV 패턴은 JOIN이 많음
   - 해결: Materialized View, 캐싱 활용
2. **초기 학습**: 개발자 학습 곡선
   - 해결: ORM 레이어에서 추상화
3. **쿼리 복잡도**: 단순 조회도 JOIN 필요
   - 해결: View 생성, 헬퍼 함수 제공

## 📊 비교 매트릭스

| 항목 | 기존 스키마 | 새로운 스키마 |
|-----|-----------|------------|
| **확장성** | ❌ 테이블 추가 필요 | ✅ 데이터로 확장 |
| **유연성** | ❌ 스키마 변경 필요 | ✅ 런타임 변경 가능 |
| **정규화** | ❌ JSON 의존 | ✅ 완전 정규화 |
| **쿼리 성능** | ❌ JSON 검색 느림 | ✅ 인덱스 활용 |
| **타입 안정성** | ❌ JSON 타입 체크 불가 | ✅ 타입별 컬럼 |
| **복잡도** | ✅ 단순함 | ⚠️ EAV 복잡도 |
| **개발 속도** | ✅ 직관적 | ⚠️ 초기 학습 필요 |

## 💡 권장 사항

### 1. 하이브리드 접근
- **핵심 엔티티**: 자주 사용되는 것은 전용 테이블 유지
- **확장 영역**: 새로운/변동성 높은 것은 EAV 패턴 적용

### 2. 성능 최적화
```sql
-- 자주 조회되는 데이터는 Materialized View
CREATE MATERIALIZED VIEW mv_product_current AS
SELECT 
    t.id,
    t.target_code as product_code,
    MAX(CASE WHEN ta.attribute_code = 'name' THEN cdv.value_text END) as name,
    MAX(CASE WHEN ta.attribute_code = 'price' THEN cdv.value_number END) as price,
    MAX(CASE WHEN ta.attribute_code = 'brand' THEN cdv.value_text END) as brand
FROM crawling_targets t
JOIN crawling_data cd ON t.id = cd.target_id AND cd.is_current = TRUE
JOIN crawling_data_values cdv ON cd.id = cdv.data_id
JOIN target_attributes ta ON cdv.attribute_id = ta.id
WHERE t.type_id = (SELECT id FROM target_types WHERE type_code = 'PRODUCT')
GROUP BY t.id, t.target_code;
```

### 3. 애플리케이션 레이어
```java
// Repository 패턴으로 복잡도 숨기기
@Repository
public class FlexibleCrawlingRepository {
    
    public <T> T findByTargetCode(String code, Class<T> type) {
        // EAV를 도메인 객체로 자동 매핑
        return mapper.mapToEntity(
            getEavData(code), type
        );
    }
    
    public void save(Object entity) {
        // 도메인 객체를 EAV로 자동 변환
        saveAsEav(entity);
    }
}
```

## 🎬 결론

새로운 스키마는:
- ✅ **무한 확장 가능**: 어떤 데이터든 크롤링 가능
- ✅ **유지보수 용이**: 스키마 변경 없이 기능 추가
- ✅ **성능 개선**: JSON 대신 인덱싱 가능한 구조
- ⚠️ **복잡도 관리 필요**: 적절한 추상화 레이어 구축 필요

기존 스키마에서 점진적 마이그레이션을 통해 하이브리드 방식으로 운영하는 것을 추천합니다.
