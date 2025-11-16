# PRD: 머스트잇 셀러 크롤러 (Mustit Seller Crawler)

**작성일**: 2025-01-15
**작성자**: sangwon-ryu
**상태**: Draft

---

## 📋 프로젝트 개요

### 비즈니스 목적

머스트잇(Mustit) 플랫폼의 셀러 상품 정보를 주기적으로 크롤링하여 자동 수집하고, 상품 변경 감지를 통해 외부 상품 서버에 실시간 업데이트를 제공하는 시스템 구축.

**핵심 가치**:
- **자동화**: 수동 상품 관리 불필요, 주기적 자동 크롤링
- **실시간성**: 상품 변경 즉시 감지 및 외부 서버 동기화
- **확장성**: 셀러별 독립적 크롤링 주기, 워커 Auto Scaling
- **안정성**: Rate Limiting, Retry 전략, Outbox Pattern

### 주요 사용자

- **내부 시스템**: 자동화된 크롤링 및 데이터 동기화
- **관리자**: 셀러 등록/수정, 주기 설정, 모니터링

### 성공 기준

- **데이터 정확성**: 상품 정보 99% 이상 정확도
- **처리량**: 하루 2만 상품 처리
- **변경 감지 속도**: 변경 발생 후 24시간 내 외부 서버 동기화
- **크롤링 성공률**: 95% 이상

---

## 🏗️ Layer별 요구사항

### 1. Domain Layer

#### 1.1 Aggregate: Seller (셀러)

**속성**:
- `sellerId`: SellerId (Value Object, 머스트잇 고유 ID)
- `name`: String (셀러 이름)
- `crawlingInterval`: CrawlingInterval (Value Object, 크롤링 주기)
- `status`: SellerStatus (Enum: ACTIVE, INACTIVE)
- `totalProductCount`: Integer (총 상품 수, 미니샵 API에서 조회)
- `createdAt`: LocalDateTime
- `updatedAt`: LocalDateTime

**비즈니스 규칙**:
1. **셀러 등록**:
   - 셀러 ID는 머스트잇에서 고유해야 함 (중복 불가)
   - 기본 크롤링 주기는 1일 (변경 가능)
   - 등록 시 상태는 ACTIVE

2. **셀러 주기 변경**:
   - 크롤링 주기는 일(day) 단위만 허용
   - 주기 변경 시 EventBridge Rule 자동 업데이트 (Application Layer)
   - 최소/최대 주기 제한: **TODO** (현재 미정, 제안: 최소 1일, 최대 30일)

3. **셀러 비활성화**:
   - INACTIVE 상태 시 크롤링 중단
   - EventBridge Rule 삭제 또는 비활성화

**Value Objects**:
- **SellerId**: String (머스트잇 셀러 ID, 예: "seller_12345")
- **CrawlingInterval**: Integer (일 단위, 1-30)
- **SellerStatus**: Enum (ACTIVE, INACTIVE)

**Zero-Tolerance 규칙 준수**:
- ✅ Law of Demeter (Getter 체이닝 금지)
  - `seller.getCrawlingIntervalDays()` (O)
  - `seller.getCrawlingInterval().getDays()` (X)
- ✅ Lombok 금지 (Pure Java/Record 사용)
- ✅ Long FK 전략 (관계 어노테이션 금지)

---

#### 1.2 Aggregate: CrawlerTask (크롤링 태스크)

**속성**:
- `taskId`: TaskId (Value Object, UUID)
- `sellerId`: SellerId (FK)
- `taskType`: CrawlerTaskType (Enum: MINISHOP, PRODUCT_DETAIL, PRODUCT_OPTION)
- `requestUrl`: String (크롤링 대상 URL)
- `status`: CrawlerTaskStatus (Enum)
- `retryCount`: Integer (재시도 횟수, 최대 2회)
- `errorMessage`: String (실패 시 에러 메시지)
- `createdAt`: LocalDateTime
- `publishedAt`: LocalDateTime (SQS 발행 시점)
- `startedAt`: LocalDateTime (워커 폴링 시점)
- `completedAt`: LocalDateTime (완료 시점)

**비즈니스 규칙**:
1. **태스크 생성**:
   - 태스크 생성 시 상태는 WAITING
   - taskType에 따라 requestUrl 형식 검증
     - MINISHOP: `/mustit-api/facade-api/v1/searchmini-shop-search?sellerId={seller_id}&pageNo={page}&pageSize=500&order=LATEST`
     - PRODUCT_DETAIL: `/mustit-api/facade-api/v1/item/{item_no}/detail/top`
     - PRODUCT_OPTION: `/mustit-api/legacy-api/v1/auction_products/{item_no}/options`

2. **태스크 상태 전환**:
   ```
   WAITING → PUBLISHED → IN_PROGRESS → COMPLETED/FAILED
                                     ↓
                                  RETRY (최대 2회)
   ```
   - **WAITING**: 태스크 저장 직후
   - **PUBLISHED**: SQS 발행 완료 시점
   - **IN_PROGRESS**: 워커가 폴링한 시점
   - **RETRY**: 실패 후 재시도 (retryCount < 2)
   - **FAILED**: 재시도 2회 초과 시 최종 실패
   - **COMPLETED**: 크롤링 성공

3. **재시도 전략**:
   - 재시도 최대 2회
   - 재시도 시 RETRY 상태로 변경 후 다시 PUBLISHED
   - 실패 알림 없음 (메트릭으로만 추적)

