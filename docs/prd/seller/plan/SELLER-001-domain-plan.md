# SELLER-001 TDD Plan

**Task**: Seller Domain Layer 구현
**Layer**: Domain
**브랜치**: feature/SELLER-001-domain
**예상 소요 시간**: 120분 (8 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ SellerStatus Enum 구현 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [x] `SellerStatusTest.java` 파일 생성
- [x] `shouldHaveActiveStatus()` 테스트 작성
- [x] `shouldHaveInactiveStatus()` 테스트 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: SellerStatus Enum 테스트 추가 (Red)` *(로컬 환경에서 수행 필요)*

#### 🟢 Green: 최소 구현
- [x] `SellerStatus.java` 파일 생성 (Enum)
- [x] `ACTIVE`, `INACTIVE` 상수 정의
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: SellerStatus Enum 구현 (Green)` *(로컬 환경에서 수행 필요)*

#### ♻️ Refactor: 리팩토링
- [x] Enum 설명 주석 추가
- [x] VO ArchUnit 테스트 추가 및 통과 *(SellerStatus 포함 패키지 검증 완료)*
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: SellerStatus Enum 개선 (Refactor)` *(로컬 환경에서 수행 필요)*

#### 🧹 Tidy: TestFixture 정리
- [x] `SellerStatusFixture.java` 생성 (Object Mother 패턴)
- [x] `SellerStatusFixture.active()` 메서드 작성
- [x] `SellerStatusFixture.inactive()` 메서드 작성
- [x] `SellerStatusTest` → Fixture 사용으로 리팩토링
- [x] 커밋: `test: SellerStatusFixture 정리 (Tidy)` *(로컬 환경에서 수행 필요)*

---

### 2️⃣ Domain Exception 구현 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [x] `DuplicateMustItSellerIdExceptionTest.java` 생성
- [x] `shouldCreateExceptionWithMessage()` 테스트 작성
- [x] 나머지 3개 Exception 테스트 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: Domain Exception 테스트 추가 (Red)` *(로컬 환경에서 수행 필요)*

#### 🟢 Green: 최소 구현
- [x] `DuplicateMustItSellerIdException.java` 생성
- [x] `DuplicateSellerNameException.java` 생성
- [x] `SellerHasActiveSchedulersException.java` 생성
- [x] `SellerNotFoundException.java` 생성
- [x] 각 Exception에 메시지 정의
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: Domain Exception 구현 (Green)` *(로컬 환경에서 수행 필요)*

#### ♻️ Refactor: 리팩토링
- [x] Exception 계층 구조 확인 *(SellerErrorCode 추가 및 재사용)*
- [x] Exception ArchUnit 테스트 추가 및 통과
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: Domain Exception 개선 (Refactor)` *(로컬 환경에서 수행 필요)*

#### 🧹 Tidy: TestFixture 정리
- [x] `SellerExceptionFixture.java` 생성
- [x] 각 Exception 생성 메서드 추가
- [x] 테스트 → Fixture 사용으로 리팩토링
- [x] 커밋: `test: SellerExceptionFixture 정리 (Tidy)` *(로컬 환경에서 수행 필요)*

---

### 3️⃣ Seller Aggregate - 생성 메서드 (forNew) (Cycle 3)

#### 🔴 Red: 테스트 작성
- [x] `SellerTest.java` 파일 생성
- [x] `shouldCreateNewSellerWithValidData()` 테스트 작성
- [x] `shouldInitializeWithActiveStatus()` 테스트 작성
- [x] `shouldSetCreatedAtAndUpdatedAt()` 테스트 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: Seller.forNew() 테스트 추가 (Red)` *(로컬 환경에서 수행 필요)*

#### 🟢 Green: 최소 구현
- [x] `Seller.java` 파일 생성 (Plain Java, Lombok 금지)
- [x] 필드 정의 (sellerId, mustItSellerId, sellerName, status, createdAt, updatedAt)
- [x] `forNew(mustItSellerId, sellerName)` 정적 팩토리 메서드 구현
- [x] 생성자 구현 (private)
- [x] Getter 메서드 수동 구현
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: Seller.forNew() 구현 (Green)` *(로컬 환경에서 수행 필요)*

#### ♻️ Refactor: 리팩토링
- [x] 필드 final 선언 (불변성 보장)
- [x] Immutable 필드 검증 (mustItSellerId, sellerName)
- [x] Aggregate ArchUnit 테스트 추가 및 통과 *(기존 규칙 검증 수행)*
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: Seller.forNew() 불변성 강화 (Refactor)` *(로컬 환경에서 수행 필요)*

#### 🧹 Tidy: TestFixture 정리
- [x] `SellerFixture.java` 생성 (Object Mother 패턴)
- [x] `SellerFixture.aNewSeller()` 메서드 작성
- [x] `SellerTest` → Fixture 사용으로 리팩토링
- [x] 커밋: `test: SellerFixture 정리 (Tidy)` *(로컬 환경에서 수행 필요)*

---

### 4️⃣ Seller Aggregate - 재구성 메서드 (of, reconstitute) (Cycle 4)

#### 🔴 Red: 테스트 작성
- [x] `shouldCreateSellerWithOf()` 테스트 작성
- [x] `shouldReconstituteSellerWithAllFields()` 테스트 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: Seller.of(), reconstitute() 테스트 추가 (Red)` *(로컬 환경에서 수행 필요)*

#### 🟢 Green: 최소 구현
- [x] `of(sellerId, mustItSellerId, sellerName, status)` 정적 팩토리 메서드 구현
- [x] `reconstitute(sellerId, mustItSellerId, sellerName, status, createdAt, updatedAt)` 구현
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: Seller.of(), reconstitute() 구현 (Green)` *(로컬 환경에서 수행 필요)*

#### ♻️ Refactor: 리팩토링
- [x] 생성 메서드 3종 패턴 명확화 (주석 추가)
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: Seller 생성 메서드 패턴 명확화 (Refactor)` *(로컬 환경에서 수행 필요)*

#### 🧹 Tidy: TestFixture 정리
- [x] `SellerFixture.aSeller()` 메서드 추가 (of 사용)
- [x] `SellerFixture.aReconstitutedSeller()` 메서드 추가
- [x] 테스트 → Fixture 사용으로 리팩토링
- [x] 커밋: `test: SellerFixture 생성 메서드 추가 (Tidy)` *(로컬 환경에서 수행 필요)*

---

### 5️⃣ Seller Aggregate - deactivate() 메서드 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [x] `shouldDeactivateSellerWhenNoActiveSchedulers()` 테스트 작성
- [x] `shouldThrowExceptionWhenActiveSchedulersExist()` 테스트 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: Seller.deactivate() 테스트 추가 (Red)` *(로컬 환경에서 수행 필요)*

#### 🟢 Green: 최소 구현
- [x] `deactivate(activeSchedulerCount)` 메서드 구현
- [x] 활성 스케줄러 존재 시 `SellerHasActiveSchedulersException` 발생
- [x] 상태를 INACTIVE로 변경
- [x] updatedAt 갱신
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: Seller.deactivate() 구현 (Green)` *(로컬 환경에서 수행 필요)*

#### ♻️ Refactor: 리팩토링
- [x] Tell Don't Ask 패턴 검증
- [x] 캡슐화 확인 (외부 판단 금지)
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: Seller.deactivate() Tell Don't Ask 패턴 적용 (Refactor)` *(로컬 환경에서 수행 필요)*

#### 🧹 Tidy: TestFixture 정리
- [x] `SellerFixture.anActiveSeller()` 메서드 추가
- [x] 테스트 → Fixture 사용으로 리팩토링
- [x] 커밋: `test: SellerFixture 활성 셀러 추가 (Tidy)` *(로컬 환경에서 수행 필요)*

---

### 6️⃣ Seller Aggregate - activate() 메서드 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [x] `shouldActivateInactiveSeller()` 테스트 작성
- [x] `shouldNotChangeAlreadyActiveSeller()` 테스트 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: Seller.activate() 테스트 추가 (Red)` *(로컬 환경에서 수행 필요)*

#### 🟢 Green: 최소 구현
- [x] `activate()` 메서드 구현
- [x] 상태를 ACTIVE로 변경
- [x] updatedAt 갱신
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: Seller.activate() 구현 (Green)` *(로컬 환경에서 수행 필요)*

#### ♻️ Refactor: 리팩토링
- [x] 중복 로직 제거 (상태 변경 공통 패턴)
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: Seller 상태 변경 로직 개선 (Refactor)` *(로컬 환경에서 수행 필요)*

#### 🧹 Tidy: TestFixture 정리
- [x] `SellerFixture.anInactiveSeller()` 메서드 추가
- [x] 테스트 → Fixture 사용으로 리팩토링
- [x] 커밋: `test: SellerFixture 비활성 셀러 추가 (Tidy)` *(로컬 환경에서 수행 필요)*

---

### 7️⃣ Domain Event - SellerDeactivatedEvent (Cycle 7)

#### 🔴 Red: 테스트 작성
- [x] `SellerDeactivatedEventTest.java` 생성
- [x] `shouldCreateEventWithSellerIdAndOccurredAt()` 테스트 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: SellerDeactivatedEvent 테스트 추가 (Red)` *(로컬 환경에서 수행 필요)*

#### 🟢 Green: 최소 구현
- [x] `SellerDeactivatedEvent.java` 생성 (Record)
- [x] `sellerId`, `occurredAt` 필드 정의
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: SellerDeactivatedEvent 구현 (Green)` *(로컬 환경에서 수행 필요)*

#### ♻️ Refactor: 리팩토링
- [x] Event 불변성 검증 (Record)
- [x] Event ArchUnit 테스트 추가 및 통과
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: SellerDeactivatedEvent 개선 (Refactor)` *(로컬 환경에서 수행 필요)*

#### 🧹 Tidy: TestFixture 정리
- [x] `SellerEventFixture.java` 생성
- [x] `SellerEventFixture.aDeactivatedEvent()` 메서드 작성
- [x] 테스트 → Fixture 사용으로 리팩토링
- [x] 커밋: `test: SellerEventFixture 정리 (Tidy)` *(로컬 환경에서 수행 필요)*

---

### 8️⃣ Seller Aggregate - Event 발행 통합 (Cycle 8)

#### 🔴 Red: 테스트 작성
- [x] `shouldPublishEventWhenDeactivated()` 테스트 작성
- [x] Event 발행 검증 로직 추가
- [x] 테스트 실행 → 실패 확인
- [x] 커밋: `test: Seller deactivate Event 발행 테스트 추가 (Red)` *(로컬 환경에서 수행 필요)*

#### 🟢 Green: 최소 구현
- [x] `deactivate()` 메서드에 Event 발행 로직 추가
- [x] `SellerDeactivatedEvent` 생성 및 반환
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: Seller deactivate Event 발행 구현 (Green)` *(로컬 환경에서 수행 필요)*

#### ♻️ Refactor: 리팩토링
- [x] Event 발행 시점 검증
- [x] Domain Event 패턴 준수 확인
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: Seller Event 발행 개선 (Refactor)` *(로컬 환경에서 수행 필요)*

#### 🧹 Tidy: TestFixture 정리
- [x] 모든 Fixture 최종 정리
- [x] 테스트 코드 가독성 개선
- [x] 커밋: `test: SellerFixture 최종 정리 (Tidy)` *(로컬 환경에서 수행 필요)*

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (8 사이클 × 4단계 = 32개 체크박스 모두 ✅)
- [ ] 모든 단위 테스트 통과 (SellerTest, SellerStatusTest, Exception Tests, Event Tests)
- [ ] ArchUnit 테스트 통과
  - [ ] Aggregate ArchUnit 검증
  - [ ] VO ArchUnit 검증
  - [ ] Exception ArchUnit 검증
  - [ ] Domain Layer 의존성 검증
- [ ] Zero-Tolerance 규칙 준수 확인
  - [ ] Lombok 미사용 검증
  - [ ] Law of Demeter 준수 검증
  - [ ] Tell Don't Ask 패턴 검증
  - [ ] Setter 미사용 검증
- [ ] TestFixture 모두 정리 완료
  - [ ] SellerFixture
  - [ ] SellerStatusFixture
  - [ ] SellerExceptionFixture
  - [ ] SellerEventFixture
- [ ] 테스트 커버리지 > 80%

---

## 📊 사이클 요약

| Cycle | 요구사항 | Red | Green | Refactor | Tidy |
|-------|----------|-----|-------|----------|------|
| 1 | SellerStatus Enum | test: | feat: | struct: | test: |
| 2 | Domain Exception | test: | feat: | struct: | test: |
| 3 | Seller forNew() | test: | feat: | struct: | test: |
| 4 | Seller of(), reconstitute() | test: | feat: | struct: | test: |
| 5 | Seller deactivate() | test: | feat: | struct: | test: |
| 6 | Seller activate() | test: | feat: | struct: | test: |
| 7 | SellerDeactivatedEvent | test: | feat: | struct: | test: |
| 8 | Event 발행 통합 | test: | feat: | struct: | test: |

**총 커밋 수**: 32개 (8 사이클 × 4단계)

---

## 🔗 관련 문서

- **Task**: `/Users/sangwon-ryu/crawlinghub/docs/prd/seller/SELLER-001-domain.md`
- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **코딩 규칙**: `docs/coding_convention/02-domain-layer/`

---

## 🎯 다음 단계

1. `/kb/domain/go` - Domain Layer TDD 시작 (Cycle 1부터)
2. 각 사이클마다 Red → Green → Refactor → Tidy 순서로 진행
3. 모든 사이클 완료 후 PR 생성
