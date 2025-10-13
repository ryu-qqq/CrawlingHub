# CRAW-79: 데이터베이스 스키마 설계 및 마이그레이션

## 📋 개요

User-Agent 기반 토큰 관리 및 분산 레이트 리미팅 시스템을 위한 데이터베이스 스키마 구축

**Epic**: CRAW-78 - 토큰 & 레이트 리미팅 시스템
**브랜치**: `feature/CRAW-79-database-schema-migration`

## 🎯 목표

- 7개 테이블 생성 및 데이터베이스 스키마 구축
- 100개 User-Agent 초기 데이터 Seed
- 월별 파티셔닝 및 인덱스 최적화
- Forward/Rollback 마이그레이션 스크립트 작성

## 📊 생성된 테이블

### 1. user_agent_pool
User-Agent 문자열 및 상태 관리 (100개 동시 운영)

**주요 필드**:
- `agent_id`: User-Agent 고유 ID
- `user_agent`: User-Agent 문자열 (UNIQUE)
- `is_active`: 활성화 여부
- `is_blocked`: 차단 여부 (429 에러 발생 시)
- `blocked_until`: 차단 해제 시각
- `usage_count`, `success_count`, `failure_count`: 통계

**인덱스**:
- `idx_active_unblocked`: (is_active, is_blocked, blocked_until)
- `idx_last_used`: (last_used_at)
- `idx_usage_stats`: (usage_count, success_count, failure_count)

### 2. user_agent_token
User-Agent별 토큰 정보 및 라이프사이클 관리

**주요 필드**:
- `token_id`: 토큰 고유 ID
- `agent_id`: user_agent_pool 참조
- `token_value`: 토큰 값 (UNIQUE, 암호화 저장 권장)
- `issued_at`, `expires_at`: 토큰 발급/만료 시각
- `refresh_count`: 갱신 횟수

**인덱스**:
- `idx_user_agent_active`: (agent_id, is_active)
- `idx_expires`: (expires_at, is_active)

### 3. token_usage_log (월별 파티셔닝 적용)
토큰 사용 이력 및 레이트 리미팅 추적

**주요 필드**:
- `log_id`: 로그 고유 ID
- `agent_id`, `token_id`: 참조 키
- `http_status_code`, `response_time_ms`: 응답 정보
- `is_rate_limited`, `is_429_error`: 레이트 리미팅 플래그
- `request_timestamp`: 요청 시각 (파티션 키)

**파티셔닝**:
- 월별 Range 파티셔닝 (2025-01 ~ 2025-12 + future)
- `PARTITION BY RANGE (YEAR(request_timestamp) * 100 + MONTH(request_timestamp))`

**인덱스**:
- `idx_user_agent_time`: (agent_id, request_timestamp) - 복합 인덱스
- `idx_429_errors`: (is_429_error, agent_id, request_timestamp) - 커버링 인덱스
- `idx_rate_limit`: (is_rate_limited, request_timestamp)
- `idx_success_time`: (is_success, request_timestamp)
- `idx_http_status`: (http_status_code, request_timestamp)

### 4. circuit_breaker_state
Circuit Breaker 패턴 구현 - 429 연속 발생 시 자동 차단

**주요 필드**:
- `state_id`: 상태 고유 ID
- `agent_id`: user_agent_pool 참조 (UNIQUE)
- `circuit_state`: CLOSED, OPEN, HALF_OPEN
- `failure_count`: 연속 실패 횟수
- `timeout_duration_seconds`: Circuit OPEN 유지 시간 (기본 10분)
- `failure_threshold`: 실패 임계값 (기본 3)

**인덱스**:
- `idx_opened`: (opened_at)
- `idx_state_failure`: (circuit_state, failure_count)

### 5. circuit_breaker_event
Circuit Breaker 상태 전환 이벤트 추적

**주요 필드**:
- `event_id`: 이벤트 고유 ID
- `agent_id`: user_agent_pool 참조
- `event_type`: STATE_CHANGE, FAILURE, SUCCESS
- `from_state`, `to_state`: 상태 전환
- `event_timestamp`: 이벤트 발생 시각

**인덱스**:
- `idx_user_agent_time`: (agent_id, event_timestamp)
- `idx_event_type`: (event_type, event_timestamp)
- `idx_state_change`: (from_state, to_state, event_timestamp)

### 6. rate_limit_bucket
Token Bucket 알고리즘 상태 (Redis 백업용)