**Value Objects**:
- **TaskId**: UUID
- **CrawlerTaskType**: Enum (MINISHOP, PRODUCT_DETAIL, PRODUCT_OPTION)
- **CrawlerTaskStatus**: Enum (WAITING, PUBLISHED, IN_PROGRESS, COMPLETED, FAILED, RETRY)

**Zero-Tolerance 규칙 준수**:
- ✅ Law of Demeter
- ✅ Lombok 금지
- ✅ Long FK 전략 (sellerId는 Long이 아닌 SellerId VO)

---

#### 1.3 Aggregate: UserAgent (유저 에이전트)

**속성**:
- `userAgentId`: UserAgentId (Value Object, UUID)
- `userAgentString`: String (실제 User-Agent 문자열)
- `token`: String (머스트잇 비회원 토큰, Nullable)
- `status`: UserAgentStatus (Enum: ACTIVE, SUSPENDED, BLOCKED)
- `requestCount`: Integer (현재 시간 기준 요청 수)
- `lastRequestAt`: LocalDateTime (마지막 요청 시점)
- `tokenIssuedAt`: LocalDateTime (토큰 발급 시점)
- `createdAt`: LocalDateTime

**비즈니스 규칙**:
1. **UserAgent 생성**:
   - 50개의 미리 정의된 UserAgent 문자열 사용
   - 생성 시 token은 null, status는 ACTIVE

2. **토큰 발급**:
   - 토큰이 없으면 `https://m.web.mustit.co.kr` 호출하여 발급
   - 쿠키에서 token 키값 추출하여 저장
   - tokenIssuedAt 기록

3. **토큰 버킷 리미터** (시간당 80회):
   - 정확히 1시간 기준 (예: 10:00-11:00)
   - 요청 전 `canMakeRequest()` 메서드로 검증
   - requestCount가 80 미만이고, lastRequestAt이 현재 시간 기준 1시간 이내면 허용
   - 1시간 경과 시 requestCount 리셋

4. **429 응답 처리**:
   - 429 응답 받은 즉시 token 폐기 (null)
   - status를 SUSPENDED로 변경
   - **자동 복구 전략**: **TODO** (현재 미정, 제안: 1시간 후 자동 ACTIVE 복귀)

5. **UserAgent 상태**:
   - **ACTIVE**: 정상 사용 가능
   - **SUSPENDED**: 429 응답으로 일시 중지
   - **BLOCKED**: 관리자 수동 차단 (장기 문제 발생 시)

**Value Objects**:
- **UserAgentId**: UUID
- **UserAgentStatus**: Enum (ACTIVE, SUSPENDED, BLOCKED)

**Domain 메서드**:
- `issueToken()`: 토큰 발급
- `canMakeRequest()`: 요청 가능 여부 확인 (토큰 버킷)
- `incrementRequestCount()`: 요청 수 증가
- `suspend()`: 429 응답 시 일시 중지
- `activate()`: 재활성화

**Zero-Tolerance 규칙 준수**:
- ✅ Law of Demeter
- ✅ Lombok 금지
- ✅ Tell Don't Ask (canMakeRequest는 내부 상태 기반 판단)

---

#### 1.4 Aggregate: Product (상품)

**속성**:
- `productId`: ProductId (Value Object, UUID)
- `itemNo`: Long (머스트잇 상품 번호)
- `sellerId`: SellerId (FK)
- `minishopDataHash`: String (미니샵 데이터 해시)
- `detailDataHash`: String (상세 데이터 해시)
- `optionDataHash`: String (옵션 데이터 해시)
- `isComplete`: Boolean (완성 여부)
- `createdAt`: LocalDateTime
- `updatedAt`: LocalDateTime

**비즈니스 규칙**:
1. **상품 생성**:
   - 미니샵 크롤링 시 itemNo 추출하여 생성
   - 초기 상태는 isComplete = false

2. **상품 완성 기준**:
   - 미니샵 + 상품 상세 + 상품 옵션 각 1번씩 크롤링 완료
   - 모든 해시값이 null이 아니면 isComplete = true

3. **변경 감지**:
   - 각 엔드포인트 크롤링 시 기존 해시값과 비교
   - 해시값 다르면 변경으로 판단 → Outbox 생성
   - **해시 알고리즘**: **TODO** (현재 미정, 제안: MD5)
   - **해시 대상**: 전체 JSON 응답 (raw data)

**Value Objects**:
- **ProductId**: UUID
- **ItemNo**: Long (머스트잇 상품 번호)

**Domain 메서드**:
- `updateMinishopData(rawJson)`: 미니샵 데이터 업데이트 및 해시 계산
- `updateDetailData(rawJson)`: 상세 데이터 업데이트 및 해시 계산
- `updateOptionData(rawJson)`: 옵션 데이터 업데이트 및 해시 계산
- `isComplete()`: 완성 여부 확인
- `hasChanged(oldHash, newHash)`: 변경 감지

**Zero-Tolerance 규칙 준수**:
- ✅ Law of Demeter
- ✅ Lombok 금지
- ✅ Long FK 전략

---

#### 1.5 Aggregate: ProductOutbox (상품 외부 전송)

