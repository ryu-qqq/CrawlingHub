# SELLER-002 TDD Plan

**Task**: Seller Application Layer 구현
**Layer**: Application
**브랜치**: feature/SELLER-002-application
**예상 소요 시간**: 180분 (12 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ Port 인터페이스 정의 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] `SellerCommandPortTest.java` 생성
- [ ] `SellerQueryPortTest.java` 생성
- [ ] Port 메서드 시그니처 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Seller Port 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerCommandPort.java` 인터페이스 생성
  - `Seller save(Seller seller)`
  - `void delete(Long sellerId)`
- [ ] `SellerQueryPort.java` 인터페이스 생성
  - `Optional<Seller> findById(Long sellerId)`
  - `Optional<Seller> findByMustItSellerId(String mustItSellerId)`
  - `Optional<Seller> findBySellerName(String sellerName)`
  - `boolean existsByMustItSellerId(String mustItSellerId)`
  - `boolean existsBySellerName(String sellerName)`
  - `Page<Seller> findAllByStatus(SellerStatus status, Pageable pageable)`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Seller Port 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Port 인터페이스 주석 추가 (역할 명확화)
- [ ] Port ArchUnit 테스트 추가 및 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: Seller Port 인터페이스 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Port Mock Fixture 정리 (필요 시)
- [ ] 커밋: `test: Seller Port 테스트 정리 (Tidy)`

---

### 2️⃣ Command/Query DTO 구현 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `RegisterSellerCommandTest.java` 생성
- [ ] `ChangeSellerStatusCommandTest.java` 생성
- [ ] `GetSellerQueryTest.java` 생성
- [ ] `ListSellersQueryTest.java` 생성
- [ ] Record 불변성 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Seller Command/Query DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RegisterSellerCommand.java` 생성 (Record)
- [ ] `ChangeSellerStatusCommand.java` 생성 (Record)
- [ ] `GetSellerQuery.java` 생성 (Record)
- [ ] `ListSellersQuery.java` 생성 (Record)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Seller Command/Query DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] DTO Record ArchUnit 테스트 추가 및 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: Seller Command/Query DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SellerCommandFixture.java` 생성
- [ ] `SellerQueryFixture.java` 생성
- [ ] 테스트 → Fixture 사용으로 리팩토링
- [ ] 커밋: `test: Seller Command/Query Fixture 정리 (Tidy)`

---

### 3️⃣ Response DTO 구현 (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `SellerResponseTest.java` 생성
- [ ] `SellerDetailResponseTest.java` 생성
- [ ] `SellerSummaryResponseTest.java` 생성
- [ ] Record 불변성 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Seller Response DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerResponse.java` 생성 (Record)
- [ ] `SellerDetailResponse.java` 생성 (Record)
- [ ] `SellerSummaryResponse.java` 생성 (Record)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Seller Response DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Response DTO ArchUnit 테스트 추가 및 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: Seller Response DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SellerResponseFixture.java` 생성
- [ ] 테스트 → Fixture 사용으로 리팩토링
- [ ] 커밋: `test: Seller Response Fixture 정리 (Tidy)`

---

### 4️⃣ SellerAssembler 구현 (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `SellerAssemblerTest.java` 생성
- [ ] `shouldAssembleSellerResponse()` 테스트 작성
- [ ] `shouldAssembleSellerDetailResponse()` 테스트 작성
- [ ] `shouldAssembleSellerSummaryResponse()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerAssembler 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerAssembler.java` 생성
- [ ] Domain Seller → SellerResponse 변환 메서드
- [ ] Domain Seller → SellerDetailResponse 변환 메서드
- [ ] Domain Seller → SellerSummaryResponse 변환 메서드
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SellerAssembler 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Assembler ArchUnit 테스트 추가 및 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: SellerAssembler 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Assembler 테스트 Fixture 사용 정리
- [ ] 커밋: `test: SellerAssembler 테스트 정리 (Tidy)`

---

### 5️⃣ RegisterSellerUseCase 구현 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `RegisterSellerServiceTest.java` 생성
- [ ] Mock Port 준비 (SellerCommandPort, SellerQueryPort)
- [ ] `shouldRegisterSellerSuccessfully()` 테스트 작성
- [ ] `shouldThrowExceptionWhenDuplicateMustItSellerId()` 테스트 작성
- [ ] `shouldThrowExceptionWhenDuplicateSellerName()` 테스트 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: RegisterSellerService 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RegisterSellerUseCase.java` 인터페이스 생성 (Port In)
- [ ] `RegisterSellerService.java` 생성 (@Service)
- [ ] `@Transactional` 추가
- [ ] 중복 검증 로직 (mustItSellerId, sellerName)
- [ ] Seller Aggregate 생성 (Domain.forNew())
- [ ] Seller 저장 (SellerCommandPort)
- [ ] SellerResponse 반환 (Assembler)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: RegisterSellerService 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증 (외부 API 호출 없는지)
- [ ] Spring Proxy 제약사항 준수 확인 (public 메서드)
- [ ] UseCase ArchUnit 테스트 추가 및 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: RegisterSellerService 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] RegisterSellerCommand Fixture 사용 정리
- [ ] Mock Port Fixture 정리
- [ ] 커밋: `test: RegisterSellerService 테스트 정리 (Tidy)`

