# SELLER-003 TDD Plan

**Task**: Seller Persistence Layer 구현
**Layer**: Persistence
**브랜치**: feature/SELLER-003-persistence
**예상 소요 시간**: 150분 (10 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ SellerJpaEntity 생성 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] `SellerJpaEntityTest.java` 파일 생성
- [ ] `shouldCreateEntityWithAllFields()` 테스트 작성
- [ ] `shouldInheritBaseAuditEntity()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerJpaEntity.java` 파일 생성
- [ ] BaseAuditEntity 상속
- [ ] 필드 정의 (id, mustItSellerId, sellerName, status)
- [ ] 생성자 구현 (Lombok 금지)
- [ ] Getter 메서드 수동 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SellerJpaEntity 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] `@Entity`, `@Table` 어노테이션 추가
- [ ] `@Column` Unique, Index 설정
- [ ] Entity ArchUnit 테스트 추가 및 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: SellerJpaEntity 매핑 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SellerJpaEntityFixture.java` 생성 (Object Mother 패턴)
- [ ] `SellerJpaEntityFixture.aSellerEntity()` 메서드 작성
- [ ] `SellerJpaEntityTest` → Fixture 사용으로 리팩토링
- [ ] 커밋: `test: SellerJpaEntityFixture 정리 (Tidy)`

---

### 2️⃣ SellerJpaRepository 구현 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `SellerJpaRepositoryTest.java` 파일 생성 (@DataJpaTest)
- [ ] `shouldSaveAndFindById()` 테스트 작성
- [ ] `shouldFindByMustItSellerId()` 테스트 작성
- [ ] `shouldFindBySellerName()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerJpaRepository 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerJpaRepository.java` 인터페이스 생성
- [ ] `JpaRepository<SellerJpaEntity, Long>` 상속
- [ ] 메서드 시그니처 정의
  - `Optional<SellerJpaEntity> findByMustItSellerId(String)`
  - `Optional<SellerJpaEntity> findBySellerName(String)`
  - `boolean existsByMustItSellerId(String)`
  - `boolean existsBySellerName(String)`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SellerJpaRepository 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Repository ArchUnit 테스트 추가 및 통과
- [ ] 메서드 네이밍 규칙 검증 (Spring Data JPA)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: SellerJpaRepository 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Repository 테스트 → Fixture 사용으로 리팩토링
- [ ] 커밋: `test: SellerJpaRepository 테스트 Fixture 적용 (Tidy)`

---

### 3️⃣ Unique Constraint 테스트 (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `shouldThrowExceptionWhenDuplicateMustItSellerId()` 테스트 작성
- [ ] `shouldThrowExceptionWhenDuplicateSellerName()` 테스트 작성
- [ ] 테스트 실행 → 실패 확인 (아직 Constraint 없음)
- [ ] 커밋: `test: Unique Constraint 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerJpaEntity`에 `@Table(uniqueConstraints = ...)` 추가
- [ ] `mustItSellerId` Unique Constraint 설정
- [ ] `sellerName` Unique Constraint 설정
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Seller Unique Constraint 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Index 이름 명시적 지정
- [ ] Constraint 이름 명시적 지정
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: Seller Constraint 이름 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Unique Constraint 테스트용 Fixture 추가
- [ ] `SellerJpaEntityFixture.aSellerEntityWithMustItSellerId(String)` 메서드 추가
- [ ] 커밋: `test: Unique Constraint Fixture 추가 (Tidy)`

---

### 4️⃣ SellerQueryDslRepository 구현 (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `SellerQueryDslRepositoryTest.java` 파일 생성
- [ ] `shouldFindAllByStatusWithPagination()` 테스트 작성
- [ ] `shouldReturnEmptyWhenNoMatchingStatus()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerQueryDslRepository 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerQueryDslRepository.java` 인터페이스 생성
- [ ] `SellerQueryDslRepositoryImpl.java` 구현 클래스 생성
- [ ] `findAllByStatus(SellerStatus, Pageable)` 메서드 구현
- [ ] QueryDSL Q-Class 사용
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SellerQueryDslRepository 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] QueryDSL DTO Projection 최적화
- [ ] N+1 문제 방지 검증
- [ ] QueryDSL ArchUnit 테스트 추가 및 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: SellerQueryDslRepository 최적화 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] QueryDSL 테스트용 Fixture 추가
- [ ] 페이징 테스트 데이터 Fixture 작성
- [ ] 커밋: `test: SellerQueryDslRepository Fixture 정리 (Tidy)`

---

### 5️⃣ SellerMapper 구현 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `SellerMapperTest.java` 파일 생성
- [ ] `shouldMapDomainToJpaEntity()` 테스트 작성
- [ ] `shouldMapJpaEntityToDomain()` 테스트 작성
- [ ] `shouldMapDomainListToJpaEntityList()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerMapper.java` 클래스 생성 (@Component)
- [ ] `toJpaEntity(Seller)` 메서드 구현
  - ⚠️ **Setter 절대 금지**: 생성자 또는 정적 팩토리 메서드 사용
  - ✅ `new SellerJpaEntity(...)` 또는 `SellerJpaEntity.of(...)`
- [ ] `toDomain(SellerJpaEntity)` 메서드 구현
  - Domain의 `reconstitute(...)` 메서드 사용
- [ ] `toDomainList(List<SellerJpaEntity>)` 메서드 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SellerMapper 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Enum 변환 로직 분리 (SellerStatus ↔ String)
- [ ] Null 안전성 검증
- [ ] Mapper ArchUnit 테스트 추가 및 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: SellerMapper 변환 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SellerMapperFixture.java` 생성
- [ ] Domain/Entity 변환 테스트용 Fixture 추가
- [ ] 커밋: `test: SellerMapperFixture 정리 (Tidy)`