**주요 필드**:
- `bucket_id`: Bucket 고유 ID
- `agent_id`: user_agent_pool 참조 (UNIQUE)
- `max_tokens`: 최대 토큰 수 (기본 80)
- `current_tokens`: 현재 토큰 수
- `refill_rate`: 토큰 재충전 속도 (tokens/second)
- `last_refill_at`: 마지막 재충전 시각

**인덱스**:
- `idx_last_refill`: (last_refill_at)
- `idx_token_level`: (current_tokens, last_refill_at)

### 7. token_refresh_schedule
토큰 자동 갱신 스케줄 관리

**주요 필드**:
- `schedule_id`: 스케줄 고유 ID
- `agent_id`, `token_id`: 참조 키 (UNIQUE 복합키)
- `next_refresh_time`: 다음 갱신 예정 시각
- `refresh_interval_seconds`: 갱신 주기 (기본 1시간)
- `consecutive_failures`: 연속 실패 횟수

**인덱스**:
- `idx_next_refresh`: (is_enabled, next_refresh_time) - 최적화된 순서
- `idx_agent_enabled`: (agent_id, is_enabled)
- `idx_failure_count`: (consecutive_failures, next_refresh_time)

## 📁 파일 구조

```
adapter/adapter-out-persistence-jpa/src/main/resources/db/migration/
├── V7__create_token_rate_limiting_tables.sql  # 7개 테이블 생성
└── V8__seed_user_agents.sql                   # 100개 User-Agent Seed 데이터

scripts/db/
├── generate_user_agents.py                    # User-Agent 생성 스크립트
├── add_partition_202601.sql                   # 파티션 추가 예시 (2026-01)
└── rollback_v7_v8.sql                         # 롤백 스크립트
```

## 🚀 마이그레이션 실행

### Forward Migration (Flyway 자동 실행)

```bash
# Gradle 빌드 시 자동 실행
./gradlew clean build

# 또는 Flyway 명령어로 직접 실행
./gradlew flywayMigrate
```

### Rollback (수동 실행 필요)

```bash
# MySQL 접속
mysql -u username -p database_name

# 롤백 스크립트 실행
source scripts/db/rollback_v7_v8.sql;
```

## 📈 성능 최적화

### 인덱스 전략

1. **복합 인덱스**: 자주 함께 조회되는 컬럼 조합
   - `(is_active, is_blocked, blocked_until)`: User-Agent 활성/차단 상태 조회
   - `(agent_id, request_timestamp)`: 시계열 로그 조회
   - `(is_enabled, next_refresh_time)`: 스케줄 조회 최적화

2. **커버링 인덱스**: SELECT 쿼리가 인덱스만으로 처리 가능
   - `idx_429_errors (is_429_error, agent_id, request_timestamp)`

3. **인덱스 크기 최적화**: VARCHAR 필드는 prefix index 사용
   - `UNIQUE KEY uk_user_agent (user_agent(255))`

4. **중복 인덱스 제거** (Gemini Code Assist 리뷰 반영):
   - `token_usage_log`: idx_agent_id, idx_token_id 제거 (복합 인덱스로 커버됨)
   - `circuit_breaker_state`: idx_user_agent_state 제거 (UNIQUE 키로 충분)
   - `circuit_breaker_event`: idx_agent_id 제거 (복합 인덱스로 커버됨)

5. **컬럼 최적화**:
   - `created_at` 컬럼 제거: `request_timestamp`, `event_timestamp`로 대체하여 스토리지 절약

### 파티셔닝 전략

**token_usage_log 테이블 월별 파티셔닝**:
- 대용량 로그 데이터 효율적 관리
- 과거 파티션 아카이빙/삭제 용이
- 월별 쿼리 성능 향상

**파티션 추가 방법**:
```sql
-- 매월 실행하여 다음 달 파티션 생성
ALTER TABLE token_usage_log
    REORGANIZE PARTITION p_future INTO (
        PARTITION p202602 VALUES LESS THAN (202603),
        PARTITION p_future VALUES LESS THAN MAXVALUE
    );
```

## 🔧 운영 가이드

### User-Agent 추가

```sql
-- 새로운 User-Agent 추가
INSERT INTO user_agent_pool (user_agent, is_active, is_blocked, usage_count, success_count, failure_count)
VALUES ('Mozilla/5.0 ...', TRUE, FALSE, 0, 0, 0);
```

### 차단된 User-Agent 복구