---

### 6️⃣ ChangeSellerStatusUseCase 구현 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `ChangeSellerStatusServiceTest.java` 생성
- [ ] Mock Port 준비 (SellerCommandPort, SellerQueryPort, SchedulerQueryPort)
- [ ] `shouldDeactivateSellerWhenNoActiveSchedulers()` 테스트 작성
- [ ] `shouldThrowExceptionWhenActiveSchedulersExist()` 테스트 작성
- [ ] `shouldActivateInactiveSeller()` 테스트 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ChangeSellerStatusService 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ChangeSellerStatusUseCase.java` 인터페이스 생성 (Port In)
- [ ] `ChangeSellerStatusService.java` 생성 (@Service)
- [ ] `@Transactional` 추가
- [ ] Seller 조회 (SellerQueryPort)
- [ ] ACTIVE 스케줄 존재 여부 확인 (SchedulerQueryPort)
- [ ] `Seller.deactivate(activeSchedulerCount)` 호출
- [ ] `Seller.activate()` 호출
- [ ] Seller 저장 (SellerCommandPort)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ChangeSellerStatusService 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증
- [ ] Tell Don't Ask 패턴 준수 확인
- [ ] UseCase ArchUnit 테스트 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: ChangeSellerStatusService 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ChangeSellerStatusCommand Fixture 사용 정리
- [ ] 커밋: `test: ChangeSellerStatusService 테스트 정리 (Tidy)`

---

### 7️⃣ GetSellerUseCase 구현 (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `GetSellerServiceTest.java` 생성
- [ ] Mock Port 준비 (SellerQueryPort, SchedulerQueryPort)
- [ ] `shouldGetSellerDetailSuccessfully()` 테스트 작성
- [ ] `shouldThrowExceptionWhenSellerNotFound()` 테스트 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GetSellerService 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetSellerUseCase.java` 인터페이스 생성 (Port In)
- [ ] `GetSellerService.java` 생성 (@Service)
- [ ] `@Transactional(readOnly = true)` 추가
- [ ] Seller 조회 (SellerQueryPort)
- [ ] 스케줄러 카운트 조회 (SchedulerQueryPort)
- [ ] SellerDetailResponse 조립 (Assembler)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: GetSellerService 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ReadOnly Transaction 검증
- [ ] Query UseCase ArchUnit 테스트 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: GetSellerService 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] GetSellerQuery Fixture 사용 정리
- [ ] 커밋: `test: GetSellerService 테스트 정리 (Tidy)`

---

### 8️⃣ ListSellersUseCase 구현 (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `ListSellersServiceTest.java` 생성
- [ ] Mock Port 준비 (SellerQueryPort, SchedulerQueryPort)
- [ ] `shouldListAllSellers()` 테스트 작성
- [ ] `shouldFilterByStatus()` 테스트 작성
- [ ] `shouldSupportPagination()` 테스트 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ListSellersService 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ListSellersUseCase.java` 인터페이스 생성 (Port In)
- [ ] `ListSellersService.java` 생성 (@Service)
- [ ] `@Transactional(readOnly = true)` 추가
- [ ] Seller 목록 조회 (SellerQueryPort)
- [ ] 스케줄러 카운트 조회 (각 Seller마다)
- [ ] PageResponse<SellerSummaryResponse> 조립
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ListSellersService 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] N+1 문제 방지 확인 (Port에서 최적화)
- [ ] Pagination 로직 검증
- [ ] Query UseCase ArchUnit 테스트 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: ListSellersService 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ListSellersQuery Fixture 사용 정리
- [ ] 커밋: `test: ListSellersService 테스트 정리 (Tidy)`

---

### 9️⃣ SellerDeactivatedEventHandler 구현 (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `SellerDeactivatedEventHandlerTest.java` 생성
- [ ] Mock Port 준비 (SchedulerQueryPort)
- [ ] `shouldHandleEventAndDeactivateSchedulers()` 테스트 작성
- [ ] `shouldProcessAsynchronously()` 테스트 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SellerDeactivatedEventHandler 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerDeactivatedEventHandler.java` 생성
- [ ] `@EventListener` 추가
- [ ] `@TransactionalEventListener(phase = AFTER_COMMIT)` 적용
- [ ] ACTIVE 스케줄 조회 (SchedulerQueryPort)
- [ ] 각 스케줄 비활성화 처리 (EventBridge 바운더리 컨텍스트 호출)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SellerDeactivatedEventHandler 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 비동기 처리 검증 (TransactionSynchronization)
- [ ] Event Handler ArchUnit 테스트 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: SellerDeactivatedEventHandler 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Event Fixture 사용 정리
- [ ] 커밋: `test: SellerDeactivatedEventHandler 테스트 정리 (Tidy)`

---

