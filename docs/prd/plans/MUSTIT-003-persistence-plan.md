# MUSTIT-003 TDD Plan

**Task**: Persistence Layer 구현
**Layer**: Persistence Layer
**브랜치**: feature/MUSTIT-003-persistence
**예상 소요 시간**: 930분 (62 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ SellerJpaEntity 구현 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] `SellerJpaEntityTest.java` 생성
- [ ] `shouldCreateEntityWithValidData()` 작성
- [ ] `shouldMapToDomain()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerJpaEntity.java` 생성 (Plain Java, Lombok 금지)
- [ ] 필드: id, sellerId, name, crawlingIntervalDays, status, totalProductCount, createdAt, updatedAt
- [ ] `@Entity`, `@Table(name = "sellers")` 어노테이션
- [ ] `@Column` 제약 조건 (Unique, Not Null, Index)
- [ ] BaseAuditEntity 상속 (createdAt, updatedAt)
- [ ] Getter/Setter 직접 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SellerJpaEntity 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Long FK 전략 확인 (String sellerId, JPA 관계 어노테이션 없음)
- [ ] ArchUnit 테스트 추가 (JPA Entity 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SellerJpaEntity 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SellerJpaEntityFixture.java` 생성 (Object Mother 패턴)
- [ ] `aSellerJpaEntity()` 메서드 작성
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: SellerJpaEntityFixture 정리 (Tidy)`

---

### 2️⃣ CrawlerTaskJpaEntity 구현 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlerTaskJpaEntityTest.java` 생성
- [ ] `shouldCreateEntityWithValidData()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: CrawlerTaskJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlerTaskJpaEntity.java` 생성 (Plain Java)
- [ ] 필드: id, taskId, sellerId(String FK), taskType, requestUrl, status, retryCount, errorMessage, createdAt, publishedAt, startedAt, completedAt
- [ ] `@Table(name = "crawler_tasks")` 어노테이션
- [ ] `@Column` 제약 조건 (Unique taskId, Index)
- [ ] Getter/Setter 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlerTaskJpaEntity 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Long FK 전략 확인 (String sellerId)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlerTaskJpaEntity 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `CrawlerTaskJpaEntityFixture.java` 생성
- [ ] 커밋: `test: CrawlerTaskJpaEntityFixture 정리 (Tidy)`

---

### 3️⃣ UserAgentJpaEntity 구현 (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `UserAgentJpaEntityTest.java` 생성
- [ ] `shouldCreateEntityWithValidData()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: UserAgentJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UserAgentJpaEntity.java` 생성
- [ ] 필드: id, userAgentId, userAgentString, token, status, requestCount, lastRequestAt, tokenIssuedAt, createdAt
- [ ] `@Table(name = "user_agents")` 어노테이션
- [ ] `@Column` 제약 조건 (Unique userAgentId, Index status)
- [ ] Getter/Setter 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: UserAgentJpaEntity 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: UserAgentJpaEntity 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `UserAgentJpaEntityFixture.java` 생성
- [ ] 커밋: `test: UserAgentJpaEntityFixture 정리 (Tidy)`

---

### 4️⃣ ProductJpaEntity 구현 (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `ProductJpaEntityTest.java` 생성
- [ ] `shouldCreateEntityWithValidData()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProductJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProductJpaEntity.java` 생성
- [ ] 필드: id, productId, itemNo, sellerId(String FK), minishopDataHash, detailDataHash, optionDataHash, isComplete, createdAt, updatedAt
- [ ] `@Table(name = "products")` 어노테이션
- [ ] `@Column` 제약 조건 (Unique productId, itemNo, Index)
- [ ] Getter/Setter 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ProductJpaEntity 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Long FK 전략 확인 (String sellerId)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ProductJpaEntity 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ProductJpaEntityFixture.java` 생성
- [ ] 커밋: `test: ProductJpaEntityFixture 정리 (Tidy)`

---

### 5️⃣ ProductRawDataJpaEntity 구현 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `ProductRawDataJpaEntityTest.java` 생성
- [ ] `shouldStoreRawJsonData()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProductRawDataJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProductRawDataJpaEntity.java` 생성
- [ ] 필드: id, productId(String FK), dataType, rawJson(TEXT), createdAt
- [ ] `@Table(name = "product_raw_data")` 어노테이션
- [ ] `@Column(columnDefinition = "TEXT")` for rawJson
- [ ] Getter/Setter 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ProductRawDataJpaEntity 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Long FK 전략 확인 (String productId)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ProductRawDataJpaEntity 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ProductRawDataJpaEntityFixture.java` 생성
- [ ] 커밋: `test: ProductRawDataJpaEntityFixture 정리 (Tidy)`

---

### 6️⃣ ProductOutboxJpaEntity 구현 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `ProductOutboxJpaEntityTest.java` 생성
- [ ] `shouldCreateOutboxWithPayload()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProductOutboxJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProductOutboxJpaEntity.java` 생성
- [ ] 필드: id, outboxId, productId(String FK), eventType, payload(TEXT), status, retryCount, errorMessage, createdAt, sentAt
- [ ] `@Table(name = "product_outbox")` 어노테이션
- [ ] `@Column(columnDefinition = "TEXT")` for payload
- [ ] Getter/Setter 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ProductOutboxJpaEntity 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Long FK 전략 확인 (String productId)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ProductOutboxJpaEntity 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ProductOutboxJpaEntityFixture.java` 생성
- [ ] 커밋: `test: ProductOutboxJpaEntityFixture 정리 (Tidy)`

---

### 7️⃣ SellerMapper 구현 (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `SellerMapperTest.java` 생성
- [ ] `shouldMapEntityToDomain()` 작성
- [ ] `shouldMapDomainToEntity()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SellerMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerMapper.java` 생성
- [ ] `toDomain(SellerJpaEntity)` 메서드 구현
- [ ] `toEntity(Seller)` 메서드 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SellerMapper 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 변환 로직 검증
- [ ] ArchUnit 테스트 추가 (Mapper 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SellerMapper 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: SellerMapper 테스트 정리 (Tidy)`

---

### 8️⃣ CrawlerTaskMapper 구현 (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlerTaskMapperTest.java` 생성
- [ ] `shouldMapEntityToDomain()` 작성
- [ ] `shouldMapDomainToEntity()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: CrawlerTaskMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlerTaskMapper.java` 생성
- [ ] `toDomain(CrawlerTaskJpaEntity)` 구현
- [ ] `toEntity(CrawlerTask)` 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlerTaskMapper 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlerTaskMapper 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: CrawlerTaskMapper 테스트 정리 (Tidy)`

---

### 9️⃣ UserAgentMapper 구현 (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `UserAgentMapperTest.java` 생성
- [ ] `shouldMapEntityToDomain()` 작성
- [ ] `shouldMapDomainToEntity()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: UserAgentMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UserAgentMapper.java` 생성
- [ ] `toDomain(UserAgentJpaEntity)` 구현
- [ ] `toEntity(UserAgent)` 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: UserAgentMapper 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: UserAgentMapper 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: UserAgentMapper 테스트 정리 (Tidy)`

---

### 🔟 ProductMapper 구현 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `ProductMapperTest.java` 생성
- [ ] `shouldMapEntityToDomain()` 작성
- [ ] `shouldMapDomainToEntity()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ProductMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProductMapper.java` 생성
- [ ] `toDomain(ProductJpaEntity)` 구현
- [ ] `toEntity(Product)` 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ProductMapper 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ProductMapper 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: ProductMapper 테스트 정리 (Tidy)`

---

### 1️⃣1️⃣ ProductOutboxMapper 구현 (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `ProductOutboxMapperTest.java` 생성
- [ ] `shouldMapEntityToDomain()` 작성
- [ ] `shouldMapDomainToEntity()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ProductOutboxMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProductOutboxMapper.java` 생성
- [ ] `toDomain(ProductOutboxJpaEntity)` 구현
- [ ] `toEntity(ProductOutbox)` 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ProductOutboxMapper 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ProductOutboxMapper 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: ProductOutboxMapper 테스트 정리 (Tidy)`

---

### 1️⃣2️⃣ SellerJpaRepository 구현 (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `SellerJpaRepositoryTest.java` 생성 (@DataJpaTest + TestContainers)
- [ ] `shouldFindBySellerId()` 작성
- [ ] `shouldFindByStatus()` 작성
- [ ] `shouldCheckExistsBySellerId()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SellerJpaRepository 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerJpaRepository.java` 인터페이스 생성
- [ ] `extends JpaRepository<SellerJpaEntity, Long>`
- [ ] `Optional<SellerJpaEntity> findBySellerId(String sellerId)` 정의
- [ ] `List<SellerJpaEntity> findByStatus(String status)` 정의
- [ ] `boolean existsBySellerId(String sellerId)` 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SellerJpaRepository 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (JPA Repository 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SellerJpaRepository 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestContainers Fixture 정리
- [ ] 커밋: `test: SellerJpaRepository 테스트 정리 (Tidy)`

---

### 1️⃣3️⃣ CrawlerTaskJpaRepository 구현 (Cycle 13)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlerTaskJpaRepositoryTest.java` 생성 (@DataJpaTest)
- [ ] `shouldFindByTaskId()` 작성
- [ ] `shouldFindByStatusWithPaging()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: CrawlerTaskJpaRepository 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlerTaskJpaRepository.java` 인터페이스 생성
- [ ] `Optional<CrawlerTaskJpaEntity> findByTaskId(String taskId)` 정의
- [ ] `List<CrawlerTaskJpaEntity> findByStatus(String status, Pageable pageable)` 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlerTaskJpaRepository 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlerTaskJpaRepository 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: CrawlerTaskJpaRepository 테스트 정리 (Tidy)`

---

### 1️⃣4️⃣ UserAgentJpaRepository 구현 (Cycle 14)

#### 🔴 Red: 테스트 작성
- [ ] `UserAgentJpaRepositoryTest.java` 생성
- [ ] `shouldFindByStatus()` 작성
- [ ] `shouldFindFirstActiveUserAgentForUpdate()` 작성 (Pessimistic Lock)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: UserAgentJpaRepository 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UserAgentJpaRepository.java` 인터페이스 생성
- [ ] `List<UserAgentJpaEntity> findByStatus(String status)` 정의
- [ ] `@Lock(LockModeType.PESSIMISTIC_WRITE)` 적용
- [ ] `@Query("SELECT ua FROM UserAgentJpaEntity ua WHERE ua.status = 'ACTIVE' ORDER BY ua.lastRequestAt ASC")` 작성
- [ ] `Optional<UserAgentJpaEntity> findFirstActiveUserAgentForUpdate()` 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: UserAgentJpaRepository 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Pessimistic Lock 동작 검증 (동시성 테스트)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: UserAgentJpaRepository 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: UserAgentJpaRepository 테스트 정리 (Tidy)`

---

### 1️⃣5️⃣ ProductJpaRepository 구현 (Cycle 15)

#### 🔴 Red: 테스트 작성
- [ ] `ProductJpaRepositoryTest.java` 생성
- [ ] `shouldFindByItemNo()` 작성
- [ ] `shouldFindBySellerIdAndIsComplete()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ProductJpaRepository 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProductJpaRepository.java` 인터페이스 생성
- [ ] `Optional<ProductJpaEntity> findByItemNo(Long itemNo)` 정의
- [ ] `List<ProductJpaEntity> findBySellerIdAndIsComplete(String sellerId, boolean isComplete)` 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ProductJpaRepository 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ProductJpaRepository 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: ProductJpaRepository 테스트 정리 (Tidy)`

---

### 1️⃣6️⃣ ProductOutboxJpaRepository 구현 (Cycle 16)

#### 🔴 Red: 테스트 작성
- [ ] `ProductOutboxJpaRepositoryTest.java` 생성
- [ ] `shouldFindByStatusOrderByCreatedAtAsc()` 작성 (배치 처리)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ProductOutboxJpaRepository 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProductOutboxJpaRepository.java` 인터페이스 생성
- [ ] `List<ProductOutboxJpaEntity> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable)` 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ProductOutboxJpaRepository 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 배치 처리 정렬 순서 검증 (오래된 순)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ProductOutboxJpaRepository 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: ProductOutboxJpaRepository 테스트 정리 (Tidy)`

---

### 1️⃣7️⃣ QueryDSL 설정 및 QClass 생성 (Cycle 17)

#### 🔴 Red: 테스트 작성
- [ ] `QueryDslConfigTest.java` 생성
- [ ] JPAQueryFactory 빈 등록 테스트
- [ ] QClass 생성 확인 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: QueryDSL 설정 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `QueryDslConfig.java` 생성
- [ ] `JPAQueryFactory` 빈 등록
- [ ] Gradle 설정: QueryDSL 플러그인 추가
- [ ] QClass 생성 (gradle build)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: QueryDSL 설정 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] QueryDSL 설정 검증
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: QueryDSL 설정 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 코드 정리
- [ ] 커밋: `test: QueryDSL 설정 테스트 정리 (Tidy)`

