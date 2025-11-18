# SELLER-003: Seller Persistence Layer 구현

**Epic**: 머스트잇 셀러 크롤링 스케줄러
**Bounded Context**: Seller
**Layer**: Persistence
**브랜치**: feature/SELLER-003-persistence
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

Seller 바운더리 컨텍스트의 데이터 저장 및 조회를 Persistence Layer에서 구현합니다.

**핵심 기능**:
- JPA Entity 설계 (Long FK 전략)
- Repository 구현 (JPA + QueryDSL)
- Port Adapter 구현 (Hexagonal Architecture)
- 동시성 제어 (필요 시)

---

## 🎯 요구사항

### JPA Entity

#### SellerJpaEntity

- [ ] **테이블**: `sellers`
- [ ] **필드**:
  - `id`: Long (PK, Auto Increment)
  - `must_it_seller_id`: String (Unique, Not Null, Index)
  - `seller_name`: String (Unique, Not Null, Index)
  - `status`: String (Not Null, Index)
  - `created_at`: LocalDateTime (Not Null)
  - `updated_at`: LocalDateTime (Not Null)

- [ ] **인덱스**:
  - `idx_must_it_seller_id` (must_it_seller_id) - Unique
  - `idx_seller_name` (seller_name) - Unique
  - `idx_status` (status) - 필터링용

- [ ] **Unique Constraint**:
  - `must_it_seller_id`
  - `seller_name`

- [ ] **BaseAuditEntity 상속**:
  - `createdAt`, `updatedAt` 자동 관리

### Repository 인터페이스

#### SellerJpaRepository (Spring Data JPA)

- [ ] **메서드**:
  ```java
  public interface SellerJpaRepository extends JpaRepository<SellerJpaEntity, Long> {
      Optional<SellerJpaEntity> findByMustItSellerId(String mustItSellerId);
      Optional<SellerJpaEntity> findBySellerName(String sellerName);
      boolean existsByMustItSellerId(String mustItSellerId);
      boolean existsBySellerName(String sellerName);
  }
  ```

#### SellerQueryDslRepository (Custom Repository)

- [ ] **메서드**:
  - `Page<SellerJpaEntity> findAllByStatus(SellerStatus status, Pageable pageable)`
  - DTO Projection 최적화 (N+1 방지)

- [ ] **구현**:
  ```java
  public class SellerQueryDslRepositoryImpl implements SellerQueryDslRepository {
      // QueryDSL 사용
  }
  ```

### Adapter 구현 (Port 구현체)

#### SellerCommandAdapter

- [ ] **구현 Port**: `SellerCommandPort`
- [ ] **메서드**:
  - `Seller persist(Seller seller)`  ⬅️ **Persist 포트 통일 패턴**
    - 생성/수정/소프트삭제 모두 `persist(aggregate)`로 통일
    - Domain Aggregate → JpaEntity 변환 (Mapper)
    - JpaRepository.save()
    - JpaEntity → Domain Aggregate 변환 (Mapper)
  - `void delete(Long sellerId)`  ⬅️ **물리 삭제 (하드 딜리트)**

#### SellerQueryAdapter

- [ ] **구현 Port**: `SellerQueryPort`
- [ ] **메서드**:
  - `Optional<Seller> findById(Long sellerId)`
  - `Optional<Seller> findByMustItSellerId(String mustItSellerId)`
  - `Optional<Seller> findBySellerName(String sellerName)`
  - `boolean existsByMustItSellerId(String mustItSellerId)`
  - `boolean existsBySellerName(String sellerName)`
  - `Page<Seller> findAllByStatus(SellerStatus status, Pageable pageable)`

### Mapper

#### SellerMapper

- [ ] **메서드**:
  - `SellerJpaEntity toJpaEntity(Seller seller)`
  - `Seller toDomain(SellerJpaEntity entity)`
  - `List<Seller> toDomainList(List<SellerJpaEntity> entities)`

- [ ] **매핑 규칙**:
  - `Seller.sellerId` ↔ `SellerJpaEntity.id`
  - `Seller.status` (Enum) ↔ `SellerJpaEntity.status` (String)
  - `Seller.createdAt` ↔ `SellerJpaEntity.createdAt`

