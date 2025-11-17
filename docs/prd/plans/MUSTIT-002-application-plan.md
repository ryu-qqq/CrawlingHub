# MUSTIT-002 TDD Plan

**Task**: Application Layer 구현
**Layer**: Application Layer
**브랜치**: feature/MUSTIT-002-application
**예상 소요 시간**: 600분 (40 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ RegisterSellerCommand DTO 구현 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] `RegisterSellerCommandTest.java` 생성
- [ ] `shouldCreateCommandWithValidData()` 작성
- [ ] `shouldRejectInvalidSellerId()` 작성
- [ ] `shouldRejectNegativeInterval()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: RegisterSellerCommand DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RegisterSellerCommand.java` 생성 (Record)
- [ ] 필드: sellerId, name, crawlingIntervalDays
- [ ] 검증 로직 추가 (sellerId 빈 값 체크, intervalDays > 0)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: RegisterSellerCommand DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Validation 메시지 명확화
- [ ] ArchUnit 테스트 추가 (Command DTO 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: RegisterSellerCommand DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `RegisterSellerCommandFixture.java` 생성
- [ ] `aRegisterSellerCommand()` 메서드 작성
- [ ] 테스트 → Fixture 사용으로 리팩토링
- [ ] 커밋: `test: RegisterSellerCommandFixture 정리 (Tidy)`

---

### 2️⃣ SellerResponse DTO 구현 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `SellerResponseTest.java` 생성
- [ ] `shouldCreateResponseFromDomain()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerResponse.java` 생성 (Record)
- [ ] 필드: sellerId, name, status, crawlingIntervalDays, totalProductCount, createdAt, updatedAt
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SellerResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Response DTO 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SellerResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SellerResponseFixture.java` 생성
- [ ] `aSellerResponse()` 메서드 작성
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: SellerResponseFixture 정리 (Tidy)`

---

### 3️⃣ SellerAssembler 구현 (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `SellerAssemblerTest.java` 생성
- [ ] `shouldConvertDomainToResponse()` 작성
- [ ] Mock 없이 실제 Seller Aggregate 사용
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerAssembler 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerAssembler.java` 생성
- [ ] `toResponse(Seller)` 메서드 구현
- [ ] Seller → SellerResponse 변환 로직
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SellerAssembler 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Assembler 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SellerAssembler 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 코드 정리 (Fixture 사용)
- [ ] 커밋: `test: SellerAssembler 테스트 정리 (Tidy)`

---

### 4️⃣ SellerCommandPort 인터페이스 정의 (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `SellerCommandPortTest.java` 생성 (Mock 테스트)
- [ ] Port 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerCommandPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerCommandPort.java` 인터페이스 생성
- [ ] `save(Seller seller)` 메서드 정의
- [ ] `delete(String sellerId)` 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SellerCommandPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가
- [ ] ArchUnit 테스트 추가 (Port 명명 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SellerCommandPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 생성 (필요 시)
- [ ] 커밋: `test: SellerCommandPort 테스트 정리 (Tidy)`

---

### 5️⃣ SellerQueryPort 인터페이스 정의 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `SellerQueryPortTest.java` 생성
- [ ] Port 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerQueryPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerQueryPort.java` 인터페이스 생성
- [ ] `findById(String sellerId)` 메서드 정의
- [ ] `findByStatus(SellerStatus status, Pageable pageable)` 메서드 정의
- [ ] `existsBySellerId(String sellerId)` 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SellerQueryPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SellerQueryPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 정리
- [ ] 커밋: `test: SellerQueryPort 테스트 정리 (Tidy)`

---

### 6️⃣ EventBridgePort 인터페이스 정의 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `EventBridgePortTest.java` 생성
- [ ] `createRule()` 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: EventBridgePort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `EventBridgePort.java` 인터페이스 생성
- [ ] `createRule(String sellerId, int intervalDays)` 메서드 정의
- [ ] `updateRule(String sellerId, int newIntervalDays)` 메서드 정의
- [ ] `deleteRule(String sellerId)` 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: EventBridgePort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: EventBridgePort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 정리
- [ ] 커밋: `test: EventBridgePort 테스트 정리 (Tidy)`

---

### 7️⃣ RegisterSellerUseCase 인터페이스 정의 (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `RegisterSellerUseCaseTest.java` 생성 (Mock 테스트)
- [ ] Use Case 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: RegisterSellerUseCase 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RegisterSellerUseCase.java` 인터페이스 생성
- [ ] `registerSeller(RegisterSellerCommand command)` 메서드 정의
- [ ] 반환 타입: `SellerResponse`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: RegisterSellerUseCase 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가 (비즈니스 로직 설명)
- [ ] ArchUnit 테스트 추가 (Input Port 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: RegisterSellerUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock UseCase Fixture 생성
- [ ] 커밋: `test: RegisterSellerUseCase 테스트 정리 (Tidy)`

---

### 8️⃣ RegisterSellerUseCaseImpl 구현 - Part 1 (중복 체크) (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `RegisterSellerUseCaseImplTest.java` 생성
- [ ] Mock Port 준비 (SellerQueryPort, SellerCommandPort, EventBridgePort)
- [ ] `shouldThrowExceptionWhenDuplicateSellerId()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 중복 셀러 ID 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RegisterSellerUseCaseImpl.java` 생성
- [ ] `@Service` 어노테이션 추가
- [ ] Port 의존성 주입 (생성자)
- [ ] 중복 체크 로직만 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 중복 셀러 ID 검증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 예외 메시지 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 중복 검증 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 사용
- [ ] 커밋: `test: 중복 검증 테스트 정리 (Tidy)`

---

### 9️⃣ RegisterSellerUseCaseImpl 구현 - Part 2 (Seller 생성 및 저장) (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `shouldRegisterSellerSuccessfully()` 작성
- [ ] Mock 동작 정의 (save, createRule)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 셀러 등록 성공 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Seller.register() 호출
- [ ] sellerCommandPort.save() 호출
- [ ] SellerAssembler.toResponse() 호출
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 셀러 등록 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 코드 가독성 개선
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 셀러 등록 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: 셀러 등록 테스트 정리 (Tidy)`

---

### 🔟 RegisterSellerUseCaseImpl 구현 - Part 3 (Transaction 경계 검증) (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCallEventBridgeAfterTransactionCommit()` 작성
- [ ] Transaction 경계 검증 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Transaction 경계 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `@Transactional` 어노테이션 추가
- [ ] EventBridge 호출을 트랜잭션 밖으로 이동
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Transaction 경계 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 주석 추가
- [ ] ArchUnit 테스트 추가 (Transaction 경계 검증)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Transaction 경계 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 코드 정리
- [ ] 커밋: `test: Transaction 경계 테스트 정리 (Tidy)`

---

### 1️⃣1️⃣ UpdateSellerIntervalCommand DTO 구현 (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `UpdateSellerIntervalCommandTest.java` 생성
- [ ] `shouldCreateCommandWithValidData()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: UpdateSellerIntervalCommand DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UpdateSellerIntervalCommand.java` 생성 (Record)
- [ ] 필드: sellerId, newIntervalDays
- [ ] 검증 로직 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: UpdateSellerIntervalCommand DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: UpdateSellerIntervalCommand DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `UpdateSellerIntervalCommandFixture.java` 생성
- [ ] 커밋: `test: UpdateSellerIntervalCommandFixture 정리 (Tidy)`

---

### 1️⃣2️⃣ UpdateSellerIntervalUseCaseImpl 구현 (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `UpdateSellerIntervalUseCaseImplTest.java` 생성
- [ ] `shouldUpdateIntervalSuccessfully()` 작성
- [ ] Mock Port 준비
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 셀러 주기 변경 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UpdateSellerIntervalUseCaseImpl.java` 생성
- [ ] Seller 조회 → changeInterval() 호출 → 저장
- [ ] EventBridge 업데이트 (트랜잭션 밖)
- [ ] `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 셀러 주기 변경 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 셀러 주기 변경 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: 셀러 주기 변경 테스트 정리 (Tidy)`

---

### 1️⃣3️⃣ CrawlerTaskCommandPort 인터페이스 정의 (Cycle 13)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlerTaskCommandPortTest.java` 생성
- [ ] Bulk Insert 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: CrawlerTaskCommandPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlerTaskCommandPort.java` 인터페이스 생성
- [ ] `save(CrawlerTask task)` 메서드 정의
- [ ] `saveAll(List<CrawlerTask> tasks)` 메서드 정의 (Bulk Insert)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlerTaskCommandPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlerTaskCommandPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 정리
- [ ] 커밋: `test: CrawlerTaskCommandPort 테스트 정리 (Tidy)`

---

### 1️⃣4️⃣ MustitApiPort 인터페이스 정의 (Cycle 14)

#### 🔴 Red: 테스트 작성
- [ ] `MustitApiPortTest.java` 생성
- [ ] 크롤링 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: MustitApiPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `MustitApiPort.java` 인터페이스 생성
- [ ] `crawlMinishop(String sellerId, int page)` 메서드 정의
- [ ] `crawlProductDetail(String itemNo)` 메서드 정의
- [ ] `crawlProductOption(String itemNo)` 메서드 정의
- [ ] 반환 타입: DTO 정의 (MinishopResponse, ProductDetailResponse, ProductOptionResponse)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: MustitApiPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: MustitApiPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 정리
- [ ] 커밋: `test: MustitApiPort 테스트 정리 (Tidy)`

---

### 1️⃣5️⃣ TriggerCrawlingCommand DTO 구현 (Cycle 15)

#### 🔴 Red: 테스트 작성
- [ ] `TriggerCrawlingCommandTest.java` 생성
- [ ] `shouldCreateCommandWithValidData()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: TriggerCrawlingCommand DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `TriggerCrawlingCommand.java` 생성 (Record)
- [ ] 필드: sellerId
- [ ] 검증 로직 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: TriggerCrawlingCommand DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: TriggerCrawlingCommand DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `TriggerCrawlingCommandFixture.java` 생성
- [ ] 커밋: `test: TriggerCrawlingCommandFixture 정리 (Tidy)`

---

### 1️⃣6️⃣ CrawlingTriggeredResponse DTO 구현 (Cycle 16)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlingTriggeredResponseTest.java` 생성
- [ ] `shouldCreateResponseWithTaskCount()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: CrawlingTriggeredResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlingTriggeredResponse.java` 생성 (Record)
- [ ] 필드: taskCount
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlingTriggeredResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlingTriggeredResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `CrawlingTriggeredResponseFixture.java` 생성
- [ ] 커밋: `test: CrawlingTriggeredResponseFixture 정리 (Tidy)`

---

### 1️⃣7️⃣ TriggerCrawlingUseCaseImpl 구현 - Part 1 (Seller 조회 및 상태 검증) (Cycle 17)

#### 🔴 Red: 테스트 작성
- [ ] `TriggerCrawlingUseCaseImplTest.java` 생성
- [ ] `shouldThrowExceptionWhenSellerNotActive()` 작성
- [ ] Mock Port 준비
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 크롤링 트리거 상태 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `TriggerCrawlingUseCaseImpl.java` 생성
- [ ] Seller 조회 → ACTIVE 상태 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 크롤링 트리거 상태 검증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 예외 메시지 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 크롤링 트리거 상태 검증 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 사용
- [ ] 커밋: `test: 크롤링 트리거 상태 검증 테스트 정리 (Tidy)`

---

### 1️⃣8️⃣ TriggerCrawlingUseCaseImpl 구현 - Part 2 (미니샵 조회 및 태스크 생성) (Cycle 18)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateTasksSuccessfully()` 작성
- [ ] Mock 동작 정의 (mustitApiPort.crawlMinishop)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 크롤링 태스크 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 미니샵 API 호출 (트랜잭션 내, 빠른 조회)
- [ ] 총 상품 수 업데이트 (Seller Aggregate)
- [ ] 페이지 수 계산: `Math.ceil(totalProductCount / 500)`
- [ ] CrawlerTask 생성 (IntStream 사용)
- [ ] Bulk Insert (crawlerTaskCommandPort.saveAll)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 크롤링 태스크 생성 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] `@Transactional` 추가
- [ ] Bulk Insert 최적화 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 크롤링 태스크 생성 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: 크롤링 태스크 생성 테스트 정리 (Tidy)`

---

### 1️⃣9️⃣ SqsPublisherPort 인터페이스 정의 (Cycle 19)

#### 🔴 Red: 테스트 작성
- [ ] `SqsPublisherPortTest.java` 생성
- [ ] Batch Send 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SqsPublisherPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SqsPublisherPort.java` 인터페이스 생성
- [ ] `sendBatch(List<CrawlerTask> tasks)` 메서드 정의
- [ ] 반환 타입: `void` 또는 `SqsPublishResult`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SqsPublisherPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가 (Batch 크기 10개 제한 명시)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SqsPublisherPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 정리
- [ ] 커밋: `test: SqsPublisherPort 테스트 정리 (Tidy)`

---

### 2️⃣0️⃣ PublishCrawlerTasksUseCaseImpl 구현 (Cycle 20)

#### 🔴 Red: 테스트 작성
- [ ] `PublishCrawlerTasksUseCaseImplTest.java` 생성
- [ ] `shouldPublishTasksSuccessfully()` 작성
- [ ] Mock Port 준비 (CrawlerTaskQueryPort, CrawlerTaskCommandPort, SqsPublisherPort)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 크롤러 태스크 발행 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `PublishCrawlerTasksUseCaseImpl.java` 생성
- [ ] WAITING 상태 태스크 조회 → PUBLISHED로 변경 → 저장
- [ ] `@Transactional` 추가
- [ ] SQS 발행 (트랜잭션 밖, Batch Send)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 크롤러 태스크 발행 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증
- [ ] Batch 크기 10개 제한 확인
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 크롤러 태스크 발행 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: 크롤러 태스크 발행 테스트 정리 (Tidy)`

---

### 2️⃣1️⃣ UserAgentQueryPort 인터페이스 정의 (Cycle 21)

#### 🔴 Red: 테스트 작성
- [ ] `UserAgentQueryPortTest.java` 생성
- [ ] Pessimistic Lock 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: UserAgentQueryPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UserAgentQueryPort.java` 인터페이스 생성
- [ ] `findById(String userAgentId)` 메서드 정의
- [ ] `findByStatus(UserAgentStatus status)` 메서드 정의
- [ ] `findFirstActiveForUpdate()` 메서드 정의 (Pessimistic Lock, `SELECT FOR UPDATE`)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: UserAgentQueryPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가 (Pessimistic Lock 설명)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: UserAgentQueryPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 정리
- [ ] 커밋: `test: UserAgentQueryPort 테스트 정리 (Tidy)`

---

### 2️⃣2️⃣ UserAgentPoolManager 구현 - Part 1 (assignUserAgent with Health-based Selection) (Cycle 22)

#### 🔴 Red: 테스트 작성
```java
// application/src/test/java/.../manager/UserAgentPoolManagerTest.java
@Test
void shouldAssignUserAgentWithTokenAndNoRateLimit() {
    // 1순위: ACTIVE + Token 보유 + Redis 제한 미초과
    UserAgent userAgent = UserAgentFixture.activeWithToken();
    when(userAgentQueryPort.findFirstActiveWithToken()).thenReturn(Optional.of(userAgent));
    when(redisTokenBucketPort.canMakeRequest(any())).thenReturn(true);

    UserAgent assigned = userAgentPoolManager.assignUserAgent();

    assertThat(assigned).isEqualTo(userAgent);
}

@Test
void shouldAssignUserAgentWithoutTokenAndIssueToken() {
    // 2순위: ACTIVE + Token 없음 → 자동 발급 시도
    UserAgent userAgent = UserAgentFixture.activeWithoutToken();
    when(userAgentQueryPort.findFirstActiveWithToken()).thenReturn(Optional.empty());
    when(userAgentQueryPort.findFirstActiveWithoutToken()).thenReturn(Optional.of(userAgent));
    when(mustitApiPort.issueToken(any())).thenReturn("new-token-123");

    UserAgent assigned = userAgentPoolManager.assignUserAgent();

    assertThat(assigned.getToken()).isNotNull();
    verify(userAgentCommandPort).save(userAgent);
}

@Test
void shouldBlockUserAgentAfter3FailedTokenIssues() {
    // 토큰 발급 3회 실패 → block()
    UserAgent userAgent = UserAgentFixture.activeWithoutToken();
    when(userAgentQueryPort.findFirstActiveWithoutToken()).thenReturn(Optional.of(userAgent));
    when(mustitApiPort.issueToken(any())).thenThrow(new TokenIssueException("Failed"));

    assertThatThrownBy(() -> userAgentPoolManager.assignUserAgent())
        .isInstanceOf(CircuitOpenException.class);

    verify(userAgent, times(3)).block();  // 3회 시도 후 차단
}

@Test
void shouldThrowCircuitOpenWhenAvailableRateBelow20Percent() {
    // Available Rate < 20% → CircuitOpenException
    when(userAgentQueryPort.countByStatusIn(ACTIVE)).thenReturn(10);
    when(userAgentQueryPort.count()).thenReturn(100);  // 10% available

    assertThatThrownBy(() -> userAgentPoolManager.assignUserAgent())
        .isInstanceOf(CircuitOpenException.class)
        .hasMessageContaining("UserAgent 풀 가용률이 20% 미만입니다");
}
```
- [ ] `UserAgentPoolManagerTest.java` 생성
- [ ] Health 기반 선택 알고리즘 테스트 추가
- [ ] 토큰 자동 발급 테스트 추가
- [ ] 서킷 브레이커 테스트 추가
- [ ] 커밋: `test: UserAgent Health 기반 할당 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
// application/src/main/java/.../manager/UserAgentPoolManager.java
@Service
public class UserAgentPoolManager {
    private static final int MAX_TOKEN_ISSUE_RETRY = 3;
    private static final double CIRCUIT_BREAKER_THRESHOLD = 20.0;

    private final UserAgentQueryPort userAgentQueryPort;
    private final UserAgentCommandPort userAgentCommandPort;
    private final RedisTokenBucketPort redisTokenBucketPort;
    private final MustitApiPort mustitApiPort;

    @Transactional
    public UserAgent assignUserAgent() {
        // 서킷 브레이커 검증
        checkCircuitBreaker();

        // 1순위: ACTIVE + Token 보유 + Redis 제한 미초과
        Optional<UserAgent> withToken = userAgentQueryPort.findFirstActiveWithToken();
        if (withToken.isPresent() && redisTokenBucketPort.canMakeRequest(withToken.get().getUserAgentId())) {
            return withToken.get();
        }

        // 2순위: ACTIVE + Token 없음 → 자동 발급 시도
        Optional<UserAgent> withoutToken = userAgentQueryPort.findFirstActiveWithoutToken();
        if (withoutToken.isPresent()) {
            UserAgent userAgent = withoutToken.get();
            tryIssueTokenWithRetry(userAgent);
            userAgentCommandPort.save(userAgent);
            return userAgent;
        }

        throw new NoAvailableUserAgentException("사용 가능한 UserAgent가 없습니다");
    }

    private void checkCircuitBreaker() {
        int available = userAgentQueryPort.countByStatusIn(List.of(UserAgentStatus.ACTIVE));
        int total = userAgentQueryPort.count();
        double availableRate = (double) available / total * 100;

        if (availableRate < CIRCUIT_BREAKER_THRESHOLD) {
            throw new CircuitOpenException(
                "UserAgent 풀 가용률이 " + CIRCUIT_BREAKER_THRESHOLD + "% 미만입니다 (현재: " + availableRate + "%)"
            );
        }
    }

    private void tryIssueTokenWithRetry(UserAgent userAgent) {
        for (int attempt = 1; attempt <= MAX_TOKEN_ISSUE_RETRY; attempt++) {
            try {
                String token = mustitApiPort.issueToken(userAgent.getUserAgentId().value());
                userAgent.issueToken(new Token(token));
                return;
            } catch (Exception e) {
                if (attempt == MAX_TOKEN_ISSUE_RETRY) {
                    userAgent.block();
                    userAgentCommandPort.save(userAgent);
                    throw new CircuitOpenException("토큰 발급 3회 실패로 UserAgent 차단됨", e);
                }
            }
        }
    }
}
```
- [ ] `UserAgentPoolManager.java` 생성
- [ ] Health 기반 선택 알고리즘 구현
- [ ] 토큰 자동 발급 로직 구현 (3회 재시도)
- [ ] 서킷 브레이커 정책 구현 (Available Rate < 20%)
- [ ] Redis Token Bucket 검증 통합
- [ ] 커밋: `feat: UserAgent Health 기반 할당 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증
- [ ] ArchUnit 테스트 추가 (외부 API 호출 검증)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: UserAgent 할당 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `struct: UserAgent 할당 테스트 정리 (Tidy)`

---

### 2️⃣3️⃣ UserAgentPoolManager 구현 - Part 2 (blockUserAgent) (Cycle 23)

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldBlockUserAgent() {
    String userAgentId = "ua_12345";
    UserAgent userAgent = UserAgentFixture.activeUserAgent();
    when(userAgentQueryPort.findById(userAgentId)).thenReturn(Optional.of(userAgent));

    userAgentPoolManager.blockUserAgent(userAgentId);

    assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.BLOCKED);
    verify(userAgentCommandPort).save(userAgent);
}

@Test
void shouldThrowExceptionWhenUserAgentNotFound() {
    String userAgentId = "ua_99999";
    when(userAgentQueryPort.findById(userAgentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userAgentPoolManager.blockUserAgent(userAgentId))
        .isInstanceOf(UserAgentNotFoundException.class);
}
```
- [ ] `shouldBlockUserAgent()` 테스트 작성
- [ ] 예외 케이스 테스트 추가
- [ ] 커밋: `test: UserAgent 차단 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
@Transactional
public void blockUserAgent(String userAgentId) {
    UserAgent userAgent = userAgentQueryPort.findById(userAgentId)
        .orElseThrow(() -> new UserAgentNotFoundException("UserAgent를 찾을 수 없습니다: " + userAgentId));

    userAgent.block();
    userAgentCommandPort.save(userAgent);
}
```
- [ ] `blockUserAgent()` 메서드 구현
- [ ] UserAgent 조회 → block() 호출 → 저장
- [ ] `@Transactional` 추가
- [ ] 커밋: `feat: UserAgent 차단 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 로직 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: UserAgent 차단 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `struct: UserAgent 차단 테스트 정리 (Tidy)`

---

### 2️⃣4️⃣ ProcessCrawlerTaskUseCaseImpl 구현 - Part 1 (상태 업데이트) (Cycle 24)

#### 🔴 Red: 테스트 작성
- [ ] `ProcessCrawlerTaskUseCaseImplTest.java` 생성
- [ ] `shouldUpdateTaskStatusToInProgress()` 작성
- [ ] Mock Port 준비
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 크롤러 태스크 상태 업데이트 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProcessCrawlerTaskUseCaseImpl.java` 생성
- [ ] PUBLISHED 상태 태스크 조회 → IN_PROGRESS로 변경 → 저장
- [ ] `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 크롤러 태스크 상태 업데이트 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증 (첫 번째 트랜잭션)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 크롤러 태스크 상태 업데이트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: 크롤러 태스크 상태 업데이트 테스트 정리 (Tidy)`

---

### 2️⃣5️⃣ ProcessCrawlerTaskUseCaseImpl 구현 - Part 2 (크롤링 실행) (Cycle 25)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCrawlSuccessfully()` 작성
- [ ] Mock 동작 정의 (UserAgentPoolManager, MustitApiPort)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 크롤링 실행 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] UserAgent 할당 (UserAgentPoolManager)
- [ ] 머스트잇 API 호출 (mustitApiPort.crawl...)
- [ ] 응답 데이터 파싱
- [ ] 트랜잭션 밖에서 실행
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 크롤링 실행 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증 (크롤링은 트랜잭션 밖)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 크롤링 실행 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: 크롤링 실행 테스트 정리 (Tidy)`

---

### 2️⃣6️⃣ ProcessCrawlerTaskUseCaseImpl 구현 - Part 3 (결과 저장 및 재시도 로직) (Cycle 26)

#### 🔴 Red: 테스트 작성
- [ ] `shouldRetryWhenCrawlingFails()` 작성
- [ ] retryCount < 2 시나리오 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 크롤링 재시도 로직 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 크롤링 결과에 따라 상태 변경
  - 성공: COMPLETED
  - 실패 (retryCount < 2): RETRY
  - 실패 (retryCount >= 2): FAILED
- [ ] 두 번째 `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 크롤링 재시도 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 2단계 Transaction 경계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 크롤링 재시도 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: 크롤링 재시도 로직 테스트 정리 (Tidy)`

---

### 2️⃣7️⃣ ProductCommandPort 인터페이스 정의 (Cycle 27)

#### 🔴 Red: 테스트 작성
- [ ] `ProductCommandPortTest.java` 생성
- [ ] Port 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProductCommandPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProductCommandPort.java` 인터페이스 생성
- [ ] `save(Product product)` 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ProductCommandPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ProductCommandPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 정리
- [ ] 커밋: `test: ProductCommandPort 테스트 정리 (Tidy)`

---

### 2️⃣8️⃣ ProductOutboxCommandPort 인터페이스 정의 (Cycle 28)

#### 🔴 Red: 테스트 작성
- [ ] `ProductOutboxCommandPortTest.java` 생성
- [ ] Port 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProductOutboxCommandPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProductOutboxCommandPort.java` 인터페이스 생성
- [ ] `save(ProductOutbox outbox)` 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ProductOutboxCommandPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ProductOutboxCommandPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 정리
- [ ] 커밋: `test: ProductOutboxCommandPort 테스트 정리 (Tidy)`

---

### 2️⃣9️⃣ ProcessCrawlerTaskUseCaseImpl 구현 - Part 4 (Product 업데이트 및 Outbox 생성) (Cycle 29)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateProductOutboxWhenProductChanged()` 작성
- [ ] 해시 계산 및 변경 감지 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Product 변경 감지 및 Outbox 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Product 해시 계산 (Product Aggregate 메서드)
- [ ] 기존 Product 조회 → 해시 비교 → 변경 감지
- [ ] 변경 감지 시 ProductOutbox 생성
- [ ] Product 저장 + Outbox 저장
- [ ] 두 번째 트랜잭션 내에서 실행
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Product 변경 감지 및 Outbox 생성 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 해시 계산 로직 검증
- [ ] Outbox 패턴 적용 확인
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Product 변경 감지 및 Outbox 생성 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: Product 변경 감지 및 Outbox 생성 테스트 정리 (Tidy)`

---

### 3️⃣0️⃣ ExternalProductApiPort 인터페이스 정의 (Cycle 30)

#### 🔴 Red: 테스트 작성
- [ ] `ExternalProductApiPortTest.java` 생성
- [ ] 외부 API 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ExternalProductApiPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ExternalProductApiPort.java` 인터페이스 생성
- [ ] `sendProductCreated(ProductOutbox outbox)` 메서드 정의
- [ ] `sendProductUpdated(ProductOutbox outbox)` 메서드 정의
- [ ] 반환 타입: `ExternalApiResponse`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ExternalProductApiPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가 (Timeout 5초 명시)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ExternalProductApiPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 정리
- [ ] 커밋: `test: ExternalProductApiPort 테스트 정리 (Tidy)`

---

### 3️⃣1️⃣ ProcessProductOutboxUseCaseImpl 구현 - Part 1 (상태 업데이트) (Cycle 31)

#### 🔴 Red: 테스트 작성
- [ ] `ProcessProductOutboxUseCaseImplTest.java` 생성
- [ ] `shouldUpdateOutboxStatusToSending()` 작성
- [ ] Mock Port 준비
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Outbox 상태 업데이트 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProcessProductOutboxUseCaseImpl.java` 생성
- [ ] WAITING 상태 Outbox 조회 (최대 100개)
- [ ] SENDING 상태로 변경 → 저장
- [ ] `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Outbox 상태 업데이트 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증 (첫 번째 트랜잭션)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Outbox 상태 업데이트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: Outbox 상태 업데이트 테스트 정리 (Tidy)`

---

### 3️⃣2️⃣ ProcessProductOutboxUseCaseImpl 구현 - Part 2 (외부 전송 및 결과 처리) (Cycle 32)

#### 🔴 Red: 테스트 작성
- [ ] `shouldSendProductToExternalApiSuccessfully()` 작성
- [ ] `shouldRetryWhenExternalApiFails()` 작성
- [ ] Mock 동작 정의 (ExternalProductApiPort)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 외부 API 전송 및 재시도 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 외부 API 호출 (트랜잭션 밖, Timeout 5초)
- [ ] 결과에 따라 상태 변경
  - 성공: COMPLETED, sentAt 기록
  - 실패: retryCount 증가, WAITING 또는 FAILED
- [ ] 두 번째 `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 외부 API 전송 및 재시도 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 2단계 Transaction 경계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 외부 API 전송 및 재시도 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: 외부 API 전송 및 재시도 테스트 정리 (Tidy)`

---

### 3️⃣3️⃣ GetSellerQuery DTO 구현 (Cycle 33)

#### 🔴 Red: 테스트 작성
- [ ] `GetSellerQueryTest.java` 생성
- [ ] `shouldCreateQueryWithValidData()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: GetSellerQuery DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetSellerQuery.java` 생성 (Record)
- [ ] 필드: sellerId
- [ ] 검증 로직 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GetSellerQuery DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Query DTO 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GetSellerQuery DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `GetSellerQueryFixture.java` 생성
- [ ] 커밋: `test: GetSellerQueryFixture 정리 (Tidy)`

---

### 3️⃣4️⃣ SellerDetailResponse DTO 구현 (Cycle 34)

#### 🔴 Red: 테스트 작성
- [ ] `SellerDetailResponseTest.java` 생성
- [ ] `shouldCreateDetailResponseFromDomain()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerDetailResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerDetailResponse.java` 생성 (Record)
- [ ] SellerResponse와 동일 + 추가 상세 정보
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SellerDetailResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SellerDetailResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SellerDetailResponseFixture.java` 생성
- [ ] 커밋: `test: SellerDetailResponseFixture 정리 (Tidy)`

---

### 3️⃣5️⃣ GetSellerUseCaseImpl 구현 (Cycle 35)

#### 🔴 Red: 테스트 작성
- [ ] `GetSellerUseCaseImplTest.java` 생성
- [ ] `shouldGetSellerSuccessfully()` 작성
- [ ] Mock Port 준비 (SellerQueryPort)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 셀러 조회 UseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetSellerUseCaseImpl.java` 생성
- [ ] Seller 조회 → SellerAssembler.toDetailResponse()
- [ ] `@Transactional(readOnly = true)` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 셀러 조회 UseCase 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ReadOnly Transaction 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 셀러 조회 UseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: 셀러 조회 UseCase 테스트 정리 (Tidy)`

---

### 3️⃣6️⃣ ListSellersQuery DTO 및 PageResponse 구현 (Cycle 36)

#### 🔴 Red: 테스트 작성
- [ ] `ListSellersQueryTest.java` 생성
- [ ] `shouldCreateQueryWithPaging()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ListSellersQuery 및 PageResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ListSellersQuery.java` 생성 (Record)
- [ ] 필드: status (Nullable), page, size
- [ ] `PageResponse<T>` Generic DTO 생성
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ListSellersQuery 및 PageResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ListSellersQuery 및 PageResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ListSellersQueryFixture.java` 생성
- [ ] 커밋: `test: ListSellersQueryFixture 정리 (Tidy)`

---

### 3️⃣7️⃣ ListSellersUseCaseImpl 구현 (Cycle 37)

#### 🔴 Red: 테스트 작성
- [ ] `ListSellersUseCaseImplTest.java` 생성
- [ ] `shouldListSellersWithPaging()` 작성
- [ ] Mock Port 준비
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 셀러 목록 조회 UseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ListSellersUseCaseImpl.java` 생성
- [ ] Seller 목록 조회 (Pageable) → PageResponse 변환
- [ ] `@Transactional(readOnly = true)` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 셀러 목록 조회 UseCase 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ReadOnly Transaction 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 셀러 목록 조회 UseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: 셀러 목록 조회 UseCase 테스트 정리 (Tidy)`

---

### 3️⃣8️⃣ GetCrawlingMetricsQuery 및 Response DTO 구현 (Cycle 38)

#### 🔴 Red: 테스트 작성
- [ ] `GetCrawlingMetricsQueryTest.java` 생성
- [ ] `CrawlingMetricsResponseTest.java` 생성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: GetCrawlingMetricsQuery 및 Response DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetCrawlingMetricsQuery.java` 생성 (Record)
- [ ] 필드: sellerId, date
- [ ] `CrawlingMetricsResponse.java` 생성 (Record)
- [ ] 필드: sellerId, date, successRate, progressRate, taskStats
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GetCrawlingMetricsQuery 및 Response DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GetCrawlingMetricsQuery 및 Response DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 생성
- [ ] 커밋: `test: GetCrawlingMetricsQuery 및 Response Fixture 정리 (Tidy)`

---

### 3️⃣9️⃣ GetCrawlingMetricsUseCaseImpl 구현 (Cycle 39)

#### 🔴 Red: 테스트 작성
- [ ] `GetCrawlingMetricsUseCaseImplTest.java` 생성
- [ ] `shouldCalculateMetricsSuccessfully()` 작성
- [ ] Mock Port 준비 (CrawlerTaskQueryPort, SellerQueryPort)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 크롤링 메트릭 조회 UseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetCrawlingMetricsUseCaseImpl.java` 생성
- [ ] 자정 기준 (00:00-24:00) 태스크 조회
- [ ] 성공률 계산: 성공 태스크 / 전체 태스크 * 100
- [ ] 진행률 계산: 완료 상품 / 셀러 총 상품 수 * 100
- [ ] 태스크 통계: COMPLETED, FAILED, IN_PROGRESS 개수
- [ ] `@Transactional(readOnly = true)` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 크롤링 메트릭 조회 UseCase 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 계산 로직 정확성 검증
- [ ] ReadOnly Transaction 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 크롤링 메트릭 조회 UseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: 크롤링 메트릭 조회 UseCase 테스트 정리 (Tidy)`

---

### 4️⃣0️⃣ GetUserAgentPoolStatusUseCaseImpl 구현 (Cycle 40)

#### 🔴 Red: 테스트 작성
- [ ] `GetUserAgentPoolStatusUseCaseImplTest.java` 생성
- [ ] `shouldGetPoolStatusSuccessfully()` 작성
- [ ] Mock Port 준비 (UserAgentQueryPort)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: UserAgent 풀 상태 조회 UseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetUserAgentPoolStatusUseCaseImpl.java` 생성
- [ ] UserAgent 상태별 개수 조회
- [ ] UserAgentPoolStatusResponse 생성
- [ ] `@Transactional(readOnly = true)` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: UserAgent 풀 상태 조회 UseCase 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ReadOnly Transaction 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: UserAgent 풀 상태 조회 UseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: UserAgent 풀 상태 조회 UseCase 테스트 정리 (Tidy)`

---

## 📅 스케줄러 관련 UseCases (Cycle 41-68)

### 4️⃣1️⃣ RegisterCrawlingScheduleCommand DTO 구현 (Cycle 41)

#### 🔴 Red: 테스트 작성
- [ ] `RegisterCrawlingScheduleCommandTest.java` 생성
- [ ] `shouldCreateCommandWithValidData()` 작성
- [ ] `shouldRejectInvalidSellerId()` 작성
- [ ] `shouldRejectInvalidScheduleExpression()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: RegisterCrawlingScheduleCommand DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RegisterCrawlingScheduleCommand.java` 생성 (Record)
- [ ] 필드: sellerId, scheduleExpression (Cron 표현식)
- [ ] 검증 로직 추가 (sellerId 빈 값 체크, scheduleExpression 형식 검증)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: RegisterCrawlingScheduleCommand DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Validation 메시지 명확화
- [ ] ArchUnit 테스트 추가 (Command DTO 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: RegisterCrawlingScheduleCommand DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `RegisterCrawlingScheduleCommandFixture.java` 생성
- [ ] `aRegisterCrawlingScheduleCommand()` 메서드 작성
- [ ] 테스트 → Fixture 사용으로 리팩토링
- [ ] 커밋: `test: RegisterCrawlingScheduleCommandFixture 정리 (Tidy)`

---

### 4️⃣2️⃣ CrawlingScheduleResponse DTO 구현 (Cycle 42)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlingScheduleResponseTest.java` 생성
- [ ] `shouldCreateResponseFromDomain()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: CrawlingScheduleResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlingScheduleResponse.java` 생성 (Record)
- [ ] 필드: scheduleId, sellerId, scheduleRule, scheduleExpression, status, createdAt, updatedAt
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlingScheduleResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Response DTO 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlingScheduleResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `CrawlingScheduleResponseFixture.java` 생성
- [ ] `aCrawlingScheduleResponse()` 메서드 작성
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: CrawlingScheduleResponseFixture 정리 (Tidy)`

---

### 4️⃣3️⃣ CrawlingScheduleAssembler 구현 (Cycle 43)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlingScheduleAssemblerTest.java` 생성
- [ ] `shouldConvertDomainToResponse()` 작성
- [ ] Mock 없이 실제 CrawlingSchedule Aggregate 사용
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: CrawlingScheduleAssembler 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlingScheduleAssembler.java` 생성
- [ ] `toResponse(CrawlingSchedule)` 메서드 구현
- [ ] CrawlingSchedule → CrawlingScheduleResponse 변환 로직
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlingScheduleAssembler 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Assembler 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlingScheduleAssembler 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 코드 정리 (Fixture 사용)
- [ ] 커밋: `test: CrawlingScheduleAssembler 테스트 정리 (Tidy)`

---

### 4️⃣4️⃣ CrawlingScheduleCommandPort 인터페이스 정의 (Cycle 44)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlingScheduleCommandPortTest.java` 생성 (Mock 테스트)
- [ ] Port 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: CrawlingScheduleCommandPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlingScheduleCommandPort.java` 인터페이스 생성
- [ ] `save(CrawlingSchedule schedule)` 메서드 정의
- [ ] `delete(UUID scheduleId)` 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlingScheduleCommandPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가
- [ ] ArchUnit 테스트 추가 (Port 명명 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlingScheduleCommandPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 생성 (필요 시)
- [ ] 커밋: `test: CrawlingScheduleCommandPort 테스트 정리 (Tidy)`

---

### 4️⃣5️⃣ CrawlingScheduleQueryPort 인터페이스 정의 (Cycle 45)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlingScheduleQueryPortTest.java` 생성
- [ ] Port 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: CrawlingScheduleQueryPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlingScheduleQueryPort.java` 인터페이스 생성
- [ ] `findById(UUID scheduleId)` 메서드 정의
- [ ] `findBySellerId(String sellerId)` 메서드 정의
- [ ] `findByStatus(ScheduleStatus status, Pageable pageable)` 메서드 정의
- [ ] `existsBySellerId(String sellerId)` 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlingScheduleQueryPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlingScheduleQueryPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 정리
- [ ] 커밋: `test: CrawlingScheduleQueryPort 테스트 정리 (Tidy)`

---

### 4️⃣6️⃣ RegisterCrawlingScheduleUseCase 인터페이스 정의 (Cycle 46)

#### 🔴 Red: 테스트 작성
- [ ] `RegisterCrawlingScheduleUseCaseTest.java` 생성 (Mock 테스트)
- [ ] Use Case 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: RegisterCrawlingScheduleUseCase 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RegisterCrawlingScheduleUseCase.java` 인터페이스 생성
- [ ] `registerSchedule(RegisterCrawlingScheduleCommand command)` 메서드 정의
- [ ] 반환 타입: `CrawlingScheduleResponse`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: RegisterCrawlingScheduleUseCase 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가 (비즈니스 로직 설명)
- [ ] ArchUnit 테스트 추가 (Input Port 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: RegisterCrawlingScheduleUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock UseCase Fixture 생성
- [ ] 커밋: `test: RegisterCrawlingScheduleUseCase 테스트 정리 (Tidy)`

---

### 4️⃣7️⃣ RegisterCrawlingScheduleUseCaseImpl 구현 - Part 1 (중복 체크) (Cycle 47)

#### 🔴 Red: 테스트 작성
- [ ] `RegisterCrawlingScheduleUseCaseImplTest.java` 생성
- [ ] Mock Port 준비 (CrawlingScheduleQueryPort, CrawlingScheduleCommandPort, SchedulerOutboxCommandPort)
- [ ] `shouldThrowExceptionWhenDuplicateSellerId()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 중복 스케줄 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RegisterCrawlingScheduleUseCaseImpl.java` 생성
- [ ] `@Service` 어노테이션 추가
- [ ] Port 의존성 주입 (생성자)
- [ ] 중복 체크 로직만 구현 (existsBySellerId)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 중복 스케줄 검증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 예외 메시지 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 중복 검증 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 사용
- [ ] 커밋: `test: 중복 검증 테스트 정리 (Tidy)`

---

### 4️⃣8️⃣ RegisterCrawlingScheduleUseCaseImpl 구현 - Part 2 (스케줄 생성 및 저장) (Cycle 48)

#### 🔴 Red: 테스트 작성
- [ ] `shouldRegisterScheduleSuccessfully()` 작성
- [ ] Mock 동작 정의 (save)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 스케줄 등록 성공 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] CrawlingSchedule.create() 호출
- [ ] scheduleCommandPort.save() 호출
- [ ] CrawlingScheduleAssembler.toResponse() 호출
- [ ] `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 스케줄 등록 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 코드 가독성 개선
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 스케줄 등록 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: 스케줄 등록 테스트 정리 (Tidy)`

---

### 4️⃣9️⃣ RegisterCrawlingScheduleUseCaseImpl 구현 - Part 3 (SchedulerOutbox 생성) (Cycle 49)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateSchedulerOutboxWhenScheduleRegistered()` 작성
- [ ] SchedulerOutbox 생성 및 저장 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SchedulerOutbox 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] SchedulerOutbox.create() 호출
- [ ] EventType: SCHEDULE_REGISTERED
- [ ] Payload: JSON 형식 (scheduleId, sellerId, scheduleExpression)
- [ ] schedulerOutboxCommandPort.save() 호출
- [ ] 트랜잭션 내에서 실행
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SchedulerOutbox 생성 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Outbox 패턴 적용 확인
- [ ] JSON Payload 형식 검증
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SchedulerOutbox 생성 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: SchedulerOutbox 생성 테스트 정리 (Tidy)`

---

### 5️⃣0️⃣ UpdateCrawlingScheduleCommand DTO 및 UseCase 구현 (Cycle 50)

#### 🔴 Red: 테스트 작성
- [ ] `UpdateCrawlingScheduleCommandTest.java` 생성
- [ ] `UpdateCrawlingScheduleUseCaseImplTest.java` 생성
- [ ] `shouldUpdateScheduleSuccessfully()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 스케줄 업데이트 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UpdateCrawlingScheduleCommand.java` 생성 (Record)
- [ ] 필드: scheduleId, newScheduleExpression
- [ ] `UpdateCrawlingScheduleUseCaseImpl.java` 생성
- [ ] CrawlingSchedule 조회 → updateScheduleExpression() 호출 → 저장
- [ ] SchedulerOutbox 생성 (EventType: SCHEDULE_UPDATED)
- [ ] `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 스케줄 업데이트 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 스케줄 업데이트 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 생성
- [ ] 커밋: `test: 스케줄 업데이트 테스트 정리 (Tidy)`

---

### 5️⃣1️⃣ DeactivateCrawlingScheduleCommand DTO 및 UseCase 구현 (Cycle 51)

#### 🔴 Red: 테스트 작성
- [ ] `DeactivateCrawlingScheduleCommandTest.java` 생성
- [ ] `DeactivateCrawlingScheduleUseCaseImplTest.java` 생성
- [ ] `shouldDeactivateScheduleSuccessfully()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 스케줄 비활성화 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `DeactivateCrawlingScheduleCommand.java` 생성 (Record)
- [ ] 필드: scheduleId
- [ ] `DeactivateCrawlingScheduleUseCaseImpl.java` 생성
- [ ] CrawlingSchedule 조회 → deactivate() 호출 → 저장
- [ ] SchedulerOutbox 생성 (EventType: SCHEDULE_DEACTIVATED)
- [ ] `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 스케줄 비활성화 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 스케줄 비활성화 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 생성
- [ ] 커밋: `test: 스케줄 비활성화 테스트 정리 (Tidy)`

---

### 5️⃣2️⃣ GetCrawlingScheduleQuery DTO 및 UseCase 구현 (Cycle 52)

#### 🔴 Red: 테스트 작성
- [ ] `GetCrawlingScheduleQueryTest.java` 생성
- [ ] `GetCrawlingScheduleUseCaseImplTest.java` 생성
- [ ] `shouldGetScheduleSuccessfully()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 스케줄 조회 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetCrawlingScheduleQuery.java` 생성 (Record)
- [ ] 필드: scheduleId 또는 sellerId
- [ ] `GetCrawlingScheduleUseCaseImpl.java` 생성
- [ ] CrawlingSchedule 조회 → CrawlingScheduleAssembler.toResponse()
- [ ] `@Transactional(readOnly = true)` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 스케줄 조회 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ReadOnly Transaction 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 스케줄 조회 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 생성
- [ ] 커밋: `test: 스케줄 조회 테스트 정리 (Tidy)`

---

### 5️⃣3️⃣ CreateScheduleExecutionCommand DTO 구현 (Cycle 53)

#### 🔴 Red: 테스트 작성
- [ ] `CreateScheduleExecutionCommandTest.java` 생성
- [ ] `shouldCreateCommandWithValidData()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: CreateScheduleExecutionCommand DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CreateScheduleExecutionCommand.java` 생성 (Record)
- [ ] 필드: scheduleId, sellerId
- [ ] 검증 로직 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CreateScheduleExecutionCommand DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Command DTO 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CreateScheduleExecutionCommand DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `CreateScheduleExecutionCommandFixture.java` 생성
- [ ] 커밋: `test: CreateScheduleExecutionCommandFixture 정리 (Tidy)`

---

### 5️⃣4️⃣ ScheduleExecutionResponse DTO 구현 (Cycle 54)

#### 🔴 Red: 테스트 작성
- [ ] `ScheduleExecutionResponseTest.java` 생성
- [ ] `shouldCreateResponseFromDomain()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ScheduleExecutionResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ScheduleExecutionResponse.java` 생성 (Record)
- [ ] 필드: executionId, scheduleId, sellerId, status, totalTasksCreated, completedTasks, failedTasks, progressRate, successRate, startedAt, completedAt
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ScheduleExecutionResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Response DTO 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ScheduleExecutionResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ScheduleExecutionResponseFixture.java` 생성
- [ ] 커밋: `test: ScheduleExecutionResponseFixture 정리 (Tidy)`

---

### 5️⃣5️⃣ ScheduleExecutionAssembler 구현 (Cycle 55)

#### 🔴 Red: 테스트 작성
- [ ] `ScheduleExecutionAssemblerTest.java` 생성
- [ ] `shouldConvertDomainToResponse()` 작성
- [ ] Mock 없이 실제 CrawlingScheduleExecution Aggregate 사용
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ScheduleExecutionAssembler 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ScheduleExecutionAssembler.java` 생성
- [ ] `toResponse(CrawlingScheduleExecution)` 메서드 구현
- [ ] CrawlingScheduleExecution → ScheduleExecutionResponse 변환 로직
- [ ] Tell Don't Ask 원칙: getProgressRate(), getSuccessRate() 활용
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ScheduleExecutionAssembler 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Assembler 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ScheduleExecutionAssembler 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 코드 정리 (Fixture 사용)
- [ ] 커밋: `test: ScheduleExecutionAssembler 테스트 정리 (Tidy)`

---

### 5️⃣6️⃣ ScheduleExecutionCommandPort 인터페이스 정의 (Cycle 56)

#### 🔴 Red: 테스트 작성
- [ ] `ScheduleExecutionCommandPortTest.java` 생성
- [ ] Port 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ScheduleExecutionCommandPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ScheduleExecutionCommandPort.java` 인터페이스 생성
- [ ] `save(CrawlingScheduleExecution execution)` 메서드 정의
- [ ] `delete(UUID executionId)` 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ScheduleExecutionCommandPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ScheduleExecutionCommandPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 생성
- [ ] 커밋: `test: ScheduleExecutionCommandPort 테스트 정리 (Tidy)`

---

### 5️⃣7️⃣ ScheduleExecutionQueryPort 인터페이스 정의 (Cycle 57)

#### 🔴 Red: 테스트 작성
- [ ] `ScheduleExecutionQueryPortTest.java` 생성
- [ ] Port 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ScheduleExecutionQueryPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ScheduleExecutionQueryPort.java` 인터페이스 생성
- [ ] `findById(UUID executionId)` 메서드 정의
- [ ] `findByScheduleId(UUID scheduleId, Pageable pageable)` 메서드 정의
- [ ] `findByStatus(ExecutionStatus status, Pageable pageable)` 메서드 정의
- [ ] `countByStatus(ExecutionStatus status)` 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ScheduleExecutionQueryPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ScheduleExecutionQueryPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 정리
- [ ] 커밋: `test: ScheduleExecutionQueryPort 테스트 정리 (Tidy)`

---

### 5️⃣8️⃣ CreateScheduleExecutionUseCaseImpl 구현 (Cycle 58)

#### 🔴 Red: 테스트 작성
- [ ] `CreateScheduleExecutionUseCaseImplTest.java` 생성
- [ ] `shouldCreateExecutionSuccessfully()` 작성
- [ ] Mock Port 준비
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 스케줄 실행 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CreateScheduleExecutionUseCaseImpl.java` 생성
- [ ] CrawlingScheduleExecution.create() 호출
- [ ] scheduleExecutionCommandPort.save() 호출
- [ ] ScheduleExecutionAssembler.toResponse() 호출
- [ ] `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 스케줄 실행 생성 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 스케줄 실행 생성 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용
- [ ] 커밋: `test: 스케줄 실행 생성 테스트 정리 (Tidy)`

---

### 5️⃣9️⃣ UpdateExecutionProgressCommand DTO 및 UseCase 구현 (Cycle 59)

#### 🔴 Red: 테스트 작성
- [ ] `UpdateExecutionProgressCommandTest.java` 생성
- [ ] `UpdateExecutionProgressUseCaseImplTest.java` 생성
- [ ] `shouldUpdateProgressSuccessfully()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 실행 진행률 업데이트 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UpdateExecutionProgressCommand.java` 생성 (Record)
- [ ] 필드: executionId, taskCompleted (Boolean)
- [ ] `UpdateExecutionProgressUseCaseImpl.java` 생성
- [ ] CrawlingScheduleExecution 조회
- [ ] taskCompleted ? completeTask() : failTask() 호출
- [ ] scheduleExecutionCommandPort.save() 호출
- [ ] `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 실행 진행률 업데이트 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 실행 진행률 업데이트 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 생성
- [ ] 커밋: `test: 실행 진행률 업데이트 테스트 정리 (Tidy)`

---

### 6️⃣0️⃣ CompleteScheduleExecutionCommand DTO 및 UseCase 구현 (Cycle 60)

#### 🔴 Red: 테스트 작성
- [ ] `CompleteScheduleExecutionCommandTest.java` 생성
- [ ] `CompleteScheduleExecutionUseCaseImplTest.java` 생성
- [ ] `shouldCompleteExecutionSuccessfully()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 스케줄 실행 완료 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CompleteScheduleExecutionCommand.java` 생성 (Record)
- [ ] 필드: executionId
- [ ] `CompleteScheduleExecutionUseCaseImpl.java` 생성
- [ ] CrawlingScheduleExecution 조회 → complete() 호출 → 저장
- [ ] `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 스케줄 실행 완료 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 스케줄 실행 완료 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 생성
- [ ] 커밋: `test: 스케줄 실행 완료 테스트 정리 (Tidy)`

---

### 6️⃣1️⃣ SchedulerOutboxCommandPort 인터페이스 정의 (Cycle 61)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerOutboxCommandPortTest.java` 생성
- [ ] Port 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerOutboxCommandPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerOutboxCommandPort.java` 인터페이스 생성
- [ ] `save(SchedulerOutbox outbox)` 메서드 정의
- [ ] `saveAll(List<SchedulerOutbox> outboxes)` 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SchedulerOutboxCommandPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SchedulerOutboxCommandPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 생성
- [ ] 커밋: `test: SchedulerOutboxCommandPort 테스트 정리 (Tidy)`

---

### 6️⃣2️⃣ SchedulerOutboxQueryPort 인터페이스 정의 (Cycle 62)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerOutboxQueryPortTest.java` 생성
- [ ] Port 메서드 시그니처 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerOutboxQueryPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerOutboxQueryPort.java` 인터페이스 생성
- [ ] `findById(UUID outboxId)` 메서드 정의
- [ ] `findByStatus(SchedulerOutboxStatus status, Pageable pageable)` 메서드 정의
- [ ] `findWaitingOutboxes(int limit)` 메서드 정의
- [ ] `countByStatus(SchedulerOutboxStatus status)` 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SchedulerOutboxQueryPort 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JavaDoc 추가
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SchedulerOutboxQueryPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mock Port Fixture 정리
- [ ] 커밋: `test: SchedulerOutboxQueryPort 테스트 정리 (Tidy)`

---

### 6️⃣3️⃣ ProcessSchedulerOutboxUseCaseImpl 구현 - Part 1 (상태 업데이트) (Cycle 63)

#### 🔴 Red: 테스트 작성
- [ ] `ProcessSchedulerOutboxUseCaseImplTest.java` 생성
- [ ] `shouldUpdateOutboxStatusToSending()` 작성
- [ ] Mock Port 준비
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SchedulerOutbox 상태 업데이트 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProcessSchedulerOutboxUseCaseImpl.java` 생성
- [ ] WAITING 상태 Outbox 조회 (최대 100개)
- [ ] SENDING 상태로 변경 → 저장
- [ ] `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SchedulerOutbox 상태 업데이트 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증 (첫 번째 트랜잭션)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SchedulerOutbox 상태 업데이트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용
- [ ] 커밋: `test: SchedulerOutbox 상태 업데이트 테스트 정리 (Tidy)`

---

### 6️⃣4️⃣ ProcessSchedulerOutboxUseCaseImpl 구현 - Part 2 (EventBridge 전송) (Cycle 64)

#### 🔴 Red: 테스트 작성
- [ ] `shouldSendToEventBridgeSuccessfully()` 작성
- [ ] Mock 동작 정의 (EventBridgePort)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: EventBridge 전송 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] EventBridge 호출 (트랜잭션 밖)
- [ ] EventType에 따라 분기
  - SCHEDULE_REGISTERED: eventBridgePort.createRule()
  - SCHEDULE_UPDATED: eventBridgePort.updateRule()
  - SCHEDULE_DEACTIVATED: eventBridgePort.deleteRule()
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: EventBridge 전송 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증 (트랜잭션 밖 호출)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: EventBridge 전송 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용
- [ ] 커밋: `test: EventBridge 전송 테스트 정리 (Tidy)`

---

### 6️⃣5️⃣ ProcessSchedulerOutboxUseCaseImpl 구현 - Part 3 (결과 처리 및 재시도) (Cycle 65)

#### 🔴 Red: 테스트 작성
- [ ] `shouldRetryWhenEventBridgeFails()` 작성
- [ ] `shouldMarkFailedAfterMaxRetries()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SchedulerOutbox 재시도 로직 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 결과에 따라 상태 변경
  - 성공: complete()
  - 실패 (retryCount < 5): fail(errorMessage), WAITING으로 복구
  - 실패 (retryCount >= 5): fail(errorMessage), FAILED 유지
- [ ] 두 번째 `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SchedulerOutbox 재시도 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 2단계 Transaction 경계 검증
- [ ] 재시도 로직 정확성 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SchedulerOutbox 재시도 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용
- [ ] 커밋: `test: SchedulerOutbox 재시도 테스트 정리 (Tidy)`

---

### 6️⃣6️⃣ GetSchedulerOutboxStatusQuery 및 UseCase 구현 (Cycle 66)

#### 🔴 Red: 테스트 작성
- [ ] `GetSchedulerOutboxStatusQueryTest.java` 생성
- [ ] `GetSchedulerOutboxStatusUseCaseImplTest.java` 생성
- [ ] `shouldGetOutboxStatusSuccessfully()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SchedulerOutbox 상태 조회 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetSchedulerOutboxStatusQuery.java` 생성 (Record)
- [ ] `SchedulerOutboxStatusResponse.java` 생성 (Record)
- [ ] 필드: waitingCount, sendingCount, completedCount, failedCount
- [ ] `GetSchedulerOutboxStatusUseCaseImpl.java` 생성
- [ ] 상태별 개수 조회 → Response 생성
- [ ] `@Transactional(readOnly = true)` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SchedulerOutbox 상태 조회 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ReadOnly Transaction 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SchedulerOutbox 상태 조회 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 생성
- [ ] 커밋: `test: SchedulerOutbox 상태 조회 테스트 정리 (Tidy)`

---

### 6️⃣7️⃣ RetryFailedOutboxCommand DTO 및 UseCase 구현 (Cycle 67)

#### 🔴 Red: 테스트 작성
- [ ] `RetryFailedOutboxCommandTest.java` 생성
- [ ] `RetryFailedOutboxUseCaseImplTest.java` 생성
- [ ] `shouldRetryFailedOutboxSuccessfully()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 실패 Outbox 재시도 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RetryFailedOutboxCommand.java` 생성 (Record)
- [ ] 필드: outboxId
- [ ] `RetryFailedOutboxUseCaseImpl.java` 생성
- [ ] SchedulerOutbox 조회 → canRetry() 검증 → retry() 호출 → 저장
- [ ] `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 실패 Outbox 재시도 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 실패 Outbox 재시도 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 생성
- [ ] 커밋: `test: 실패 Outbox 재시도 테스트 정리 (Tidy)`

---

### 6️⃣8️⃣ CleanupCompletedOutboxCommand DTO 및 UseCase 구현 (Cycle 68)

#### 🔴 Red: 테스트 작성
- [ ] `CleanupCompletedOutboxCommandTest.java` 생성
- [ ] `CleanupCompletedOutboxUseCaseImplTest.java` 생성
- [ ] `shouldCleanupCompletedOutboxSuccessfully()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 완료 Outbox 정리 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CleanupCompletedOutboxCommand.java` 생성 (Record)
- [ ] 필드: olderThanDays (기본값: 7일)
- [ ] `CleanupCompletedOutboxUseCaseImpl.java` 생성
- [ ] COMPLETED 상태 + 7일 이전 Outbox 조회 → 삭제
- [ ] `@Transactional` 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 완료 Outbox 정리 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증
- [ ] Batch Delete 최적화
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 완료 Outbox 정리 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 생성
- [ ] 커밋: `test: 완료 Outbox 정리 테스트 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 68개 TDD 사이클 모두 완료 (272개 체크박스 모두 ✅)
- [ ] 모든 테스트 통과
- [ ] ArchUnit 테스트 통과 (Command/Query 분리, Transaction 경계)
- [ ] Zero-Tolerance 규칙 준수
  - [ ] Command/Query 분리 (CQRS)
  - [ ] Transaction 경계 엄격 관리
  - [ ] Assembler 패턴 사용
  - [ ] Spring 프록시 제약사항 준수
  - [ ] EventBridge Port 추상화 완료
  - [ ] SchedulerOutbox 패턴 구현 완료
- [ ] TestFixture 모두 정리 (Object Mother 패턴)
- [ ] 코드 커버리지 > 80%

---

## 🔗 관련 문서

- Task: docs/prd/tasks/MUSTIT-002.md
- PRD: docs/prd/mustit-seller-crawler.md
- Application Layer 규칙: docs/coding_convention/03-application-layer/

---

## 📚 참고사항

### Transaction 경계 설계 패턴

**Pattern 1: 단일 트랜잭션**
```java
@Transactional
public SellerResponse registerSeller(RegisterSellerCommand command) {
    // 1. DB 작업만
    Seller seller = Seller.register(...);
    sellerCommandPort.save(seller);
    // 트랜잭션 커밋

    // 2. 외부 API 호출 (트랜잭션 밖)
    eventBridgePort.createRule(seller.getSellerId(), ...);

    return assembler.toResponse(seller);
}
```

**Pattern 2: 2단계 트랜잭션**
```java
public TaskProcessedResponse processTask(ProcessCrawlerTaskCommand command) {
    // 1. 트랜잭션: 상태 업데이트
    updateTaskStatus(command.getTaskId(), IN_PROGRESS);

    // 2. 외부 API 호출 (트랜잭션 밖)
    CrawlingResult result = mustitApiPort.crawl(...);

    // 3. 트랜잭션: 결과 저장
    saveTaskResult(command.getTaskId(), result);

    return ...;
}
```

### Pessimistic Lock 사용 예시

```java
// UserAgentPoolManager
@Transactional
public UserAgent assignUserAgent() {
    // SELECT FOR UPDATE (Pessimistic Lock)
    UserAgent userAgent = userAgentQueryPort.findFirstActiveForUpdate()
        .orElseThrow(() -> new NoAvailableUserAgentException());

    if (!userAgent.canMakeRequest()) {
        throw new RateLimitExceededException();
    }

    userAgent.incrementRequestCount();
    userAgentCommandPort.save(userAgent);

    return userAgent;
}
```

### Bulk Insert 최적화

```java
// TriggerCrawlingUseCase
List<CrawlerTask> tasks = IntStream.range(0, pageCount)
    .mapToObj(page -> CrawlerTask.create(
        sellerId,
        MINISHOP,
        buildMinishopUrl(sellerId, page)
    ))
    .toList();

// Bulk Insert (한 번에 저장)
crawlerTaskCommandPort.saveAll(tasks);
```