---

### 1️⃣8️⃣ CrawlerTaskQueryDslRepository 구현 - Part 1 (findBySellerIdAndDateRange) (Cycle 18)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlerTaskQueryDslRepositoryTest.java` 생성 (@DataJpaTest)
- [ ] `shouldFindBySellerIdAndDateRange()` 작성
- [ ] DTO Projection 검증 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: CrawlerTaskQueryDslRepository 기간 조회 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlerTaskQueryDslRepository.java` 생성 (`@Repository`)
- [ ] JPAQueryFactory 주입
- [ ] `findBySellerIdAndDateRange(String sellerId, LocalDate startDate, LocalDate endDate)` 구현
- [ ] QueryDSL 쿼리 작성 (QCrawlerTaskJpaEntity 사용)
- [ ] DTO Projection (Projections.constructor 사용)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlerTaskQueryDslRepository 기간 조회 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] N+1 방지 확인 (DTO Projection)
- [ ] 쿼리 최적화 검증
- [ ] ArchUnit 테스트 추가 (QueryDSL Repository 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlerTaskQueryDslRepository 기간 조회 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] DTO Fixture 생성
- [ ] 커밋: `test: CrawlerTaskQueryDslRepository 기간 조회 테스트 정리 (Tidy)`

---

### 1️⃣9️⃣ CrawlerTaskQueryDslRepository 구현 - Part 2 (countBySellerIdAndStatusAndDate) (Cycle 19)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCountBySellerIdAndStatusAndDate()` 작성
- [ ] 메트릭 집계 검증 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: CrawlerTaskQueryDslRepository 메트릭 집계 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `countBySellerIdAndStatusAndDate(String sellerId, LocalDate date)` 메서드 구현
- [ ] 자정 기준 (00:00-24:00) 쿼리 작성
- [ ] `groupBy(crawlerTask.status)` 사용
- [ ] DTO Projection (CrawlerTaskMetricsDto)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlerTaskQueryDslRepository 메트릭 집계 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 집계 쿼리 최적화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlerTaskQueryDslRepository 메트릭 집계 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 메트릭 DTO Fixture 생성
- [ ] 커밋: `test: CrawlerTaskQueryDslRepository 메트릭 집계 테스트 정리 (Tidy)`

