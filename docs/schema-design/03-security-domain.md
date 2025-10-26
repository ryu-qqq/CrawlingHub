# 🔐 보안 도메인 (Security Domain) 스키마 설계

## 📌 개요

보안 도메인은 크롤링 시 차단을 우회하고 안정적인 데이터 수집을 보장하는 시스템입니다.
User-Agent 관리, 레이트 리미팅, 인증 토큰 관리, 프록시 로테이션 등을 담당합니다.

### 핵심 기능
- **User-Agent 로테이션**: 다양한 브라우저/디바이스로 위장
- **토큰 버킷**: 요청 속도 제한
- **인증 관리**: 비회원/회원 토큰 관리
- **프록시 관리**: IP 로테이션
- **패턴 감지**: 차단 패턴 학습 및 회피

## 📊 ERD (Entity Relationship Diagram)

```mermaid
erDiagram
    user_agents {
        bigint id PK
        varchar(500) user_agent_string "UA 문자열"
        varchar(50) browser_name
        varchar(20) browser_version
        varchar(50) os_name
        varchar(20) os_version
        enum device_type "DESKTOP, MOBILE, TABLET"
        enum status "ACTIVE, BLOCKED, RETIRED"
        int weight "가중치 1-100"
        timestamp last_used_at
        bigint total_requests
        bigint success_count
        bigint blocked_count
        float success_rate
        timestamp created_at
        timestamp updated_at
        timestamp deleted_at
    }
    
    ua_rotation_pools {
        bigint id PK
        bigint source_id FK
        varchar(100) pool_name
        enum rotation_strategy "RANDOM, ROUND_ROBIN, WEIGHTED"
        int min_interval_ms "최소 간격"
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }
    
    ua_pool_members {
        bigint pool_id FK
        bigint user_agent_id FK
        int weight "가중치"
        boolean is_active
        PRIMARY KEY(pool_id, user_agent_id)
    }
    
    token_buckets {
        bigint id PK
        bigint source_id FK
        bigint user_agent_id FK
        varchar(100) bucket_key "버킷 식별자"
        enum bucket_type "PER_SECOND, PER_MINUTE, PER_HOUR"
        int capacity "버킷 용량"
        float current_tokens "현재 토큰"
        float refill_rate "초당 충전량"
        timestamp last_refill_at
        timestamp window_start_at
        bigint requests_in_window
        timestamp created_at
        timestamp updated_at
    }
    
    source_auth_tokens {
        bigint id PK
        bigint source_id FK
        enum token_type "GUEST, MEMBER, API_KEY, OAUTH"
        varchar(100) token_name
        text token_value "암호화된 토큰"
        varchar(100) token_location "HEADER, COOKIE, QUERY"
        varchar(100) header_name "헤더명"
        enum status "ACTIVE, EXPIRED, REVOKED"
        timestamp issued_at
        timestamp expires_at
        timestamp last_used_at
        bigint usage_count
        timestamp created_at
        timestamp updated_at
    }
    
    ip_rotations {
        bigint id PK
        varchar(45) ip_address
        int port
        enum proxy_type "HTTP, HTTPS, SOCKS5"
        varchar(100) location "지역"
        enum status "ACTIVE, INACTIVE, BLACKLISTED"
        int response_time_ms
        float success_rate
        timestamp last_used_at
        bigint total_requests
        timestamp created_at
        timestamp updated_at
    }
    
    request_logs {
        bigint id PK
        bigint source_id FK
        bigint target_id FK
        bigint user_agent_id FK
        bigint ip_rotation_id FK
        varchar(1000) request_url
        varchar(10) http_method
        int status_code
        int response_time_ms
        bigint response_size_bytes
        boolean is_blocked
        text error_message
        text request_headers
        timestamp requested_at
        timestamp created_at
    }
    
    blocking_patterns {
        bigint id PK
        bigint source_id FK
        varchar(100) pattern_name
        enum pattern_type "RATE_LIMIT, CAPTCHA, IP_BLOCK, UA_BLOCK"
        text detection_rule "감지 규칙"
        text response_pattern "응답 패턴"
        enum action "RETRY, CHANGE_UA, CHANGE_IP, DELAY, STOP"
        int occurrence_count
        timestamp first_detected_at
        timestamp last_detected_at
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }
    
    security_policies {
        bigint id PK
        bigint source_id FK
        varchar(100) policy_name
        enum policy_type "RATE_LIMIT, RETRY, ROTATION"
        int priority "우선순위"
        text policy_rules "정책 규칙 JSON"
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }
    
    user_agents ||--o{ ua_pool_members : belongs_to
    ua_rotation_pools ||--o{ ua_pool_members : contains
    user_agents ||--o{ token_buckets : limited_by
    user_agents ||--o{ request_logs : uses
    ip_rotations ||--o{ request_logs : uses
    crawling_sources ||--o{ source_auth_tokens : has
    crawling_sources ||--o{ blocking_patterns : detected_for
    crawling_sources ||--o{ security_policies : configured_for
```

