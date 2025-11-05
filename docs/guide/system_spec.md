📘 머스트잇 크롤링 시스템 명세서

(PM → 엔지니어 핸드오프용)

1. 요구사항 요약
   항목	설명
   목표	머스트잇(MustIt) 플랫폼의 셀러 상품을 안정적·자동적으로 크롤링하고, 상품 변동을 빠르게 감지하여 내부 시스템으로 전달
   크롤링 대상	머스트잇 모바일 웹(https://m.web.mustit.co.kr) API 엔드포인트
   핵심 API	- 미니샵 목록: /mustit-api/facade-api/v1/search/mini-shop-search
- 상품 상세: /mustit-api/facade-api/v1/item/{item_no}/detail/top
- 상품 옵션: /mustit-api/legacy-api/v1/auction_products/{item_no}/options
  운영 주체	API 서버 (EventBridge 트리거) + SQS Worker (ECS 기반)
  목표 속성	고가용성, 분산 리밋 제어, 에러 복구, 변경 감지 및 자동 전송
2. 주요 기능 정의
   🧩 2.1 셀러 관리
   항목	설명
   셀러 등록/수정	고유한 sellerId (머스트잇 제공 ID) 등록
   주기 관리	셀러별 크롤링 주기 설정 가능 (시간/일 단위)
   주기 변경 반영	변경 즉시 다음 스케줄에 반영
   상태 관리	Active / Paused / Disabled 상태 전환 가능
   ⏰ 2.2 스케줄링 & 트리거
   항목	설명
   EventBridge	셀러 주기 정책에 따라 API 서버의 /crawl/start 호출
   Trigger	sellerId 기반으로 CrawlTask 생성 후 Outbox에 저장
   Outbox → SQS	비동기 큐 기반 발행 (Idempotent 보장)
   ECS Worker	SQS 폴링하여 크롤링 작업 수행
   🧠 2.3 크롤링 태스크 관리
   항목	설명
   엔티티	CrawlTask (sellerId, endpoint, page, status, retries, createdAt 등)
   상태	WAITING → PUBLISHED → RUNNING → SUCCESS / FAILED / RETRY
   재시도 정책	HTTP 429 or 5xx 시 RETRY 상태로 이동, DLQ 3회 후 FAILED
   타임아웃	10분 이상 미완료 시 재큐잉
   Idempotency	(sellerId, endpoint, page) unique constraint 적용
   하위 작업	미니샵 → 상세 → 옵션 순서로 생성 및 연계
   🧩 2.4 유저 에이전트 및 토큰 관리
   항목	설명
   User-Agent Pool	여러 에이전트를 로드밸런싱 형태로 사용
   Token 발급	User-Agent별로 MustIt API 요청 시 쿠키로 발급된 token 저장
   Token 버킷 리미터	시간당 80회 제한 (Redis Lua 기반 분산 제어)
   상태 전이	IDLE → ACTIVE → RATE_LIMITED → DISABLED → RECOVERED
   429 응답 처리	해당 토큰 즉시 폐기, 에이전트 상태 DISABLED 전환
   복구 프로세스	일정 시간 후 자동 RECOVERED
   📦 2.5 데이터 저장 및 변경 감지
   항목	설명
   저장 구조	Raw JSON → S3 (원본), RDS(MySQL) (상품 메타)
   변경 감지	JSON 주요 필드 subset 해시 비교 (가격, 옵션, 이미지 등)
   신규 상품	기존 DB에 없는 itemNo 등록
   변경 상품	Outbox 테이블에 이벤트 생성 후 내부 API/SQS 전송
   데이터 버전	version, hash, last_changed_at 필드로 관리
   📊 2.6 모니터링 / 통계
   항목	설명
   성공률 추적	일/셀러 기준 성공률, 실패률, 재시도 횟수 집계
   진행률	태스크 상태 기반 비율 계산
   알림	Slack / CloudWatch Alarm 통합
   대시보드	Grafana or OpenSearch Dashboard 연동 (Seller별, Endpoint별)
3. 시퀀스 다이어그램
   sequenceDiagram
   participant EB as AWS EventBridge
   participant API as API Server
   participant DB as RDS / Outbox
   participant SQS as SQS Queue
   participant Worker as ECS Worker
   participant MustIt as MustIt API

   EB->>API: /crawl/start (sellerId)
   API->>DB: CrawlTask 생성 (WAITING)
   API->>SQS: CrawlTask 발행 (PUBLISHED)
   Worker->>SQS: 메시지 폴링
   Worker->>MustIt: 상품 목록/상세/옵션 요청
   MustIt-->>Worker: JSON 응답
   Worker->>DB: 결과 저장 (S3 + RDS)
   alt 변경 감지
   Worker->>DB: Outbox 이벤트 생성
   DB->>API: 내부 상품 갱신 트리거
   end
   Worker->>DB: 상태 → SUCCESS / FAILED

4. 태스크 상태 다이어그램
   stateDiagram-v2
   [*] --> WAITING
   WAITING --> PUBLISHED: Outbox → SQS
   PUBLISHED --> RUNNING: Worker consumes
   RUNNING --> SUCCESS: 완료
   RUNNING --> FAILED: 오류 발생
   FAILED --> RETRY: 조건 충족 시 재시도
   RETRY --> PUBLISHED: 재발행
   RUNNING --> TIMEOUT: 작업 지연
   TIMEOUT --> RETRY
   SUCCESS --> [*]

5. 시스템 확장 및 향후 고려사항
   구분	제안 내용
   Adaptive Scheduling	셀러별 데이터 변경 빈도 기반으로 주기 자동 조정
   Agent Analytics	토큰/에이전트별 성공률, 429 비율 추적
   Data Quality Scoring	셀러별 데이터 정합성 점수 부여
   Self-Healing	5xx 지속 발생 셀러 자동 일시정지 및 Slack 알림
   Sandbox Mock	MustIt API 변경 대비용 Mock 서버 구축
6. 기술 스택
   영역	기술
   Scheduling	AWS EventBridge, CloudWatch
   Queueing	AWS SQS (DLQ 포함)
   Worker Infra	AWS ECS (Fargate)
   Storage	S3 (Raw), RDS (MySQL), Redis
   Monitoring	CloudWatch, OpenSearch, Grafana
   Rate Limiting	Redis Lua 기반 Token Bucket
   Language	Python 3.11 / Java 21 (혼합 가능)
   Framework	Chalice / Spring Boot
   Deployment	GitLab CI/CD or AWS CodePipeline
7. 요약 아키텍처 다이어그램 (텍스트 버전)
   [EventBridge]
   ↓
   [API Server - Chalice/Spring]
   ↓ (Outbox)
   [SQS Queue]
   ↓
   [ECS Worker Cluster]
   ↘
   [MustIt API → Crawling]
   ↘
   [S3 Raw Storage]
   [RDS Product Meta]
   ↘
   [Outbox Event → Internal API/SQS]