---

### 2️⃣0️⃣ SellerCommandAdapter 구현 (Cycle 20)

#### 🔴 Red: 테스트 작성
- [ ] `SellerCommandAdapterTest.java` 생성 (TestContainers)
- [ ] `shouldSaveSeller()` 작성
- [ ] `shouldDeleteSeller()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SellerCommandAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerCommandAdapter.java` 생성 (`@Component`)
- [ ] SellerCommandPort 구현
- [ ] SellerJpaRepository 주입
- [ ] SellerMapper 주입
- [ ] `save(Seller)` 메서드 구현: Domain → Entity → 저장
- [ ] `delete(String sellerId)` 메서드 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SellerCommandAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Command Adapter 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SellerCommandAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: SellerCommandAdapter 테스트 정리 (Tidy)`

---

### 2️⃣1️⃣ CrawlerTaskCommandAdapter 구현 (Cycle 21)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlerTaskCommandAdapterTest.java` 생성
- [ ] `shouldSaveCrawlerTask()` 작성
- [ ] `shouldBulkInsertCrawlerTasks()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: CrawlerTaskCommandAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlerTaskCommandAdapter.java` 생성
- [ ] CrawlerTaskCommandPort 구현
- [ ] `save(CrawlerTask)` 구현
- [ ] `saveAll(List<CrawlerTask>)` 구현 (Bulk Insert)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlerTaskCommandAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Bulk Insert 최적화 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlerTaskCommandAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: CrawlerTaskCommandAdapter 테스트 정리 (Tidy)`

---

### 2️⃣2️⃣ UserAgentCommandAdapter 구현 (Cycle 22)

#### 🔴 Red: 테스트 작성
- [ ] `UserAgentCommandAdapterTest.java` 생성
- [ ] `shouldSaveUserAgent()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: UserAgentCommandAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UserAgentCommandAdapter.java` 생성
- [ ] UserAgentCommandPort 구현
- [ ] `save(UserAgent)` 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: UserAgentCommandAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: UserAgentCommandAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: UserAgentCommandAdapter 테스트 정리 (Tidy)`