**속성**:
- `outboxId`: OutboxId (Value Object, UUID)
- `productId`: ProductId (FK)
- `eventType`: OutboxEventType (Enum: PRODUCT_CREATED, PRODUCT_UPDATED)
- `payload`: String (JSON, 외부 전송할 데이터)
- `status`: OutboxStatus (Enum: WAITING, SENDING, COMPLETED, FAILED)
- `retryCount`: Integer (재시도 횟수)
- `errorMessage`: String (실패 시 에러 메시지)
- `createdAt`: LocalDateTime
- `sentAt`: LocalDateTime (전송 시점)

**비즈니스 규칙**:
1. **Outbox 생성**:
   - 상품 변경 감지 시 자동 생성
   - 초기 상태는 WAITING
   - payload에 외부 상품 서버 API 형식으로 데이터 구성

2. **외부 전송 (배치 처리)**:
   - 아웃박스 저장 후 배치 처리 (즉시 전송 아님)
   - 배치 주기: **TODO** (현재 미정, 제안: 5분마다)
   - REST API 호출로 외부 상품 서버에 전송

3. **재시도 전략**:
   - 실패 시 지연 재시도 (Exponential Backoff)
   - 재시도 최대 횟수: **TODO** (현재 미정, 제안: 5회)
   - 최종 실패 시 FAILED 상태로 저장

4. **Outbox 상태 전환**:
   ```
   WAITING → SENDING → COMPLETED
                    ↓
                 FAILED (재시도 5회 초과)
   ```

**Value Objects**:
- **OutboxId**: UUID
- **OutboxEventType**: Enum (PRODUCT_CREATED, PRODUCT_UPDATED)
- **OutboxStatus**: Enum (WAITING, SENDING, COMPLETED, FAILED)

**Zero-Tolerance 규칙 준수**:
- ✅ Law of Demeter
- ✅ Lombok 금지
- ✅ Long FK 전략

---

### 2. Application Layer

#### 2.1 Command UseCase

##### RegisterSellerUseCase (셀러 등록)

**Input**: `RegisterSellerCommand(sellerId, name, crawlingIntervalDays)`
**Output**: `SellerResponse(sellerId, name, status, crawlingIntervalDays)`
**Transaction**: Yes

**비즈니스 로직**:
1. 셀러 ID 중복 확인 (중복 시 예외)
2. Seller Aggregate 생성 (상태: ACTIVE)
3. DB 저장
4. **트랜잭션 커밋**
5. EventBridge Rule 생성 (Application Layer 내 별도 메서드)
   - ⚠️ EventBridge API 호출은 **트랜잭션 밖**

**EventBridge Rule 생성**:
- Rule Name: `mustit-crawler-{sellerId}`
- Schedule Expression: `rate({crawlingIntervalDays} days)`
- Target: API 서버 엔드포인트 (`POST /api/internal/crawling/trigger`)

---

##### UpdateSellerIntervalUseCase (셀러 주기 변경)

**Input**: `UpdateSellerIntervalCommand(sellerId, newIntervalDays)`
**Output**: `SellerResponse`
**Transaction**: Yes

**비즈니스 로직**:
1. Seller 조회 (없으면 예외)
2. 주기 변경 (Domain 메서드)
3. DB 저장
4. **트랜잭션 커밋**
5. EventBridge Rule 업데이트 (트랜잭션 밖)
   - Schedule Expression 변경: `rate({newIntervalDays} days)`

---

##### TriggerCrawlingUseCase (크롤링 트리거)

**Input**: `TriggerCrawlingCommand(sellerId)`
**Output**: `CrawlingTriggeredResponse(taskCount)`
**Transaction**: Yes

**비즈니스 로직**:
1. Seller 조회 (ACTIVE 상태만 처리)
2. 미니샵 API 호출 (pageNo=0, pageSize=1) → 총 상품 수 조회
   - ⚠️ 외부 API 호출이지만, 빠른 조회(< 1초)이므로 트랜잭션 내 허용
   - 대안: 트랜잭션 밖에서 조회 후 트랜잭션 시작
3. 총 상품 수 업데이트 (Seller Aggregate)
4. 페이지 수 계산: `Math.ceil(totalProductCount / 500)`
5. 페이지별 MINISHOP 태스크 생성 (WAITING 상태)
6. DB 저장 (Bulk Insert 최적화)
7. **트랜잭션 커밋**
8. SQS 발행 (트랜잭션 밖, 다음 UseCase)

**Transaction 경계 설계**:
- **Option 1** (현재 설계): 미니샵 조회 포함 (단일 트랜잭션)
  - 장점: 단순함
  - 단점: 외부 API 지연 시 트랜잭션 지연
- **Option 2**: 미니샵 조회 → 트랜잭션 → SQS 발행 (분리)
  - 장점: 트랜잭션 최소화
  - 단점: 복잡도 증가

**권장**: Option 1 (미니샵 조회는 빠르므로 트랜잭션 내 허용)

---

##### PublishCrawlerTasksUseCase (크롤러 태스크 발행)

**Input**: `PublishCrawlerTasksCommand(taskIds)`
**Output**: `PublishedTasksResponse(publishedCount)`
**Transaction**: Yes (상태 업데이트만)

**비즈니스 로직**:
1. WAITING 상태 태스크 조회 (Batch)
2. 상태를 PUBLISHED로 변경
3. publishedAt 기록
4. DB 저장
5. **트랜잭션 커밋**
6. SQS 발행 (트랜잭션 밖)
   - Message Body: `{ taskId, requestUrl, taskType }`
   - Message Attributes: `sellerId`

