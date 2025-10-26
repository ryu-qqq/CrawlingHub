# 🏗️ 범용 크롤링 시스템 데이터베이스 스키마 설계

## 📌 개요

**EAV 패턴 기반의 범용 크롤링 플랫폼** - 스키마 변경 없이 모든 타입의 데이터를 수집할 수 있는 유연한 시스템입니다.

### 🔄 패러다임 전환
- **기존**: 상품/셀러 중심의 고정된 스키마
- **신규**: EAV 패턴으로 무한 확장 가능한 범용 스키마
- **이점**: 코드 변경 없이 새로운 데이터 타입 추가 가능

### 🎯 핵심 특징
- **무한 확장성**: 상품, 뉴스, SNS, 부동산 등 모든 데이터 타입 지원
- **동적 타입 시스템**: 런타임에 새로운 타입과 속성 추가
- **완전 정규화**: JSON 최소화, 인덱싱 가능한 구조
- **버전 관리**: 모든 데이터의 변경 이력 추적
- **타입 안정성**: 데이터 타입별 분리된 저장

## 📂 문서 구조

```
docs/schema-design/
├── README.md                      # 👈 현재 문서
├── 00-overview.md                 # 범용 시스템 아키텍처 및 EAV 패턴
├── 01-target-domain.md           # 타겟 도메인 (EAV 코어)
├── 02-workflow-domain.md         # 워크플로우 도메인
├── 03-security-domain.md         # 보안 도메인
├── 04-monitoring-domain.md       # 모니터링 도메인  
├── 05-migration-guide.md         # 마이그레이션 가이드
├── 06-implementation-guide.md    # 구현 가이드
├── 07-flexible-schema-redesign.md # 유연한 스키마 재설계
└── 08-schema-comparison.md       # 기존 vs 신규 비교
```

## 🏗️ 데이터베이스 구조

### 🔄 EAV (Entity-Attribute-Value) 패턴

```sql
-- Entity: 크롤링 대상
crawling_targets (id, source_id, type_id, target_code)
    ↓
-- Attribute: 타입별 속성 정의  
target_attributes (id, type_id, attribute_code, data_type)
    ↓
-- Value: 실제 데이터 값
crawling_data_values (data_id, attribute_id, value_text, value_number, ...)
```

### 4개 핵심 도메인

#### 1️⃣ **타겟 도메인** (6개 핵심 테이블)
- `crawling_sources`: 크롤링 소스 (사이트, API)
- `target_types`: 데이터 타입 정의
- `target_attributes`: 타입별 속성 메타데이터
- `crawling_targets`: 크롤링 대상
- `crawling_data`: 데이터 버전 관리
- `crawling_data_values`: 실제 데이터 값 (EAV)

#### 2️⃣ **워크플로우 도메인** (7개 테이블)
- 동적 워크플로우 정의 및 실행
- 스케줄링 및 작업 큐 관리
- **핵심 테이블**: `crawling_workflows`, `crawling_jobs`, `crawling_queues`

#### 3️⃣ **보안 도메인** (8개 테이블)
- User-Agent 로테이션 전략
- 토큰 버킷 레이트 리미터
- 인증 토큰 및 프록시 관리
- **핵심 테이블**: `user_agents`, `token_buckets`, `source_auth_tokens`

#### 4️⃣ **모니터링 도메인** (10개 테이블)
- 실시간 메트릭 및 알림
- 데이터 품질 검증
- 대시보드 지원
- **핵심 테이블**: `crawling_metrics`, `data_quality_metrics`, `alert_configurations`

## 🚀 빠른 시작

### 1. 데이터베이스 생성
```sql
CREATE DATABASE crawling_hub 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### 2. 새로운 유연한 스키마 적용
```bash
# 범용 스키마 마이그레이션
flyway migrate -url=jdbc:mysql://localhost:3306/crawling_hub
```

### 3. 새로운 데이터 타입 추가 (런타임)
```sql
-- 예: 부동산 크롤링 추가 (스키마 변경 없음!)
INSERT INTO target_types (type_code, type_name) VALUES ('REAL_ESTATE', '부동산');

-- 속성 정의
INSERT INTO target_attributes (type_id, attribute_code, attribute_name, data_type) VALUES
(@type_id, 'address', '주소', 'STRING'),
(@type_id, 'price', '가격', 'NUMBER'),
(@type_id, 'area', '면적', 'NUMBER');