---

### 2️⃣3️⃣ ProductCommandAdapter 구현 (Cycle 23)

#### 🔴 Red: 테스트 작성
- [ ] `ProductCommandAdapterTest.java` 생성
- [ ] `shouldSaveProduct()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ProductCommandAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProductCommandAdapter.java` 생성
- [ ] ProductCommandPort 구현
- [ ] `save(Product)` 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ProductCommandAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ProductCommandAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: ProductCommandAdapter 테스트 정리 (Tidy)`

---

### 2️⃣4️⃣ ProductOutboxCommandAdapter 구현 (Cycle 24)

#### 🔴 Red: 테스트 작성
- [ ] `ProductOutboxCommandAdapterTest.java` 생성
- [ ] `shouldSaveProductOutbox()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ProductOutboxCommandAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProductOutboxCommandAdapter.java` 생성
- [ ] ProductOutboxCommandPort 구현
- [ ] `save(ProductOutbox)` 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ProductOutboxCommandAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ProductOutboxCommandAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: ProductOutboxCommandAdapter 테스트 정리 (Tidy)`

---

### 2️⃣5️⃣ SellerQueryAdapter 구현 (Cycle 25)

#### 🔴 Red: 테스트 작성
- [ ] `SellerQueryAdapterTest.java` 생성 (TestContainers)
- [ ] `shouldFindById()` 작성
- [ ] `shouldFindByStatus()` 작성
- [ ] `shouldCheckExists()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SellerQueryAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerQueryAdapter.java` 생성 (`@Component`)
- [ ] SellerQueryPort 구현
- [ ] `findById(String sellerId)` 구현
- [ ] `findByStatus(SellerStatus status)` 구현
- [ ] `existsBySellerId(String sellerId)` 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SellerQueryAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Query Adapter 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SellerQueryAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: SellerQueryAdapter 테스트 정리 (Tidy)`

---

### 2️⃣6️⃣ CrawlerTaskQueryAdapter 구현 (Cycle 26)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlerTaskQueryAdapterTest.java` 생성
- [ ] `shouldFindById()` 작성
- [ ] `shouldFindByStatusWithPaging()` 작성
- [ ] `shouldFindBySellerIdAndDateRange()` 작성
- [ ] `shouldCountBySellerIdAndStatusAndDate()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: CrawlerTaskQueryAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlerTaskQueryAdapter.java` 생성
- [ ] CrawlerTaskQueryPort 구현
- [ ] JPA Repository + QueryDSL Repository 주입
- [ ] `findById()`, `findByStatus()` 구현 (JPA Repository 사용)
- [ ] `findBySellerIdAndDateRange()`, `countBySellerIdAndStatusAndDate()` 구현 (QueryDSL 사용)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlerTaskQueryAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] N+1 방지 검증 (DTO Projection)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlerTaskQueryAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: CrawlerTaskQueryAdapter 테스트 정리 (Tidy)`

---

### 2️⃣7️⃣ UserAgentQueryAdapter 구현 (Cycle 27)

#### 🔴 Red: 테스트 작성
- [ ] `UserAgentQueryAdapterTest.java` 생성
- [ ] `shouldFindById()` 작성
- [ ] `shouldFindByStatus()` 작성
- [ ] `shouldFindFirstActiveForUpdate()` 작성 (Pessimistic Lock 검증)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: UserAgentQueryAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UserAgentQueryAdapter.java` 생성
- [ ] UserAgentQueryPort 구현
- [ ] `findById()`, `findByStatus()` 구현
- [ ] `findFirstActiveForUpdate()` 구현 (Pessimistic Lock)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: UserAgentQueryAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Pessimistic Lock 동시성 테스트 추가
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: UserAgentQueryAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: UserAgentQueryAdapter 테스트 정리 (Tidy)`

---

### 2️⃣8️⃣ ProductQueryAdapter 구현 (Cycle 28)

#### 🔴 Red: 테스트 작성
- [ ] `ProductQueryAdapterTest.java` 생성
- [ ] `shouldFindByItemNo()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ProductQueryAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProductQueryAdapter.java` 생성
- [ ] ProductQueryPort 구현
- [ ] `findByItemNo(Long itemNo)` 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ProductQueryAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ProductQueryAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: ProductQueryAdapter 테스트 정리 (Tidy)`