## 📝 테이블 상세 설명

### 1. user_agents (User-Agent 관리)

다양한 브라우저와 디바이스 User-Agent 문자열 관리

#### 주요 컬럼
- **device_type**: 디바이스 유형
  - `DESKTOP`: 데스크톱 브라우저
  - `MOBILE`: 모바일 브라우저
  - `TABLET`: 태블릿 브라우저
- **weight**: 선택 가중치 (높을수록 자주 선택)
- **success_rate**: 성공률 기반 자동 조정

#### User-Agent 예시
```sql
INSERT INTO user_agents (user_agent_string, browser_name, device_type, weight) VALUES
('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'Chrome', 'DESKTOP', 80),
('Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)', 'Safari', 'MOBILE', 60),
('Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', 'Safari', 'DESKTOP', 70);
```

### 2. ua_rotation_pools (UA 로테이션 풀)

소스별 User-Agent 로테이션 전략 관리

#### 로테이션 전략
- **RANDOM**: 무작위 선택
- **ROUND_ROBIN**: 순차적 순환
- **WEIGHTED**: 가중치 기반 선택

### 3. token_buckets (토큰 버킷)

레이트 리미팅을 위한 토큰 버킷 알고리즘 구현

#### 버킷 타입
- **PER_SECOND**: 초당 제한
- **PER_MINUTE**: 분당 제한
- **PER_HOUR**: 시간당 제한

#### 토큰 버킷 로직
```sql
-- 토큰 소비 및 충전
DELIMITER //
CREATE PROCEDURE consume_token(
    IN p_bucket_id BIGINT,
    IN p_tokens_needed FLOAT,
    OUT p_allowed BOOLEAN
)
BEGIN
    DECLARE v_current_tokens FLOAT;
    DECLARE v_capacity INT;
    DECLARE v_refill_rate FLOAT;
    DECLARE v_last_refill TIMESTAMP;
    DECLARE v_time_passed FLOAT;
    DECLARE v_tokens_to_add FLOAT;
    
    -- 현재 상태 조회
    SELECT current_tokens, capacity, refill_rate, last_refill_at
    INTO v_current_tokens, v_capacity, v_refill_rate, v_last_refill
    FROM token_buckets
    WHERE id = p_bucket_id
    FOR UPDATE;
    
    -- 토큰 충전 계산
    SET v_time_passed = TIMESTAMPDIFF(MICROSECOND, v_last_refill, NOW()) / 1000000.0;
    SET v_tokens_to_add = v_time_passed * v_refill_rate;
    SET v_current_tokens = LEAST(v_capacity, v_current_tokens + v_tokens_to_add);
    
    -- 토큰 소비 가능 여부 확인
    IF v_current_tokens >= p_tokens_needed THEN
        SET p_allowed = TRUE;
        SET v_current_tokens = v_current_tokens - p_tokens_needed;
    ELSE
        SET p_allowed = FALSE;
    END IF;
    
    -- 상태 업데이트
    UPDATE token_buckets
    SET current_tokens = v_current_tokens,
        last_refill_at = NOW(),
        requests_in_window = requests_in_window + 1
    WHERE id = p_bucket_id;
END//
DELIMITER ;
```

### 4. source_auth_tokens (인증 토큰)

소스별 인증 토큰 관리

#### 토큰 타입
- **GUEST**: 비회원 토큰
- **MEMBER**: 회원 토큰
- **API_KEY**: API 키
- **OAUTH**: OAuth 토큰

#### MUSTIT 비회원 토큰 예시
```sql
INSERT INTO source_auth_tokens (source_id, token_type, token_name, token_location, header_name) VALUES
(@mustit_id, 'GUEST', '비회원 액세스 토큰', 'HEADER', 'X-Guest-Token');
```

### 5. ip_rotations (IP 로테이션)

프록시 서버 관리 및 IP 로테이션

#### 프록시 선택 로직
```sql
-- 최적 프록시 선택
SELECT * FROM ip_rotations
WHERE status = 'ACTIVE'
  AND success_rate > 0.7
  AND last_used_at < DATE_SUB(NOW(), INTERVAL 1 MINUTE)
ORDER BY success_rate DESC, response_time_ms ASC
LIMIT 1;
```

### 6. request_logs (요청 로그)

모든 HTTP 요청 로깅 (감사 및 분석용)