### 🔟 SchedulerQueryPort 인터페이스 정의 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerQueryPortTest.java` 생성
- [ ] Port 메서드 시그니처 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerQueryPort 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerQueryPort.java` 인터페이스 생성
  - `int countActiveSchedulersBySellerId(Long sellerId)`
  - `int countTotalSchedulersBySellerId(Long sellerId)`
  - `List<Scheduler> findActiveSchedulersBySellerId(Long sellerId)`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerQueryPort 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Port ArchUnit 테스트 추가 및 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: SchedulerQueryPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Port Mock Fixture 정리
- [ ] 커밋: `test: SchedulerQueryPort 테스트 정리 (Tidy)`

---

### 1️⃣1️⃣ CQRS 패키지 구조 검증 (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `ApplicationLayerArchUnitTest.java` 생성
- [ ] Command/Query 패키지 분리 검증 테스트 작성
- [ ] service/command/, service/query/ 분리 검증
- [ ] Port 의존성 검증 테스트
- [ ] 테스트 실행 → 실패 확인 (구조 검증)
- [ ] 커밋: `test: Application Layer ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 패키지 구조 재정렬 (필요 시)
  - `service/command/RegisterSellerService.java`
  - `service/command/ChangeSellerStatusService.java`
  - `service/query/GetSellerService.java`
  - `service/query/ListSellersService.java`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Application Layer CQRS 패키지 구조 적용 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Application Layer 의존성 검증 (Domain만 의존)
- [ ] Adapter Layer 의존 금지 확인
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: Application Layer 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 모든 Fixture 최종 정리
- [ ] 커밋: `test: Application Layer Fixture 최종 정리 (Tidy)`

---

### 1️⃣2️⃣ 통합 테스트 및 최종 검증 (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `SellerUseCaseIntegrationTest.java` 생성
- [ ] 전체 UseCase 흐름 통합 테스트 작성
- [ ] RegisterSeller → GetSeller → ChangeStatus → ListSellers 시나리오
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Seller UseCase 통합 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 통합 테스트 통과를 위한 누락 로직 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Seller UseCase 통합 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 전체 Application Layer 코드 리뷰
- [ ] Zero-Tolerance 규칙 최종 검증
  - Lombok 미사용
  - Command/Query 분리
  - Transaction 경계
  - Spring Proxy 제약사항 준수
- [ ] 테스트 커버리지 > 80% 확인
- [ ] 커밋: `struct: Application Layer 최종 검증 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 모든 TestFixture 최종 정리
- [ ] 테스트 가독성 개선
- [ ] 커밋: `test: Application Layer 최종 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (12 사이클 × 4단계 = 48개 체크박스 모두 ✅)
- [ ] 모든 단위 테스트 통과
  - Port 테스트
  - DTO 테스트
  - Assembler 테스트
  - UseCase 테스트 (Mock Port)
  - Event Handler 테스트
- [ ] 통합 테스트 통과
- [ ] ArchUnit 테스트 통과
  - Application Layer 의존성 검증
  - CQRS 패키지 분리 검증
  - Port 인터페이스 의존 검증
- [ ] Zero-Tolerance 규칙 준수 확인
  - Command/Query 분리 (CQRS)
  - Transaction 경계 엄격 관리
  - Spring Proxy 제약사항 준수
  - Port 의존성 역전
  - Assembler 패턴 사용
- [ ] TestFixture 모두 정리 완료
  - SellerCommandFixture
  - SellerQueryFixture
  - SellerResponseFixture
  - Mock Port Fixtures
- [ ] 테스트 커버리지 > 80%

---

## 📊 사이클 요약

| Cycle | 요구사항 | Red | Green | Refactor | Tidy |
|-------|----------|-----|-------|----------|------|
| 1 | Port 인터페이스 정의 | test: | feat: | struct: | test: |
| 2 | Command/Query DTO | test: | feat: | struct: | test: |
| 3 | Response DTO | test: | feat: | struct: | test: |
| 4 | SellerAssembler | test: | feat: | struct: | test: |
| 5 | RegisterSellerUseCase | test: | feat: | struct: | test: |
| 6 | ChangeSellerStatusUseCase | test: | feat: | struct: | test: |
| 7 | GetSellerUseCase | test: | feat: | struct: | test: |
| 8 | ListSellersUseCase | test: | feat: | struct: | test: |
| 9 | SellerDeactivatedEventHandler | test: | feat: | struct: | test: |
| 10 | SchedulerQueryPort | test: | feat: | struct: | test: |
| 11 | CQRS 패키지 구조 검증 | test: | feat: | struct: | test: |
| 12 | 통합 테스트 및 최종 검증 | test: | feat: | struct: | test: |

**총 커밋 수**: 48개 (12 사이클 × 4단계)

---

## 🔗 관련 문서

- **Task**: `/Users/sangwon-ryu/crawlinghub/docs/prd/seller/SELLER-002-application.md`
- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **코딩 규칙**: `docs/coding_convention/03-application-layer/`
- **선행 Task**: SELLER-001 (Domain Layer)

---

## 🎯 다음 단계

1. `/kb/application/go` - Application Layer TDD 시작 (Cycle 1부터)
2. 각 사이클마다 Red → Green → Refactor → Tidy 순서로 진행
3. 모든 사이클 완료 후 PR 생성