---

### 2️⃣9️⃣ ProductOutboxQueryAdapter 구현 (Cycle 29)

#### 🔴 Red: 테스트 작성
- [ ] `ProductOutboxQueryAdapterTest.java` 생성
- [ ] `shouldFindByStatusOrderByCreatedAtAsc()` 작성
- [ ] 배치 처리 정렬 검증 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ProductOutboxQueryAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProductOutboxQueryAdapter.java` 생성
- [ ] ProductOutboxQueryPort 구현
- [ ] `findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable)` 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ProductOutboxQueryAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 정렬 순서 검증 (오래된 순)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ProductOutboxQueryAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: ProductOutboxQueryAdapter 테스트 정리 (Tidy)`

---

### 3️⃣0️⃣ Flyway 마이그레이션 - V1 (sellers 테이블) (Cycle 30)

#### 🔴 Red: 테스트 작성
- [ ] `FlywayMigrationTest.java` 생성 (TestContainers)
- [ ] `shouldApplyV1Migration()` 작성
- [ ] sellers 테이블 존재 확인 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Flyway V1 마이그레이션 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `V1__create_sellers_table.sql` 작성
- [ ] CREATE TABLE sellers 구문
- [ ] 제약 조건: CHECK (crawling_interval_days > 0)
- [ ] 인덱스: uk_seller_id (Unique), idx_status
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Flyway V1 마이그레이션 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] SQL 문법 검증
- [ ] 인덱스 최적화 확인
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Flyway V1 마이그레이션 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 마이그레이션 테스트 정리
- [ ] 커밋: `test: Flyway V1 마이그레이션 테스트 정리 (Tidy)`

