# Product 가공 로직 리팩토링 분석

## 1. CrawledRaw 정리 전략

### 현황

`CrawledRaw`는 크롤링된 원본 데이터를 JSON 형태로 보관하는 Aggregate입니다.
상태 흐름: `PENDING` -> `PROCESSED` / `FAILED`

### 정리 대상

| 상태 | 보존 기간 | 정리 전략 |
|------|-----------|-----------|
| PROCESSED | 7일 | 배치 스케줄러로 주기적 삭제 |
| FAILED | 30일 | 재처리 시도 후 삭제 (최대 3회) |
| PENDING | - | 가공 처리 대상 (삭제 불가) |

### 정리 구현 방향

1. **배치 스케줄러**: `CrawledRawCleanupScheduler`로 주기적 삭제
2. **소프트 삭제 미적용**: Raw 데이터는 재생성 가능하므로 하드 삭제
3. **파티셔닝 고려**: 데이터 규모 증가 시 날짜 기반 파티셔닝 검토

---

## 2. 미갱신 상품 처리 전략

### 문제 정의

MINI_SHOP 크롤링에서 더 이상 노출되지 않는 상품(판매 종료, 품절 등)은
새로운 CrawledRaw가 생성되지 않아 상태가 갱신되지 않습니다.

### 탐지 기준

| 기준 | 설명 |
|------|------|
| `lastCrawledAt` | 마지막 크롤링 시점으로부터 N일 경과 |
| `needsSync = false` | 동기화 불필요 상태 |
| 스케줄러 활성 상태 | 해당 판매자의 스케줄러가 활성 상태인데 상품만 미갱신 |

### 처리 전략

1. **StaleProductDetectionScheduler**: 미갱신 상품 탐지 스케줄러
   - 주기: 1일 1회
   - 기준: `lastCrawledAt < now - 7일` AND 스케줄러 ACTIVE
2. **상태 전환**: `ACTIVE` -> `STALE` -> `INACTIVE`
   - STALE: 7일 미갱신 (경고 단계)
   - INACTIVE: 14일 미갱신 (비활성화)
3. **복구**: 다시 크롤링되면 자동으로 ACTIVE 복구

### 구현 우선순위

- Phase 1: 미갱신 상품 탐지 쿼리 추가 (CrawledProductQueryPort)
- Phase 2: StaleProductDetectionScheduler 구현
- Phase 3: 외부 동기화 시 STALE/INACTIVE 상품 처리 로직 추가