**SQS 발행 전략**:
- Batch Send (최대 10개씩)
- 발행 실패 시 재시도 (최대 3회)

---

##### ProcessCrawlerTaskUseCase (크롤러 태스크 처리)

**Input**: `ProcessCrawlerTaskCommand(taskId)`
**Output**: `TaskProcessedResponse(status)`
**Transaction**: Yes (상태 업데이트만)

**비즈니스 로직**:
1. 태스크 조회 (PUBLISHED 상태만 처리)
2. 상태를 IN_PROGRESS로 변경
3. startedAt 기록
4. DB 저장
5. **트랜잭션 커밋**
6. 크롤링 실행 (트랜잭션 밖)
   - UserAgent 할당 (UserAgentPoolManager)
   - 머스트잇 API 호출
   - 응답 데이터 처리
7. 트랜잭션 시작
8. 크롤링 결과에 따라 상태 변경:
   - 성공: COMPLETED
   - 실패 (retryCount < 2): RETRY
   - 실패 (retryCount >= 2): FAILED
9. Product 업데이트 (해시 계산 및 변경 감지)
10. 변경 감지 시 ProductOutbox 생성
11. DB 저장
12. **트랜잭션 커밋**

**동시성 제어**:
- **TODO**: 동시 크롤링 제한 (현재 미정, 제안: 최대 100개 동시 처리)
- SQS Visibility Timeout: 30초
  - 30초 내 처리 완료해야 함
  - 실패 시 자동 재큐잉

---

##### ProcessProductOutboxUseCase (상품 외부 전송)

**Input**: `ProcessProductOutboxCommand(outboxIds)` (배치)
**Output**: `OutboxProcessedResponse(successCount, failedCount)`
**Transaction**: Yes (상태 업데이트만)

**비즈니스 로직**:
1. WAITING 상태 Outbox 조회 (Batch, 최대 100개)
2. 상태를 SENDING으로 변경
3. DB 저장
4. **트랜잭션 커밋**
5. 외부 API 호출 (트랜잭션 밖)
   - REST API: 외부 상품 서버
   - Timeout: **TODO** (현재 미정, 제안: 5초)
6. 트랜잭션 시작
7. 결과에 따라 상태 변경:
   - 성공: COMPLETED, sentAt 기록
   - 실패: retryCount 증가, WAITING (재시도) 또는 FAILED (최종 실패)
8. DB 저장
9. **트랜잭션 커밋**

**배치 처리 주기**:
- **TODO**: 현재 미정, 제안: 5분마다 Scheduled Task

---

#### 2.2 Query UseCase

##### GetSellerUseCase (셀러 조회)

**Input**: `GetSellerQuery(sellerId)`
**Output**: `SellerDetailResponse(sellerId, name, status, crawlingIntervalDays, totalProductCount, ...)`
**Transaction**: ReadOnly

---

##### ListSellersUseCase (셀러 목록 조회)

**Input**: `ListSellersQuery(status, page, size)`
**Output**: `PageResponse<SellerSummaryResponse>`
**Transaction**: ReadOnly
**페이징**: Offset-based (간단함)

---

##### GetCrawlingMetricsUseCase (크롤링 메트릭 조회)

**Input**: `GetCrawlingMetricsQuery(sellerId, date)` (날짜 기준)
**Output**: `CrawlingMetricsResponse(successRate, progressRate, taskStats)`
**Transaction**: ReadOnly

**메트릭 계산**:
- **자정 기준** (00:00-24:00)
- **성공률**: 성공 태스크 수 / 전체 태스크 수 * 100
- **진행률**: 완료된 상품 수 / 셀러 총 상품 수 * 100
- **태스크 통계**: COMPLETED, FAILED, IN_PROGRESS 개수

**쿼리 최적화**:
- QueryDSL로 집계 쿼리 (COUNT, GROUP BY)
- 인덱스: `(sellerId, createdAt, status)`

---

##### GetUserAgentPoolStatusUseCase (UserAgent 풀 상태 조회)

**Input**: `GetUserAgentPoolStatusQuery()`
**Output**: `UserAgentPoolStatusResponse(totalCount, activeCount, suspendedCount, blockedCount)`
**Transaction**: ReadOnly

---

#### 2.3 Application Service

##### UserAgentPoolManager (UserAgent 풀 관리)

**책임**:
- UserAgent 할당 (크롤링 요청 시)
- 토큰 버킷 리미터 검증
- 429 응답 시 UserAgent 일시 중지
- **자동 복구 전략**: **TODO** (현재 미정, 제안: Scheduled Task로 1시간마다 SUSPENDED → ACTIVE 복귀)

**메서드**:
- `assignUserAgent()`: 사용 가능한 UserAgent 할당 (Round-robin)
- `releaseUserAgent(userAgentId)`: UserAgent 반환
- `suspendUserAgent(userAgentId)`: 429 응답 시 일시 중지
- `recoverSuspendedUserAgents()`: 일시 중지된 UserAgent 복구 (Scheduled)

**동시성 제어**:
- UserAgent 할당 시 Race Condition 방지
- **Pessimistic Lock** 사용 (`SELECT FOR UPDATE`)

---

#### Zero-Tolerance 규칙 준수