---

### 3️⃣1️⃣ Flyway 마이그레이션 - V2 (crawler_tasks 테이블) (Cycle 31)

#### 🔴 Red: 테스트 작성
- [ ] `shouldApplyV2Migration()` 작성
- [ ] crawler_tasks 테이블 존재 확인 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Flyway V2 마이그레이션 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `V2__create_crawler_tasks_table.sql` 작성
- [ ] CREATE TABLE crawler_tasks 구문
- [ ] 인덱스: uk_task_id (Unique), idx_seller_id_created_at, idx_status_created_at
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Flyway V2 마이그레이션 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Composite Index 최적화 확인
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Flyway V2 마이그레이션 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 마이그레이션 테스트 정리
- [ ] 커밋: `test: Flyway V2 마이그레이션 테스트 정리 (Tidy)`

---

### 3️⃣2️⃣ Flyway 마이그레이션 - V3 (user_agents 테이블) (Cycle 32)

#### 🔴 Red: 테스트 작성
- [ ] `shouldApplyV3Migration()` 작성
- [ ] user_agents 테이블 존재 확인 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Flyway V3 마이그레이션 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `V3__create_user_agents_table.sql` 작성
- [ ] CREATE TABLE user_agents 구문
- [ ] 인덱스: uk_user_agent_id (Unique), idx_status
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Flyway V3 마이그레이션 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] SQL 검증
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Flyway V3 마이그레이션 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 마이그레이션 테스트 정리
- [ ] 커밋: `test: Flyway V3 마이그레이션 테스트 정리 (Tidy)`

---

### 3️⃣3️⃣ Flyway 마이그레이션 - V4 (products 테이블) (Cycle 33)

#### 🔴 Red: 테스트 작성
- [ ] `shouldApplyV4Migration()` 작성
- [ ] products 테이블 존재 확인 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Flyway V4 마이그레이션 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `V4__create_products_table.sql` 작성
- [ ] CREATE TABLE products 구문
- [ ] 인덱스: uk_product_id (Unique), uk_item_no (Unique), idx_seller_id_is_complete
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Flyway V4 마이그레이션 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Unique Index 최적화 확인
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Flyway V4 마이그레이션 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 마이그레이션 테스트 정리
- [ ] 커밋: `test: Flyway V4 마이그레이션 테스트 정리 (Tidy)`

---

### 3️⃣4️⃣ Flyway 마이그레이션 - V5 (product_raw_data 테이블) (Cycle 34)