---

### 6️⃣ SellerCommandAdapter 구현 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `SellerCommandAdapterTest.java` 파일 생성
- [ ] `shouldPersistNewSeller()` 테스트 작성
- [ ] `shouldPersistExistingSeller()` 테스트 작성 (업데이트)
- [ ] `shouldDeleteSeller()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerCommandAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerCommandAdapter.java` 클래스 생성 (@Component)
- [ ] `SellerCommandPort` 인터페이스 구현
- [ ] `persist(Seller)` 메서드 구현
  - Domain → Entity 변환 (Mapper)
  - JpaRepository.save()
  - Entity → Domain 변환 (Mapper)
  - ⚠️ **Persist 포트 통일 패턴**: 생성/수정 모두 persist()로 처리
- [ ] `delete(Long)` 메서드 구현 (물리 삭제)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SellerCommandAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증 (Adapter 레벨은 @Transactional 없음)
- [ ] Command Adapter ArchUnit 테스트 추가 및 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: SellerCommandAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Command Adapter 테스트용 Fixture 추가
- [ ] Mock Repository 설정 Fixture 작성
- [ ] 커밋: `test: SellerCommandAdapter Fixture 정리 (Tidy)`

---

### 7️⃣ SellerQueryAdapter 구현 (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `SellerQueryAdapterTest.java` 파일 생성
- [ ] `shouldFindById()` 테스트 작성
- [ ] `shouldFindByMustItSellerId()` 테스트 작성
- [ ] `shouldFindBySellerName()` 테스트 작성
- [ ] `shouldReturnEmptyWhenNotFound()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerQueryAdapter 조회 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerQueryAdapter.java` 클래스 생성 (@Component)
- [ ] `SellerQueryPort` 인터페이스 구현
- [ ] `findById(Long)` 메서드 구현
- [ ] `findByMustItSellerId(String)` 메서드 구현
- [ ] `findBySellerName(String)` 메서드 구현
- [ ] Entity → Domain 변환 (Mapper)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SellerQueryAdapter 조회 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Query Adapter ArchUnit 테스트 추가 및 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: SellerQueryAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Query Adapter 테스트용 Fixture 추가
- [ ] 커밋: `test: SellerQueryAdapter Fixture 정리 (Tidy)`

---

### 8️⃣ SellerQueryAdapter - exists 메서드 (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `shouldReturnTrueWhenMustItSellerIdExists()` 테스트 작성
- [ ] `shouldReturnFalseWhenMustItSellerIdNotExists()` 테스트 작성
- [ ] `shouldReturnTrueWhenSellerNameExists()` 테스트 작성
- [ ] `shouldReturnFalseWhenSellerNameNotExists()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerQueryAdapter exists 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `existsByMustItSellerId(String)` 메서드 구현
- [ ] `existsBySellerName(String)` 메서드 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SellerQueryAdapter exists 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] exists 메서드 최적화 (COUNT 쿼리 vs EXISTS 쿼리)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: SellerQueryAdapter exists 최적화 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] exists 테스트용 Fixture 추가
- [ ] 커밋: `test: SellerQueryAdapter exists Fixture 추가 (Tidy)`

---

### 9️⃣ SellerQueryAdapter - findAllByStatus (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFindAllByStatusWithPagination()` 테스트 작성
- [ ] `shouldReturnEmptyPageWhenNoMatchingStatus()` 테스트 작성
- [ ] `shouldRespectPageableSettings()` 테스트 작성 (size, sort)
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerQueryAdapter findAllByStatus 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `findAllByStatus(SellerStatus, Pageable)` 메서드 구현
- [ ] SellerQueryDslRepository 호출
- [ ] Entity List → Domain List 변환 (Mapper)
- [ ] Page 객체 변환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SellerQueryAdapter findAllByStatus 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Page 변환 로직 최적화
- [ ] N+1 문제 없음 검증 (QueryDSL DTO Projection)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: SellerQueryAdapter 페이징 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 페이징 테스트용 Fixture 추가
- [ ] 다양한 Pageable 설정 Fixture 작성
- [ ] 커밋: `test: SellerQueryAdapter 페이징 Fixture 추가 (Tidy)`