- ✅ Command/Query 분리 (CQRS)
- ✅ **Transaction 경계 엄격 관리**
  - 외부 API 호출 (머스트잇 크롤링, 외부 상품 서버)은 트랜잭션 밖
  - 예외: 미니샵 총 상품 수 조회 (빠른 조회이므로 허용)
- ✅ Assembler 패턴 사용 (Command/Response DTO ↔ Domain 변환)

---

### 3. Persistence Layer

#### 3.1 JPA Entity

##### SellerJpaEntity

**테이블**: `sellers`

**필드**:
- `id`: Long (PK, Auto Increment)
- `seller_id`: String (Unique, Not Null, Index)
- `name`: String (Not Null)
- `crawling_interval_days`: Integer (Not Null, CHECK > 0)
- `status`: String (Not Null, Index)
- `total_product_count`: Integer (Default 0)
- `created_at`: LocalDateTime (Not Null)
- `updated_at`: LocalDateTime (Not Null)

**인덱스**:
- `idx_seller_id` (seller_id) - Unique
- `idx_status` (status) - 셀러 목록 조회

---

##### CrawlerTaskJpaEntity

**테이블**: `crawler_tasks`

**필드**:
- `id`: Long (PK, Auto Increment)
- `task_id`: String (UUID, Unique, Not Null, Index)
- `seller_id`: String (FK, Not Null, Index)
- `task_type`: String (Not Null)
- `request_url`: String (Not Null)
- `status`: String (Not Null, Index)
- `retry_count`: Integer (Default 0)
- `error_message`: String (Nullable)
- `created_at`: LocalDateTime (Not Null, Index)
- `published_at`: LocalDateTime (Nullable)
- `started_at`: LocalDateTime (Nullable)
- `completed_at`: LocalDateTime (Nullable)

**인덱스**:
- `idx_task_id` (task_id) - Unique
- `idx_seller_id_created_at` (seller_id, created_at DESC) - 셀러별 태스크 조회
- `idx_status_created_at` (status, created_at DESC) - 상태별 태스크 조회 (배치 처리)

**파티셔닝 전략**:
- **TODO**: 현재 미정, 제안: `created_at` 기준 월별 파티셔닝 (PARTITION BY RANGE)
- 1년 후 데이터 증가 시 적용 검토

---

##### UserAgentJpaEntity

**테이블**: `user_agents`

**필드**:
- `id`: Long (PK, Auto Increment)
- `user_agent_id`: String (UUID, Unique, Not Null, Index)
- `user_agent_string`: String (Not Null)
- `token`: String (Nullable, 길이 500)
- `status`: String (Not Null, Index)
- `request_count`: Integer (Default 0)
- `last_request_at`: LocalDateTime (Nullable)
- `token_issued_at`: LocalDateTime (Nullable)
- `created_at`: LocalDateTime (Not Null)

**인덱스**:
- `idx_user_agent_id` (user_agent_id) - Unique
- `idx_status` (status) - 활성 UserAgent 조회

---

##### ProductJpaEntity

**테이블**: `products`

**필드**:
- `id`: Long (PK, Auto Increment)
- `product_id`: String (UUID, Unique, Not Null, Index)
- `item_no`: Long (Unique, Not Null, Index)
- `seller_id`: String (FK, Not Null, Index)
- `minishop_data_hash`: String (Nullable, MD5 해시)
- `detail_data_hash`: String (Nullable, MD5 해시)
- `option_data_hash`: String (Nullable, MD5 해시)
- `is_complete`: Boolean (Default false, Index)
- `created_at`: LocalDateTime (Not Null)
- `updated_at`: LocalDateTime (Not Null)

**인덱스**:
- `idx_product_id` (product_id) - Unique
- `idx_item_no` (item_no) - Unique
- `idx_seller_id_is_complete` (seller_id, is_complete) - 셀러별 완성 상품 조회

---

##### ProductRawDataJpaEntity

**테이블**: `product_raw_data`

**필드**:
- `id`: Long (PK, Auto Increment)
- `product_id`: String (FK, Not Null, Index)
- `data_type`: String (MINISHOP, PRODUCT_DETAIL, PRODUCT_OPTION, Not Null)
- `raw_json`: String (TEXT, Not Null)
- `created_at`: LocalDateTime (Not Null)

**인덱스**:
- `idx_product_id_data_type` (product_id, data_type) - Raw 데이터 조회

**데이터 저장 전략**:
- RDB(MySQL)에만 저장 (S3 사용 안 함)
- TEXT 타입으로 JSON 저장

---

##### ProductOutboxJpaEntity

**테이블**: `product_outbox`

**필드**:
- `id`: Long (PK, Auto Increment)
- `outbox_id`: String (UUID, Unique, Not Null, Index)
- `product_id`: String (FK, Not Null, Index)
- `event_type`: String (Not Null)
- `payload`: String (TEXT, Not Null)
- `status`: String (Not Null, Index)
- `retry_count`: Integer (Default 0)
- `error_message`: String (Nullable)
- `created_at`: LocalDateTime (Not Null, Index)
- `sent_at`: LocalDateTime (Nullable)

**인덱스**:
- `idx_outbox_id` (outbox_id) - Unique
- `idx_status_created_at` (status, created_at ASC) - 배치 처리 (오래된 순)

---

#### 3.2 Repository

##### SellerJpaRepository

```java
public interface SellerJpaRepository extends JpaRepository<SellerJpaEntity, Long> {
    Optional<SellerJpaEntity> findBySellerId(String sellerId);
    List<SellerJpaEntity> findByStatus(String status);
}
```