#### 🔴 Red: 테스트 작성
- [ ] `shouldApplyV5Migration()` 작성
- [ ] product_raw_data 테이블 존재 확인 테스트
- [ ] TEXT 타입 검증 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Flyway V5 마이그레이션 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `V5__create_product_raw_data_table.sql` 작성
- [ ] CREATE TABLE product_raw_data 구문
- [ ] raw_json TEXT 타입 설정
- [ ] 인덱스: idx_product_id_data_type
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Flyway V5 마이그레이션 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] TEXT 타입 저장 전략 검증
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Flyway V5 마이그레이션 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 마이그레이션 테스트 정리
- [ ] 커밋: `test: Flyway V5 마이그레이션 테스트 정리 (Tidy)`

---

### 3️⃣5️⃣ Flyway 마이그레이션 - V6 (product_outbox 테이블) (Cycle 35)

#### 🔴 Red: 테스트 작성
- [ ] `shouldApplyV6Migration()` 작성
- [ ] product_outbox 테이블 존재 확인 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Flyway V6 마이그레이션 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `V6__create_product_outbox_table.sql` 작성
- [ ] CREATE TABLE product_outbox 구문
- [ ] payload TEXT 타입 설정
- [ ] 인덱스: uk_outbox_id (Unique), idx_status_created_at
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Flyway V6 마이그레이션 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Outbox 패턴 인덱스 최적화 확인 (배치 처리)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Flyway V6 마이그레이션 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 마이그레이션 테스트 정리
- [ ] 커밋: `test: Flyway V6 마이그레이션 테스트 정리 (Tidy)`

---

### 3️⃣6️⃣ Flyway 마이그레이션 - V7 (인덱스 생성) (Cycle 36)

#### 🔴 Red: 테스트 작성
- [ ] `shouldApplyV7Migration()` 작성
- [ ] 인덱스 존재 확인 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Flyway V7 마이그레이션 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `V7__create_indexes.sql` 작성
- [ ] CREATE INDEX 구문 (V1-V6에서 누락된 인덱스)
- [ ] Covering Index 추가 (자주 조회되는 컬럼)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Flyway V7 마이그레이션 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 인덱스 최적화 전략 검증
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Flyway V7 마이그레이션 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 마이그레이션 테스트 정리
- [ ] 커밋: `test: Flyway V7 마이그레이션 테스트 정리 (Tidy)`

---

### 3️⃣7️⃣ Flyway 마이그레이션 - V8 (초기 UserAgent 데이터) (Cycle 37)

#### 🔴 Red: 테스트 작성
- [ ] `shouldApplyV8Migration()` 작성
- [ ] 50개 UserAgent 삽입 확인 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Flyway V8 마이그레이션 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `V8__insert_initial_user_agents.sql` 작성
- [ ] INSERT INTO user_agents 구문 (50개)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Flyway V8 마이그레이션 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 초기 데이터 검증
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Flyway V8 마이그레이션 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 마이그레이션 테스트 정리
- [ ] 커밋: `test: Flyway V8 마이그레이션 테스트 정리 (Tidy)`

---

### 3️⃣8️⃣ ArchUnit 테스트 - Long FK 전략 검증 (Cycle 38)

#### 🔴 Red: 테스트 작성
- [ ] `PersistenceArchUnitTest.java` 생성
- [ ] `shouldUseLongFKStrategy()` 작성
- [ ] JPA 관계 어노테이션 금지 검증 (@ManyToOne, @OneToMany 등)
- [ ] 테스트 실행 → 통과 확인 (이미 준수 중)
- [ ] 커밋: `test: Long FK 전략 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] `noClasses().that().areAnnotatedWith(Entity.class).should().haveAnnotation(ManyToOne.class)` 등
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Long FK 전략 ArchUnit 테스트 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Long FK 전략 ArchUnit 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ArchUnit 테스트 정리
- [ ] 커밋: `test: Long FK 전략 ArchUnit 테스트 정리 (Tidy)`

---

### 3️⃣9️⃣ ArchUnit 테스트 - Lombok 금지 검증 (Cycle 39)

#### 🔴 Red: 테스트 작성
- [ ] `shouldNotUseLombok()` 작성
- [ ] @Data, @Getter, @Setter 등 Lombok 어노테이션 금지 검증
- [ ] 테스트 실행 → 통과 확인 (이미 준수 중)
- [ ] 커밋: `test: Lombok 금지 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] `noClasses().should().beAnnotatedWith(Data.class)` 등
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Lombok 금지 ArchUnit 테스트 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Lombok 금지 ArchUnit 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ArchUnit 테스트 정리
- [ ] 커밋: `test: Lombok 금지 ArchUnit 테스트 정리 (Tidy)`