- [ ] **매퍼 구현 원칙**:
  - ⚠️ **Setter 절대 금지**: 엔티티 생성 시 생성자 또는 정적 팩토리 메서드 사용
  - ✅ 상태는 생성 시점에 확정 (`new SellerJpaEntity(...)` 또는 `SellerJpaEntity.of(...)`)
  - ❌ 절대 금지: `entity.setStatus(...)`, `entity.setSellerName(...)` 같은 세터 호출

### 동시성 제어

- [ ] **Seller 등록 시 동시 요청 처리**:
  - 전략: 동시성 제어 불필요 (단일 사용자)
  - 이유: 관리자 1명만 사용

- [ ] **Unique Constraint 의존**:
  - `must_it_seller_id`, `seller_name` 중복 방지는 DB Constraint로 처리

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Long FK 전략 (필수!)**
  - JPA 관계 어노테이션 절대 금지
  - 예시:
    - ✅ `private Long sellerId;`
    - ❌ `@ManyToOne private Seller seller;`
    - ❌ `@OneToMany private List<Scheduler> schedulers;`

- [ ] **Lombok 금지**
  - Pure Java 또는 Record 사용
  - Getter/Setter 수동 구현

- [ ] **QueryDSL 최적화**
  - N+1 문제 방지 (DTO Projection)
  - 복잡한 조회 쿼리는 QueryDSL 사용

- [ ] **엔티티 재조립 패턴**
  - ⚠️ 엔티티에 연관관계 없음 → 필요한 그래프는 Application Layer에서 재조립
  - 예시: Seller + Scheduler 조회 시
    1. SellerQueryAdapter에서 Seller 조회
    2. SchedulerQueryAdapter에서 Scheduler 조회
    3. Application Layer에서 두 결과를 조합하여 Response DTO 생성

- [ ] **Open-in-View 비활성화**
  - `spring.jpa.open-in-view=false` (필수)
  - 읽기는 Projection/QueryDSL DTO로 해결
  - 엔티티를 API로 직접 노출 금지

- [ ] **BaseAuditEntity 상속**
  - `createdAt`, `updatedAt` 자동 관리

### 테스트 규칙

- [ ] **Integration Test 필수**
  - TestContainers MySQL 사용
  - 실제 DB 쿼리 검증

- [ ] **Unique Constraint 테스트**
  - 중복 `must_it_seller_id` 저장 시 예외 발생 검증
  - 중복 `seller_name` 저장 시 예외 발생 검증

- [ ] **QueryDSL 쿼리 테스트**
  - N+1 문제 발생 여부 검증
  - DTO Projection 검증

- [ ] **Mapper 테스트**
  - Domain ↔ JpaEntity 변환 검증

- [ ] **ArchUnit 테스트**
  - Persistence Layer는 Domain, Application Layer 의존 가능
  - Adapter는 Port 구현 검증

- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] JPA Entity 구현 완료
  - SellerJpaEntity
  - BaseAuditEntity 상속

- [ ] Repository 구현 완료
  - SellerJpaRepository (Spring Data JPA)
  - SellerQueryDslRepository (QueryDSL)

- [ ] Adapter 구현 완료
  - SellerCommandAdapter
  - SellerQueryAdapter

- [ ] Mapper 구현 완료
  - SellerMapper

- [ ] Integration Test 완료
  - CRUD 테스트 (TestContainers)
  - Unique Constraint 테스트
  - QueryDSL 쿼리 테스트
  - N+1 방지 검증

- [ ] ArchUnit 테스트 완료
  - Persistence Layer 의존성 검증
  - Long FK 전략 검증

- [ ] Zero-Tolerance 규칙 준수 확인
  - Long FK 전략 준수
  - Lombok 미사용
  - QueryDSL 최적화

- [ ] Flyway Migration 스크립트 작성
  - `V001__Create_sellers_table.sql`

- [ ] 코드 리뷰 승인

- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **Plan**: `docs/prd/seller/plans/SELLER-003-persistence-plan.md` (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: `docs/coding_convention/04-persistence-layer/mysql/`
- **선행 Task**: SELLER-001 (Domain), SELLER-002 (Application)

---

## 📋 다음 단계

1. `/create-plan SELLER-003` - TDD Plan 생성
2. `/kb/persistence/go` - Persistence Layer TDD 시작