---

##### CrawlerTaskJpaRepository

```java
public interface CrawlerTaskJpaRepository extends JpaRepository<CrawlerTaskJpaEntity, Long> {
    Optional<CrawlerTaskJpaEntity> findByTaskId(String taskId);
    List<CrawlerTaskJpaEntity> findByStatus(String status, Pageable pageable);
}
```

---

##### CrawlerTaskQueryDslRepository

**메서드**:
- `findBySellerIdAndDateRange(sellerId, startDate, endDate)`: 셀러별 기간 조회
- `countBySellerIdAndStatusAndDate(sellerId, status, date)`: 메트릭 계산용 집계

**최적화**:
- DTO Projection (N+1 방지)
- 인덱스 활용 (`idx_seller_id_created_at`, `idx_status_created_at`)

---

##### UserAgentJpaRepository

```java
public interface UserAgentJpaRepository extends JpaRepository<UserAgentJpaEntity, Long> {
    List<UserAgentJpaEntity> findByStatus(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ua FROM UserAgentJpaEntity ua WHERE ua.status = 'ACTIVE' ORDER BY ua.lastRequestAt ASC")
    Optional<UserAgentJpaEntity> findFirstActiveUserAgentForUpdate();
}
```

**동시성 제어**:
- Pessimistic Lock (`SELECT FOR UPDATE`) 사용
- UserAgent 할당 시 Race Condition 방지

---

##### ProductJpaRepository

```java
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {
    Optional<ProductJpaEntity> findByItemNo(Long itemNo);
    List<ProductJpaEntity> findBySellerIdAndIsComplete(String sellerId, boolean isComplete);
}
```

---

##### ProductOutboxJpaRepository

```java
public interface ProductOutboxJpaRepository extends JpaRepository<ProductOutboxJpaEntity, Long> {
    List<ProductOutboxJpaEntity> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
}
```

---

#### Zero-Tolerance 규칙 준수

- ✅ Long FK 전략
  - `private String sellerId;` (O)
  - `@ManyToOne private Seller seller;` (X)
- ✅ QueryDSL 최적화 (N+1 방지)
- ✅ Lombok 금지 (Entity는 Pure Java 또는 Record)

---

### 4. REST API Layer

#### 4.1 API 엔드포인트

| Method | Path | Description | Request DTO | Response DTO | Status Code |
|--------|------|-------------|-------------|--------------|-------------|
| POST | /api/v1/sellers | 셀러 등록 | RegisterSellerRequest | SellerResponse | 201 Created |
| GET | /api/v1/sellers/{sellerId} | 셀러 조회 | - | SellerDetailResponse | 200 OK |
| GET | /api/v1/sellers | 셀러 목록 조회 | ListSellersRequest | PageResponse<SellerSummaryResponse> | 200 OK |
| PATCH | /api/v1/sellers/{sellerId}/interval | 셀러 주기 변경 | UpdateSellerIntervalRequest | SellerResponse | 200 OK |
| POST | /api/v1/sellers/{sellerId}/activate | 셀러 활성화 | - | SellerResponse | 200 OK |
| POST | /api/v1/sellers/{sellerId}/deactivate | 셀러 비활성화 | - | SellerResponse | 200 OK |
| GET | /api/v1/metrics/crawling | 크롤링 메트릭 조회 | GetCrawlingMetricsRequest | CrawlingMetricsResponse | 200 OK |
| GET | /api/v1/user-agents/status | UserAgent 풀 상태 | - | UserAgentPoolStatusResponse | 200 OK |
| POST | /api/internal/crawling/trigger | 크롤링 트리거 (EventBridge) | TriggerCrawlingRequest | CrawlingTriggeredResponse | 200 OK |

---

#### 4.2 Request/Response DTO

##### RegisterSellerRequest

```java
public record RegisterSellerRequest(
    @NotBlank String sellerId,
    @NotBlank String name,
    @Min(1) @Max(30) Integer crawlingIntervalDays
) {}
```

---

##### SellerResponse