---

### 4️⃣0️⃣ ArchUnit 테스트 - QueryDSL 최적화 검증 (Cycle 40)

#### 🔴 Red: 테스트 작성
- [ ] `shouldUseQueryDslOptimization()` 작성
- [ ] DTO Projection 사용 검증
- [ ] 테스트 실행 → 통과 확인 (이미 준수 중)
- [ ] 커밋: `test: QueryDSL 최적화 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] QueryDSL Repository 명명 규칙 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: QueryDSL 최적화 ArchUnit 테스트 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: QueryDSL 최적화 ArchUnit 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ArchUnit 테스트 정리
- [ ] 커밋: `test: QueryDSL 최적화 ArchUnit 테스트 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 40개 TDD 사이클 모두 완료 (160개 체크박스 모두 ✅)
- [ ] 모든 테스트 통과 (TestContainers, Integration Test)
- [ ] ArchUnit 테스트 통과 (Long FK, Lombok 금지, QueryDSL 최적화)
- [ ] Zero-Tolerance 규칙 준수
  - [ ] Long FK 전략 (JPA 관계 어노테이션 금지)
  - [ ] QueryDSL 최적화 (N+1 방지, DTO Projection)
  - [ ] Lombok 금지 (Plain Java)
  - [ ] Pessimistic Lock 사용 (UserAgent 할당)
- [ ] Flyway 마이그레이션 스크립트 작성 완료 (V1-V8)
- [ ] TestFixture 모두 정리 (Object Mother 패턴)
- [ ] 테스트 커버리지 > 80%

---

## 🔗 관련 문서

- Task: docs/prd/tasks/MUSTIT-003.md
- PRD: docs/prd/mustit-seller-crawler.md
- Persistence Layer 규칙: docs/coding_convention/04-persistence-layer/

---

## 📚 참고사항

### Flyway 마이그레이션 예시

```sql
-- V1__create_sellers_table.sql
CREATE TABLE sellers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    crawling_interval_days INT NOT NULL CHECK (crawling_interval_days > 0),
    status VARCHAR(50) NOT NULL,
    total_product_count INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_seller_id (seller_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### QueryDSL DTO Projection 예시

```java
// CrawlerTaskQueryDslRepository
public List<CrawlerTaskMetricsDto> countBySellerIdAndStatusAndDate(
    String sellerId, LocalDate date) {

    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

    return queryFactory
        .select(Projections.constructor(
            CrawlerTaskMetricsDto.class,
            crawlerTask.status,
            crawlerTask.count()
        ))
        .from(crawlerTask)
        .where(
            crawlerTask.sellerId.eq(sellerId),
            crawlerTask.createdAt.between(startOfDay, endOfDay)
        )
        .groupBy(crawlerTask.status)
        .fetch();
}
```

### Pessimistic Lock 예시

```java
// UserAgentJpaRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT ua FROM UserAgentJpaEntity ua WHERE ua.status = 'ACTIVE' ORDER BY ua.lastRequestAt ASC")
Optional<UserAgentJpaEntity> findFirstActiveUserAgentForUpdate();
```

### Bulk Insert 최적화

```java
// CrawlerTaskCommandAdapter
@Transactional
public void saveAll(List<CrawlerTask> tasks) {
    List<CrawlerTaskJpaEntity> entities = tasks.stream()
        .map(mapper::toEntity)
        .toList();

    // Batch Insert (한 번에 저장)
    crawlerTaskJpaRepository.saveAll(entities);
}
```

### TestContainers 설정

```java
@SpringBootTest
@Testcontainers
class SellerCommandAdapterTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Test
    void save_seller_success() {
        // Given
        Seller seller = Seller.register("seller_123", "셀러명", 1);

        // When
        sellerCommandAdapter.save(seller);

        // Then
        Optional<Seller> found = sellerQueryAdapter.findById(seller.getSellerId());
        assertThat(found).isPresent();
    }
}
```