---

### 🔟 Integration Test - 종합 테스트 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `SellerPersistenceIntegrationTest.java` 파일 생성 (@DataJpaTest)
- [ ] `shouldPerformFullCRUDCycle()` 테스트 작성
- [ ] `shouldEnforceUniqueConstraints()` 테스트 작성
- [ ] `shouldHandleQueryDslPagination()` 테스트 작성
- [ ] TestContainers MySQL 설정
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Seller Persistence Integration 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] TestContainers 설정 완료
- [ ] 모든 컴포넌트 통합 (Entity, Repository, Adapter, Mapper)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Seller Persistence Integration 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Flyway Migration 스크립트 작성
  - `V001__Create_sellers_table.sql`
- [ ] Integration Test에서 Flyway 사용하도록 변경
- [ ] Persistence Layer ArchUnit 테스트 추가 및 통과
  - Long FK 전략 검증
  - Lombok 미사용 검증
  - BaseAuditEntity 상속 검증
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: Seller Persistence 통합 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 모든 Fixture 최종 정리
- [ ] Integration Test 가독성 개선
- [ ] 테스트 코드 중복 제거
- [ ] 커밋: `test: Seller Persistence Fixture 최종 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (10 사이클 × 4단계 = 40개 체크박스 모두 ✅)
- [ ] 모든 단위 테스트 통과
  - SellerJpaEntityTest
  - SellerJpaRepositoryTest
  - SellerQueryDslRepositoryTest
  - SellerMapperTest
  - SellerCommandAdapterTest
  - SellerQueryAdapterTest
- [ ] Integration Test 통과
  - SellerPersistenceIntegrationTest (TestContainers)
- [ ] ArchUnit 테스트 통과
  - Entity ArchUnit 검증
  - Repository ArchUnit 검증
  - Mapper ArchUnit 검증
  - Adapter ArchUnit 검증
  - Persistence Layer 의존성 검증
- [ ] Zero-Tolerance 규칙 준수 확인
  - Long FK 전략 준수 (JPA 관계 어노테이션 없음)
  - Lombok 미사용 검증
  - QueryDSL DTO Projection 사용
  - Mapper Setter 미사용 검증
  - BaseAuditEntity 상속 검증
- [ ] TestFixture 모두 정리 완료
  - SellerJpaEntityFixture
  - SellerMapperFixture
  - Command/Query Adapter Fixture
- [ ] Flyway Migration 스크립트 작성
  - `V001__Create_sellers_table.sql`
  - Unique Constraint 포함
  - Index 포함
- [ ] 테스트 커버리지 > 80%

---

## 📊 사이클 요약

| Cycle | 요구사항 | Red | Green | Refactor | Tidy |
|-------|----------|-----|-------|----------|------|
| 1 | SellerJpaEntity | test: | feat: | struct: | test: |
| 2 | SellerJpaRepository | test: | feat: | struct: | test: |
| 3 | Unique Constraint | test: | feat: | struct: | test: |
| 4 | SellerQueryDslRepository | test: | feat: | struct: | test: |
| 5 | SellerMapper | test: | feat: | struct: | test: |
| 6 | SellerCommandAdapter | test: | feat: | struct: | test: |
| 7 | SellerQueryAdapter (조회) | test: | feat: | struct: | test: |
| 8 | SellerQueryAdapter (exists) | test: | feat: | struct: | test: |
| 9 | SellerQueryAdapter (페이징) | test: | feat: | struct: | test: |
| 10 | Integration Test | test: | feat: | struct: | test: |

**총 커밋 수**: 40개 (10 사이클 × 4단계)

---

## 🔗 관련 문서

- **Task**: `/Users/sangwon-ryu/crawlinghub/docs/prd/seller/SELLER-003-persistence.md`
- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **코딩 규칙**: `docs/coding_convention/04-persistence-layer/mysql/`

---

## 🎯 다음 단계

1. `/kb/persistence/go` - Persistence Layer TDD 시작 (Cycle 1부터)
2. 각 사이클마다 Red → Green → Refactor → Tidy 순서로 진행
3. 모든 사이클 완료 후 PR 생성