#### 파티셔닝
```sql
-- 일별 파티셔닝
ALTER TABLE request_logs
PARTITION BY RANGE (TO_DAYS(requested_at)) (
    PARTITION p_20240120 VALUES LESS THAN (TO_DAYS('2024-01-21')),
    PARTITION p_20240121 VALUES LESS THAN (TO_DAYS('2024-01-22')),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

### 7. blocking_patterns (차단 패턴)

차단 패턴 감지 및 대응 전략

#### 패턴 타입
- **RATE_LIMIT**: 속도 제한 감지
- **CAPTCHA**: 캡차 출현
- **IP_BLOCK**: IP 차단
- **UA_BLOCK**: User-Agent 차단

#### 대응 액션
- **RETRY**: 재시도
- **CHANGE_UA**: User-Agent 변경
- **CHANGE_IP**: IP 변경
- **DELAY**: 지연 시간 추가
- **STOP**: 중단

### 8. security_policies (보안 정책)

소스별 보안 정책 설정

#### 정책 규칙 예시
```json
{
  "rate_limit": {
    "max_requests_per_minute": 60,
    "max_requests_per_hour": 1000,
    "backoff_multiplier": 2
  },
  "retry": {
    "max_attempts": 3,
    "initial_delay_ms": 1000,
    "max_delay_ms": 60000
  },
  "rotation": {
    "ua_change_after_requests": 100,
    "ip_change_after_blocks": 3
  }
}
```

## 🔧 보안 전략

### 1. 지능형 UA 로테이션

```sql
-- 성공률 기반 UA 선택
CREATE VIEW v_ua_performance AS
SELECT 
    ua.id,
    ua.user_agent_string,
    ua.success_rate,
    ua.weight,
    (ua.success_rate * ua.weight / 100) as effective_weight
FROM user_agents ua
WHERE ua.status = 'ACTIVE'
  AND ua.deleted_at IS NULL
ORDER BY effective_weight DESC;
```

### 2. 적응형 레이트 리미팅

```sql
-- 동적 레이트 조정
CREATE PROCEDURE adjust_rate_limits()
BEGIN
    -- 차단이 많이 발생한 소스의 레이트 낮추기
    UPDATE token_buckets tb
    JOIN (
        SELECT source_id, 
               SUM(CASE WHEN is_blocked THEN 1 ELSE 0 END) / COUNT(*) as block_rate
        FROM request_logs
        WHERE requested_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
        GROUP BY source_id
        HAVING block_rate > 0.1
    ) blocked ON tb.source_id = blocked.source_id
    SET tb.refill_rate = tb.refill_rate * 0.8;
END;
```

### 3. 토큰 자동 갱신

```sql
-- 만료 임박 토큰 갱신
CREATE EVENT refresh_expiring_tokens
ON SCHEDULE EVERY 5 MINUTE
DO
    UPDATE source_auth_tokens
    SET status = 'EXPIRED'
    WHERE status = 'ACTIVE'
      AND expires_at <= NOW();
```

## 📈 모니터링 지표

### 1. UA 성능 분석
```sql
-- UA별 성능 통계
SELECT 
    ua.browser_name,
    ua.device_type,
    COUNT(rl.id) as request_count,
    AVG(CASE WHEN rl.status_code BETWEEN 200 AND 299 THEN 1 ELSE 0 END) as success_rate,
    AVG(rl.response_time_ms) as avg_response_time
FROM user_agents ua
JOIN request_logs rl ON ua.id = rl.user_agent_id
WHERE rl.requested_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY ua.browser_name, ua.device_type;
```

### 2. 차단 패턴 분석
```sql
-- 최근 차단 패턴
SELECT 
    bp.pattern_type,
    bp.pattern_name,
    bp.occurrence_count,
    bp.last_detected_at,
    bp.action
FROM blocking_patterns bp
WHERE bp.is_active = TRUE
  AND bp.last_detected_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY bp.occurrence_count DESC;
```

### 3. 레이트 리밋 상태
```sql
-- 토큰 버킷 사용률
SELECT 
    s.name as source_name,
    tb.bucket_type,
    tb.current_tokens / tb.capacity * 100 as token_usage_pct,
    tb.requests_in_window
FROM token_buckets tb
JOIN crawling_sources s ON tb.source_id = s.id
WHERE tb.updated_at >= DATE_SUB(NOW(), INTERVAL 5 MINUTE);
```

## 🚨 알림 조건

### 1. 높은 차단률
```sql
-- 차단률 30% 초과 시 알림
SELECT source_id, 
       COUNT(*) as total_requests,
       SUM(is_blocked) as blocked_requests,
       (SUM(is_blocked) / COUNT(*)) * 100 as block_rate
FROM request_logs
WHERE requested_at >= DATE_SUB(NOW(), INTERVAL 10 MINUTE)
GROUP BY source_id
HAVING block_rate > 30;
```

### 2. 토큰 고갈
```sql
-- 토큰 10% 미만 시 알림
SELECT * FROM token_buckets
WHERE (current_tokens / capacity) < 0.1;
```

## 📚 관련 문서
- [00-overview.md](00-overview.md) - 전체 개요
- [02-workflow-domain.md](02-workflow-domain.md) - 워크플로우 도메인
- [04-monitoring-domain.md](04-monitoring-domain.md) - 모니터링 도메인