```java
public record SellerResponse(
    String sellerId,
    String name,
    SellerStatus status,
    Integer crawlingIntervalDays,
    Integer totalProductCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

---

##### CrawlingMetricsResponse

```java
public record CrawlingMetricsResponse(
    String sellerId,
    LocalDate date,
    Double successRate,
    Double progressRate,
    TaskStats taskStats
) {
    public record TaskStats(
        Integer total,
        Integer completed,
        Integer failed,
        Integer inProgress
    ) {}
}
```

---

##### Error Response

```json
{
  "errorCode": "SELLER_NOT_FOUND",
  "message": "셀러를 찾을 수 없습니다.",
  "timestamp": "2025-01-15T12:34:56Z",
  "path": "/api/v1/sellers/invalid-seller-id"
}
```

---

#### 4.3 인증/인가

**인증**:
- **내부 API** (`/api/internal/*`): API Key 인증 (EventBridge에서 호출)
- **관리 API** (`/api/v1/*`): JWT 인증 (관리자 전용)

**인가**:
- 관리자만 셀러 등록/수정/비활성화 가능
- 메트릭 조회는 읽기 권한만 필요

---

#### 4.4 HTTP Status Code 전략

| Status Code | 용도 | 예시 |
|-------------|------|------|
| 200 OK | 성공 | GET, PATCH 요청 성공 |
| 201 Created | 생성 성공 | POST 셀러 등록 |
| 400 Bad Request | Validation 실패 | 잘못된 입력 (crawlingIntervalDays < 1) |
| 401 Unauthorized | 인증 실패 | JWT 토큰 없음 또는 만료 |
| 403 Forbidden | 권한 없음 | 관리자 권한 없음 |
| 404 Not Found | 리소스 없음 | 셀러 ID 존재하지 않음 |
| 409 Conflict | 비즈니스 규칙 위반 | 셀러 ID 중복 |
| 500 Internal Server Error | 서버 오류 | 예상치 못한 에러 |

---

#### Zero-Tolerance 규칙 준수

- ✅ RESTful 설계 원칙
- ✅ 일관된 Error Response 형식
- ✅ Validation 필수 (`@NotBlank`, `@Min`, `@Max`)

---

### 5. Infrastructure Layer

#### 5.1 EventBridge 연동

**책임**:
- EventBridge Rule 생성/업데이트/삭제
- Cron 표현식 관리

**구현 클래스**: `AwsEventBridgeAdapter`

**메서드**:
- `createRule(sellerId, intervalDays)`: Rule 생성
- `updateRule(sellerId, newIntervalDays)`: Rule 업데이트
- `deleteRule(sellerId)`: Rule 삭제
- `disableRule(sellerId)`: Rule 비활성화
- `enableRule(sellerId)`: Rule 활성화

**Rule 설정**:
- **Rule Name**: `mustit-crawler-{sellerId}`
- **Schedule Expression**: `rate({intervalDays} days)`
- **Target**: API Gateway → API 서버 (`POST /api/internal/crawling/trigger`)
- **Input**: `{ "sellerId": "{sellerId}" }`

---

#### 5.2 SQS 연동

**책임**:
- SQS 메시지 발행 (Producer)
- SQS 메시지 폴링 (Consumer)

**구현 클래스**:
- `SqsPublisherAdapter`: 메시지 발행
- `SqsConsumerAdapter`: 메시지 폴링 및 처리

**SQS 설정**:
- **Queue Name**: `mustit-crawler-tasks.fifo` (FIFO Queue)
- **Message Group ID**: `{sellerId}` (셀러별 순서 보장)
- **Visibility Timeout**: 30초
- **Dead Letter Queue**: `mustit-crawler-tasks-dlq.fifo` (재시도 2회 초과 시)

**Message Body**:
```json
{
  "taskId": "uuid",
  "sellerId": "seller_12345",
  "taskType": "MINISHOP",
  "requestUrl": "https://m.web.mustit.co.kr/..."
}
```

---

#### 5.3 머스트잇 API 크롤러

**책임**:
- 머스트잇 API 호출
- UserAgent 할당 및 토큰 관리
- 429 응답 처리

**구현 클래스**: `MustitApiCrawlerAdapter`

**메서드**:
- `crawlMinishop(sellerId, pageNo, pageSize, userAgent)`: 미니샵 크롤링
- `crawlProductDetail(itemNo, userAgent)`: 상품 상세 크롤링
- `crawlProductOption(itemNo, userAgent)`: 상품 옵션 크롤링
- `issueToken(userAgentString)`: 토큰 발급

**HTTP Client 설정**:
- **Timeout**: Connect 3초, Read 10초
- **Retry**: 네트워크 오류 시 최대 3회 (Exponential Backoff)
- **User-Agent Header**: 할당받은 UserAgent 문자열 사용

**429 응답 처리**:
1. UserAgent 즉시 SUSPENDED 상태로 변경
2. 다른 UserAgent 할당 재시도
3. 모든 UserAgent가 SUSPENDED면 예외 발생 (태스크 RETRY)

---

#### 5.4 외부 상품 서버 연동

**책임**:
- 외부 상품 서버 API 호출 (REST)
- Outbox 데이터 전송

**구현 클래스**: `ExternalProductApiAdapter`

**메서드**:
- `sendProductCreated(payload)`: 상품 생성 이벤트 전송
- `sendProductUpdated(payload)`: 상품 업데이트 이벤트 전송

**HTTP Client 설정**:
- **Timeout**: **TODO** (현재 미정, 제안: Connect 3초, Read 5초)
- **Retry**: 실패 시 Exponential Backoff (최대 5회)

**Payload 형식**:
```json
{
  "eventType": "PRODUCT_UPDATED",
  "itemNo": 12345,
  "sellerId": "seller_12345",
  "productData": {
    "name": "상품명",
    "price": 10000,
    ...
  }
}
```

---

## ⚠️ 제약사항

### 비기능 요구사항

**성능**:
- 크롤링 응답 시간: **TODO** (현재 미정, 제안: P95 < 5초)
- 하루 처리할 상품 수: 2만 개
- Peak Time TPS: **TODO** (현재 미정, 제안: 100 TPS)

**보안**:
- API Key 인증 (내부 API)
- JWT 인증 (관리 API)
- HTTPS 통신 (TLS 1.2+)

**확장성**:
- SQS 워커 Auto Scaling (CPU 70% 기준)
- RDB Connection Pool: 최소 10, 최대 50
- **동시 크롤링 제한**: **TODO** (현재 미정, 제안: 최대 100개)

**안정성**:
- Retry 전략: 크롤링 태스크 2회, Outbox 5회
- Dead Letter Queue: 최종 실패 메시지 보관
- Health Check: `/actuator/health`

---

## 🧪 테스트 전략

### Unit Test

**Domain**:
- Seller Aggregate 비즈니스 로직 (주기 변경, 상태 전환)
- CrawlerTask 상태 전환 로직
- UserAgent 토큰 버킷 리미터 (`canMakeRequest()`)
- Product 변경 감지 로직 (`hasChanged()`)

**Application**:
- RegisterSellerUseCase (Mock PersistencePort)
- TriggerCrawlingUseCase (Mock PersistencePort, Mock MustitApiCrawler)
- ProcessCrawlerTaskUseCase (Mock PersistencePort, Mock UserAgentPoolManager)

---

### Integration Test

**Persistence**:
- SellerJpaRepository CRUD 테스트 (TestContainers MySQL)
- CrawlerTaskQueryDslRepository 집계 쿼리 테스트
- UserAgent Pessimistic Lock 테스트

**REST API**:
- SellerApiController (MockMvc)
- Validation 테스트 (400 Bad Request)
- 인증/인가 테스트 (401, 403)

**Infrastructure**:
- EventBridge Rule 생성/업데이트 테스트 (Localstack)
- SQS 발행/폴링 테스트 (Localstack)

---

### E2E Test

- 셀러 등록 → 크롤링 트리거 → 태스크 처리 → 상품 저장 → Outbox 전송 플로우
- UserAgent 429 응답 처리 및 자동 복구

---

## 🚀 개발 계획

### Phase 1: Domain Layer (예상: 5일)
- [ ] Seller Aggregate 구현
- [ ] CrawlerTask Aggregate 구현
- [ ] UserAgent Aggregate 구현
- [ ] Product Aggregate 구현
- [ ] ProductOutbox Aggregate 구현
- [ ] Domain Unit Test (TestFixture 패턴)

### Phase 2: Application Layer (예상: 7일)
- [ ] RegisterSellerUseCase 구현
- [ ] TriggerCrawlingUseCase 구현
- [ ] ProcessCrawlerTaskUseCase 구현
- [ ] ProcessProductOutboxUseCase 구현
- [ ] UserAgentPoolManager 구현
- [ ] Command/Query DTO 구현
- [ ] Application Unit Test

### Phase 3: Persistence Layer (예상: 4일)
- [ ] JPA Entity 구현 (5개)
- [ ] JpaRepository 구현
- [ ] QueryDSL 쿼리 구현 (메트릭 집계)
- [ ] Integration Test (TestContainers)

### Phase 4: Infrastructure Layer (예상: 5일)
- [ ] EventBridge 연동 구현
- [ ] SQS 연동 구현 (Publisher, Consumer)
- [ ] 머스트잇 API 크롤러 구현
- [ ] 외부 상품 서버 연동 구현
- [ ] Infrastructure Integration Test (Localstack)

### Phase 5: REST API Layer (예상: 3일)
- [ ] SellerApiController 구현
- [ ] MetricsApiController 구현
- [ ] InternalCrawlingApiController 구현
- [ ] Exception Handling 구현
- [ ] REST API Integration Test (MockMvc)

### Phase 6: Scheduled Tasks (예상: 2일)
- [ ] Outbox 배치 처리 (5분마다)
- [ ] UserAgent 자동 복구 (1시간마다)
- [ ] 메트릭 집계 (일 단위)

### Phase 7: Integration Test (예상: 2일)
- [ ] End-to-End Test 작성
- [ ] 동시성 테스트 (UserAgent 할당, 태스크 처리)

**총 예상 기간**: 약 28일 (4주)

---

## 📚 참고 문서

- [Domain Layer 규칙](../coding_convention/02-domain-layer/)
- [Application Layer 규칙](../coding_convention/03-application-layer/)
- [Persistence Layer 규칙](../coding_convention/04-persistence-layer/)
- [REST API Layer 규칙](../coding_convention/01-adapter-rest-api-layer/)

---

## 🔄 TODO 항목 정리

다음 항목들은 추가 논의 및 결정이 필요합니다:

1. **셀러 주기 제한**: 최소/최대 크롤링 주기 (제안: 1-30일)
2. **해시 알고리즘**: 변경 감지용 해시 (제안: MD5)
3. **Outbox 배치 주기**: 외부 전송 주기 (제안: 5분)
4. **Outbox 재시도 횟수**: 최대 재시도 (제안: 5회)
5. **UserAgent 자동 복구**: SUSPENDED → ACTIVE 복귀 시점 (제안: 1시간 후)
6. **RDB 파티셔닝**: crawler_tasks 테이블 파티셔닝 전략 (제안: 월별)
7. **동시 크롤링 제한**: 최대 동시 처리 수 (제안: 100개)
8. **외부 API Timeout**: 크롤링 및 외부 전송 Timeout (제안: 5초)
9. **Peak TPS**: Peak Time 예상 TPS (제안: 100 TPS)
10. **모니터링 대시보드**: Grafana/CloudWatch 대시보드 필요 여부

---

## 다음 단계

1. **PRD 검토 및 TODO 항목 결정**
2. **Jira 티켓 생성**: `/jira-from-prd docs/prd/mustit-seller-crawler.md`
3. **TDD 사이클 시작**: Domain Layer부터 시작

---

**승인 후 개발 시작 가능합니다!** 🎉