```sql
-- Circuit Breaker OPEN → CLOSED
UPDATE circuit_breaker_state
SET circuit_state = 'CLOSED',
    failure_count = 0,
    closed_at = NOW()
WHERE agent_id = ? AND circuit_state = 'OPEN';

-- User-Agent 차단 해제
UPDATE user_agent_pool
SET is_blocked = FALSE,
    blocked_until = NULL
WHERE agent_id = ?;
```

### 파티션 관리

```sql
-- 과거 파티션 삭제 (3개월 이상 된 데이터)
ALTER TABLE token_usage_log DROP PARTITION p202201;

-- 파티션 상태 확인
SELECT
    PARTITION_NAME,
    PARTITION_DESCRIPTION,
    TABLE_ROWS,
    DATA_LENGTH / 1024 / 1024 AS 'Size (MB)'
FROM INFORMATION_SCHEMA.PARTITIONS
WHERE TABLE_NAME = 'token_usage_log'
ORDER BY PARTITION_ORDINAL_POSITION;
```

## 📊 모니터링 쿼리

### User-Agent 통계

```sql
-- User-Agent별 사용 통계
SELECT
    agent_id,
    LEFT(user_agent, 50) AS user_agent_preview,
    is_active,
    is_blocked,
    usage_count,
    success_count,
    failure_count,
    ROUND(success_count * 100.0 / NULLIF(usage_count, 0), 2) AS success_rate
FROM user_agent_pool
ORDER BY usage_count DESC
LIMIT 20;
```

### 레이트 리미팅 현황

```sql
-- 최근 1시간 429 에러 발생 현황
SELECT
    agent_id,
    COUNT(*) AS error_count,
    MAX(request_timestamp) AS last_error_time
FROM token_usage_log
WHERE
    is_429_error = TRUE
    AND request_timestamp >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
GROUP BY agent_id
ORDER BY error_count DESC;
```

### Circuit Breaker 상태

```sql
-- Circuit Breaker 상태 모니터링
SELECT
    cbs.agent_id,
    LEFT(uap.user_agent, 50) AS user_agent_preview,
    cbs.circuit_state,
    cbs.failure_count,
    cbs.opened_at,
    cbs.timeout_duration_seconds
FROM circuit_breaker_state cbs
JOIN user_agent_pool uap ON cbs.agent_id = uap.agent_id
WHERE cbs.circuit_state != 'CLOSED'
ORDER BY cbs.opened_at DESC;
```

## ✅ 완료 조건 검증

### 1. 테이블 생성 확인

```sql
SHOW TABLES LIKE '%user_agent%' OR LIKE '%token%' OR LIKE '%circuit%' OR LIKE '%rate_limit%';
```

예상 결과: 7개 테이블
- user_agent_pool
- user_agent_token
- token_usage_log
- circuit_breaker_state
- circuit_breaker_event
- rate_limit_bucket
- token_refresh_schedule

### 2. Seed 데이터 확인

```sql
SELECT COUNT(*) AS user_agent_count FROM user_agent_pool;
```

예상 결과: 100개

### 3. 인덱스 확인

```sql
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS indexed_columns
FROM INFORMATION_SCHEMA.STATISTICS
WHERE
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME IN (
        'user_agent_pool',
        'user_agent_token',
        'token_usage_log',
        'circuit_breaker_state',
        'circuit_breaker_event',
        'rate_limit_bucket',
        'token_refresh_schedule'
    )
GROUP BY TABLE_NAME, INDEX_NAME
ORDER BY TABLE_NAME, INDEX_NAME;
```

### 4. 파티션 확인

```sql
SELECT
    PARTITION_NAME,
    PARTITION_DESCRIPTION,
    TABLE_ROWS
FROM INFORMATION_SCHEMA.PARTITIONS
WHERE
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'token_usage_log'
ORDER BY PARTITION_ORDINAL_POSITION;
```

예상 결과: 13개 파티션 (2025-01 ~ 2025-12 + p_future)

## 🔗 관련 문서

- Epic 문서: https://www.notion.so/Epic2-288b00296f3b80e6b25ce58aeec963ee
- Jira 이슈: CRAW-79

## 📝 다음 단계

1. **도메인 엔티티 생성** (CRAW-80)
   - JPA Entity 클래스 구현
   - Repository 인터페이스 정의

2. **토큰 관리 서비스 구현** (CRAW-81)
   - User-Agent Pool 관리
   - 토큰 발급/갱신 로직

3. **레이트 리미팅 구현** (CRAW-82)
   - Token Bucket 알고리즘
   - Circuit Breaker 패턴

4. **Redis 통합** (CRAW-83)
   - 분산 레이트 리미팅
   - 토큰 캐싱
