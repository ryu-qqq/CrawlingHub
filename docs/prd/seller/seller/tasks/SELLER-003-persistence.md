# SELLER-003: Seller Persistence Layer 구현

**Bounded Context**: Seller
**Sub-Context**: Seller (셀러 자체)
**Layer**: Persistence Layer
**브랜치**: feature/SELLER-003-persistence

---

## 📝 목적

Seller 데이터 영속성.

---

## 🎯 요구사항

### 1. JPA Entity

#### SellerEntity
- **엔티티 네이밍**: `*Entity` 접미사 사용 (Jpa 접두사 불필요)
- 테이블: `sellers`
- 인덱스:
  - `idx_seller_id` (seller_id) - Unique
  - `idx_status` (status)

### 2. Repository

**네이밍 규칙**:
- JPA Repository: `*Repository` (Jpa 접두사 불필요)
- QueryDSL Repository: `*QueryDslRepository`

- SellerRepository (JPA 기본)
  - `findBySellerId(String sellerId)` - sellerId로 조회
  - `existsBySellerId(String sellerId)` - 존재 여부
  - `findAll(Pageable)` - 페이징 조회

### 3. Adapter 구현 (Port 구현체)

**Adapter 구조 규칙**:
- Command Adapter: `*CommandAdapter` (CUD 연산, `mysql/adapter/command/`)
- Query Adapter: `*QueryAdapter` (Read 연산, `mysql/adapter/query/`)
- Mapper: `*EntityMapper` (Domain ↔ Entity 변환, `mysql/mapper/`)

#### Command Adapter
- **SellerCommandAdapter** (implements `SellerPersistencePort`)
  - save(), delete() 구현
  - Domain ↔ Entity 변환 (Mapper 사용)

#### Query Adapter
- **SellerQueryAdapter** (implements `SellerQueryPort`)
  - findBySellerId(), existsBySellerId(), findAll() 구현

#### Mapper
- **SellerEntityMapper** (Domain ↔ Entity 변환)
  - 위치: `mysql/mapper/`

### 3. Flyway

- V1__create_sellers_table.sql

---

## ✅ 완료 조건

- [ ] SellerEntity 구현 완료
- [ ] SellerRepository 구현 완료
- [ ] Command/Query Adapter 구현 완료
- [ ] EntityMapper 구현 완료

---

## 🔗 관련 문서

- **Plan**: docs/prd/seller/seller/plans/SELLER-003-persistence-plan.md

---

## 📚 참고사항

### SellerEntity

```java
@Entity
@Table(
    name = "sellers",
    indexes = {
        @Index(name = "idx_seller_id", columnList = "seller_id", unique = true),
        @Index(name = "idx_status", columnList = "status")
    }
)
public class SellerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false, unique = true, length = 50)
    private String sellerId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SellerStatus status;

    @Column(name = "total_product_count")
    private Integer totalProductCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Getters, Setters 생략
}
```

### SellerRepository

```java
public interface SellerRepository extends JpaRepository<SellerEntity, Long> {
    Optional<SellerEntity> findBySellerId(String sellerId);
    boolean existsBySellerId(String sellerId);
    Page<SellerEntity> findAll(Pageable pageable);
}
```

### SellerCommandAdapter (Command Port 구현체)

**위치**: `persistence-mysql/seller/adapter/command/`

```java
@PersistenceAdapter
@RequiredArgsConstructor
public class SellerCommandAdapter implements SellerPersistencePort {
    private final SellerRepository sellerRepository;
    private final SellerEntityMapper sellerEntityMapper;

    @Override
    public void save(Seller seller) {
        SellerEntity entity = sellerEntityMapper.toEntity(seller);
        sellerRepository.save(entity);
    }

    @Override
    public void delete(Seller seller) {
        SellerEntity entity = sellerEntityMapper.toEntity(seller);
        sellerRepository.delete(entity);
    }
}
```

### SellerQueryAdapter (Query Port 구현체)

**위치**: `persistence-mysql/seller/adapter/query/`

```java
@PersistenceAdapter
@RequiredArgsConstructor
public class SellerQueryAdapter implements SellerQueryPort {
    private final SellerRepository sellerRepository;
    private final SellerEntityMapper sellerEntityMapper;

    @Override
    public Optional<Seller> findBySellerId(SellerId sellerId) {
        return sellerRepository.findBySellerId(sellerId.value())
            .map(sellerEntityMapper::toDomain);
    }

    @Override
    public boolean existsBySellerId(String sellerId) {
        return sellerRepository.existsBySellerId(sellerId);
    }

    @Override
    public Page<Seller> findAll(Pageable pageable) {
        Page<SellerEntity> entities = sellerRepository.findAll(pageable);
        return entities.map(sellerEntityMapper::toDomain);
    }
}
```

### SellerEntityMapper (Domain ↔ Entity 변환)

**위치**: `persistence-mysql/seller/mapper/`

```java
@Component
public class SellerEntityMapper {

    public Seller toDomain(SellerEntity entity) {
        return new Seller(
            new SellerId(entity.getSellerId()),
            entity.getName(),
            entity.getStatus(),
            entity.getTotalProductCount(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public SellerEntity toEntity(Seller seller) {
        SellerEntity entity = new SellerEntity();
        entity.setSellerId(seller.getSellerIdValue());
        entity.setName(seller.getName());
        entity.setStatus(seller.getStatus());
        entity.setTotalProductCount(seller.getTotalProductCount());
        entity.setCreatedAt(seller.getCreatedAt());
        entity.setUpdatedAt(seller.getUpdatedAt());
        return entity;
    }
}
```

### Flyway Migration

```sql
-- V1__create_sellers_table.sql
CREATE TABLE sellers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id VARCHAR(50) NOT NULL UNIQUE COMMENT '셀러 ID',
    name VARCHAR(100) NOT NULL COMMENT '셀러 이름',
    status VARCHAR(20) NOT NULL COMMENT 'ACTIVE, INACTIVE',
    total_product_count INT DEFAULT 0 COMMENT '총 상품 수',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='셀러';
```

### Integration Test (TestContainers)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Transactional
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
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private SellerCommandAdapter sellerCommandAdapter;

    @Autowired
    private SellerQueryAdapter sellerQueryAdapter;

    @Test
    void 셀러_저장_성공() {
        // Given: Seller 생성
        Seller seller = Seller.create(new SellerId("SELLER-001"), "테스트 셀러");

        // When: 저장
        sellerCommandAdapter.save(seller);

        // Then: 조회 성공
        Optional<Seller> found = sellerQueryAdapter.findBySellerId(new SellerId("SELLER-001"));
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("테스트 셀러");
        assertThat(found.get().getStatus()).isEqualTo(SellerStatus.INACTIVE);
    }

    @Test
    void 중복_sellerId_존재_여부_확인() {
        // Given: Seller 저장
        Seller seller = Seller.create(new SellerId("SELLER-002"), "테스트 셀러");
        sellerCommandAdapter.save(seller);

        // When & Then: 존재 확인
        boolean exists = sellerQueryAdapter.existsBySellerId("SELLER-002");
        assertThat(exists).isTrue();

        boolean notExists = sellerQueryAdapter.existsBySellerId("SELLER-999");
        assertThat(notExists).isFalse();
    }
}
```

### 중요 변경사항

⚠️ **crawling_interval_days 컬럼 제거**:
- Seller는 스케줄링 정보를 저장하지 않음
- EventBridge Context의 `crawling_schedules` 테이블에서 관리