-- 바로 사용 가능!
```

## 💡 주요 설계 결정

### 1. EAV 패턴 채택
- **이유**: 스키마 변경 없이 무한 확장 가능
- **구현**: Entity(targets) - Attribute(metadata) - Value(data)
- **장점**: 런타임 타입 추가, 동적 속성 관리

### 2. JSON 최소화
- **기존**: 많은 JSON 컬럼으로 유연성 확보
- **신규**: 정규화된 테이블로 분리
- **이점**: 인덱싱 가능, 타입 안정성, 쿼리 성능

### 3. 타입별 값 분리
```sql
-- 데이터 타입별 별도 컬럼
value_text TEXT,           -- 문자열
value_number DECIMAL(20,6), -- 숫자
value_date DATETIME,        -- 날짜  
value_boolean BOOLEAN       -- 불린
```

### 4. 버전 관리
- **모든 데이터 버전 추적**: `crawling_data` 테이블
- **변경 감지**: `version_hash`로 데이터 변경 추적
- **이력 보존**: 이전 버전 데이터 유지

## 📊 성능 최적화

### 인덱싱 전략
- **복합 인덱스**: 자주 함께 조회되는 컬럼 조합
- **Covering Index**: SELECT 절의 모든 컬럼 포함
- **부분 인덱스**: WHERE 조건 활용 (MySQL 8.0+)

### 최적화 전략
```sql
-- EAV 조회 최적화
CREATE INDEX idx_values_lookup 
ON crawling_data_values(attribute_id, data_id);

-- Materialized View for 자주 조회되는 데이터
CREATE VIEW v_current_products AS
SELECT /* EAV를 일반 테이블처럼 변환 */
```

## 🔐 보안 고려사항

### 1. 암호화
- 프록시 인증 정보: AES 암호화
- 비회원 토큰: 안전한 저장 및 관리

### 2. 접근 제어
- 애플리케이션 계정: 필요 최소 권한
- 읽기 전용 계정: 모니터링/리포팅용

### 3. SQL Injection 방지
- Prepared Statement 사용
- 파라미터 바인딩

## 📈 모니터링 대시보드

### 핵심 메트릭
- **크롤링 성공률**: 시간별, 일별, 셀러별
- **응답 시간**: P50, P95, P99 백분위
- **데이터 품질**: 완전성, 정확성, 신선도
- **시스템 상태**: CPU, 메모리, 디스크 사용률

### 알림 설정
- 높은 실패율 (성공률 < 50%)
- 느린 응답 시간 (P95 > 5초)
- 낮은 데이터 품질 (완전성 < 80%)

## 📊 지원 가능한 데이터 타입

### 현재 지원
- **전자상거래**: 상품, 리뷰, 셀러, 가격, 재고
- **뉴스/미디어**: 기사, 블로그, 동영상
- **SNS**: 포스트, 댓글, 팔로워
- **금융**: 주가, 환율, 암호화폐
- **부동산**: 매물, 시세, 거래
- **날씨/환경**: 기온, 습도, 미세먼지
- ... 무제한 확장 가능

## 🛠️ 마이그레이션 상태

### Phase 1: 신규 스키마
- ✅ **V10**: 범용 타겟 도메인 테이블
- ✅ **V11**: 초기 타입 및 데이터
- 📝 **V12-V15**: 워크플로우, 보안, 모니터링

### Phase 2: 데이터 이전
- 📝 **V16**: 기존 sites → crawling_sources
- 📝 **V17**: 기존 products → crawling_targets + EAV
- 📝 **V18-V20**: 나머지 데이터 마이그레이션

## 🤝 기여 가이드

### 스키마 변경 시
1. 설계 문서 업데이트 (`docs/schema-design/`)
2. Flyway 마이그레이션 스크립트 생성
3. JPA 엔티티 업데이트
4. 테스트 코드 작성

### 명명 규칙
- **테이블**: 소문자, 언더스코어, 복수형
- **컬럼**: 소문자, 언더스코어
- **인덱스**: `idx_{table}_{columns}`
- **외래키**: `fk_{table}_{reference}`

## 📚 참고 자료

### 내부 문서
- [전체 개요](00-overview.md)
- [구현 가이드](06-implementation-guide.md)

### 외부 참고
- [MySQL 8.0 Reference](https://dev.mysql.com/doc/refman/8.0/en/)
- [Flyway Documentation](https://flywaydb.org/)
- [Spring Data JPA Guide](https://spring.io/projects/spring-data-jpa)

## 🎯 핵심 장점

### 개발 측면
- ✅ **무한 확장**: 스키마 변경 없이 새 타입 추가
- ✅ **빠른 대응**: 배포 없이 새로운 크롤링 대상 추가
- ✅ **유연한 구조**: 타입별 독립적 속성 관리

### 운영 측면
- ✅ **무중단 확장**: 런타임 타입 추가
- ✅ **버전 관리**: 모든 데이터 변경 이력
- ✅ **성능 최적화**: Materialized View, 인덱싱 전략

### 비즈니스 측면
- ✅ **신속한 서비스 확장**: 새로운 도메인 즉시 지원
- ✅ **비용 절감**: 스키마 변경 비용 제로
- ✅ **데이터 통합**: 다양한 소스 통합 관리

## 💬 문의

질문이나 개선 제안이 있으시면 이슈를 등록해 주세요.

---
*Last Updated: 2024-01-21*
*Version: 2.0.0 - EAV Pattern Redesign*
