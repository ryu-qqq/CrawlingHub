# 🎯 Hexagonal Architecture Coding Standards

이 문서는 **표준화된 Spring Boot 프로젝트**의 코딩 표준을 정의합니다.
모든 레이어는 **Hexagonal Architecture (Ports & Adapters)** 원칙을 엄격히 준수해야 합니다.

---

## 📐 아키텍처 원칙

### 의존성 방향 (Dependency Rule)

```
Bootstrap → Adapter → Application → Domain
               ↓           ↓
           (구현)      (인터페이스)
```

#### ✅ 허용되는 의존성
- **Domain**: 아무것도 의존하지 않음 (완전 독립)
- **Application**: Domain만 의존
- **Adapter-In**: Application(Port) + Domain 의존
- **Adapter-Out**: Application(Port) + Domain 의존
- **Bootstrap**: 모든 레이어 의존 (조립 목적)

#### ❌ 금지되는 의존성
- Adapter → Adapter (Adapter 간 직접 의존 절대 금지)
- Application → Adapter (구체 구현 의존 금지)
- Domain → 모든 외부 의존성 (완전 순수성)
- 모든 레이어의 순환 의존성

---

## 🏛️ Domain Layer 규칙

### 1. 완전한 순수성 (Purity)

#### ❌ 금지 사항
```java
// ❌ Spring Framework 의존
import org.springframework.*;

// ❌ JPA/Hibernate 의존
import jakarta.persistence.*;
import org.hibernate.*;

// ❌ Lombok
import lombok.*;

// ❌ 인프라 라이브러리
import com.amazonaws.*;
import org.apache.http.*;
```

#### ✅ 허용 사항
```java
// ✅ Java 표준 라이브러리
import java.util.*;
import java.time.*;

// ✅ Jakarta Validation (표준)
import jakarta.validation.*;

// ✅ 순수 유틸리티
import org.apache.commons.lang3.StringUtils;
```

### 2. 불변성 (Immutability)

#### ❌ Bad
```java
public class Order {
    private Long id;
    private String status;

    // ❌ Setter 금지
    public void setStatus(String status) {
        this.status = status;
    }
}
```

#### ✅ Good
```java
public class Order {
    private final OrderId id;
    private final OrderStatus status;

    private Order(OrderId id, OrderStatus status) {
        this.id = id;
        this.status = status;
    }

    // ✅ 수정은 새 객체 반환
    public Order confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Cannot confirm non-pending order");
        }
        return new Order(this.id, OrderStatus.CONFIRMED);
    }

    // ✅ Getter만
    public OrderId getId() { return id; }
    public OrderStatus getStatus() { return status; }
}
```

### 3. 생성 규칙

#### ❌ Bad
```java
// ❌ Public 생성자 금지
public class Order {
    public Order(Long id, String status) { }
}
```

#### ✅ Good
```java
public class Order {
    // ✅ Private 생성자
    private Order(OrderId id, OrderStatus status) {
        this.id = Objects.requireNonNull(id, "Order ID cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    // ✅ 정적 팩토리 메서드
    public static Order create(OrderId id, List<OrderItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        return new Order(id, OrderStatus.PENDING);
    }

    // ✅ 재구성용 (영속성 계층에서 복원 시)
    public static Order reconstitute(OrderId id, OrderStatus status, LocalDateTime createdAt) {
        Order order = new Order(id, status);
        // 추가 복원 로직
        return order;
    }
}
```

### 4. 비즈니스 로직 위치

#### ✅ 규칙
- 모든 비즈니스 규칙은 **Domain 객체 내부**에 위치
- 도메인 서비스는 **여러 Aggregate 간 로직**만 담당
- 계산, 검증, 상태 전이는 **Domain 객체 메서드**로

#### ✅ Good
```java
public class Order {
    public Money calculateTotal() {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }

    public Order cancel() {
        if (this.status == OrderStatus.SHIPPED) {
            throw new OrderAlreadyShippedException("Cannot cancel shipped order");
        }
        return new Order(this.id, OrderStatus.CANCELLED);
    }
}
```

### 5. Value Object

#### ✅ Record 사용 권장
```java
// ✅ 식별자가 없는 값 객체는 record
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}

public record OrderId(Long value) {
    public OrderId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
    }
}
```

### 6. 예외 처리

#### ✅ Domain 전용 예외
```java
// ✅ 도메인 예외 계층
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}

public class InvalidOrderStateException extends DomainException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}

public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException(OrderId orderId) {
        super("Order not found: " + orderId);
    }
}
```

---

## 🔧 Application Layer 규칙

### 1. Port 책임 원칙 (Port Responsibility Principle)

Port는 **레이어 간 인터페이스**로서 명확한 책임 범위를 가져야 합니다.

#### Port 종류별 책임

**Inbound Port (UseCase)**
- **책임**: 비즈니스 유스케이스 정의만
- **포함**: 메서드 시그니처, 입출력 DTO 정의
- **제외**: 비즈니스 규칙, 검증 로직, 트랜잭션 관리

**Outbound Port**
- **책임**: 데이터 영속성 및 외부 시스템 연동 추상화만
- **포함**: 저장, 조회, 삭제 같은 데이터 작업
- **제외**: 비즈니스 규칙, 검증 로직, 상태 전이

#### ❌ Bad - Port에 비즈니스 규칙 포함

```java
/**
 * UploadPolicy 삭제를 위한 Outbound Port
 */
public interface DeleteUploadPolicyPort {
    /**
     * PolicyKey에 해당하는 UploadPolicy를 삭제합니다.
     *
     * 비즈니스 규칙:
     * - 활성화된 정책은 삭제할 수 없습니다  ❌ Port의 책임 아님!
     * - PolicyKey에 해당하는 정책이 존재하지 않으면 예외 발생  ❌
     */
    void delete(PolicyKey policyKey);
}
```

**문제점:**
- 비즈니스 규칙은 Application Service의 책임
- Port는 순수 데이터 작업만 담당해야 함
- 책임 경계가 모호해짐

#### ✅ Good - Port는 순수 데이터 작업만

```java
/**
 * UploadPolicy 삭제를 위한 Outbound Port
 *
 * <p>Persistence Adapter에서 구현하며, 데이터 영속성 작업만 수행합니다.
 * 비즈니스 규칙 검증은 Application Service에서 처리합니다.</p>
 *
 * @see DeleteUploadPolicyService 비즈니스 규칙은 여기서 처리
 */
public interface DeleteUploadPolicyPort {
    /**
     * PolicyKey에 해당하는 UploadPolicy를 삭제합니다.
     *
     * @param policyKey 삭제할 정책의 키
     * @throws IllegalArgumentException policyKey가 null인 경우
     */
    void delete(PolicyKey policyKey);
}
```

```java
// ✅ 비즈니스 규칙은 Application Service에서
@UseCase
@Transactional
public class DeleteUploadPolicyService implements DeleteUploadPolicyUseCase {
    private final LoadUploadPolicyPort loadPort;
    private final DeleteUploadPolicyPort deletePort;

    @Override
    public void execute(DeletePolicyCommand command) {
        // ✅ 비즈니스 규칙 검증
        UploadPolicy policy = loadPort.loadByKey(command.policyKey())
            .orElseThrow(() -> new PolicyNotFoundException(...));

        if (policy.isActive()) {  // ✅ 비즈니스 규칙
            throw new IllegalStateException("활성 정책은 삭제 불가");
        }

        // ✅ 단순 데이터 작업만 Port로 위임
        deletePort.delete(command.policyKey());
    }
}
```

#### Port Javadoc 정책

**모든 public Port 인터페이스는 클래스 레벨 Javadoc 필수:**

```java
/**
 * UploadPolicy 생성을 위한 Inbound Port (Use Case)
 *
 * <p>외부(Web Adapter 등)에서 새로운 업로드 정책을 생성할 때 사용하는 인터페이스입니다.</p>
 *
 * @author your-name
 * @since 1.0.0
 */
public interface CreateUploadPolicyUseCase {
    UploadPolicyResponse execute(CreateUploadPolicyCommand command);
}

/**
 * UploadPolicy 저장을 위한 Outbound Port
 *
 * <p>Persistence Adapter에서 구현하며, UploadPolicy 엔티티의 저장을 담당합니다.
 * 비즈니스 규칙 검증은 Application Service에서 수행됩니다.</p>
 *
 * @see UploadPolicy
 * @see CreateUploadPolicyService
 */
public interface SaveUploadPolicyPort {
    /**
     * UploadPolicy를 저장합니다.
     *
     * @param policy 저장할 정책 (null 불가)
     * @return 저장된 정책 (ID 포함)
     * @throws IllegalArgumentException policy가 null인 경우
     */
    UploadPolicy save(UploadPolicy policy);
}
```

**Javadoc 필수 항목:**
- Port의 목적과 책임 범위
- Adapter Layer 구현 위치 언급
- 비즈니스 규칙 처리 위치 명시 (Service)
- 관련 Domain 객체 참조 (`@see`)

---

### 2. Port 정의

#### ✅ Input Port (UseCase)
```java
package application.order.port.in;

// ✅ 인터페이스로 정의, 단일 메서드 권장
public interface CreateOrderUseCase {
    CreateOrderResult execute(CreateOrderCommand command);
}
```

#### ✅ Output Port
```java
package application.order.port.out;

// ✅ 영속성 추상화
public interface SaveOrderPort {
    Order save(Order order);
}

public interface LoadOrderPort {
    Optional<Order> loadById(OrderId orderId);
}

// ✅ 외부 시스템 추상화
public interface SendOrderEventPort {
    void send(OrderCreatedEvent event);
}
```

### 2. 트랜잭션 관리

#### ❌ Bad - Adapter에 @Transactional
```java
// ❌ 절대 금지
@Component
public class OrderPersistenceAdapter implements SaveOrderPort {
    @Transactional  // ❌ Adapter에 트랜잭션 금지!
    public Order save(Order order) { }
}
```

#### ✅ Good - Application에 @Transactional
```java
// ✅ Application UseCase에만 트랜잭션
@UseCase
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final LoadUserPort loadUserPort;
    private final SaveOrderPort saveOrderPort;
    private final SendOrderEventPort sendEventPort;

    public CreateOrderService(
        LoadUserPort loadUserPort,
        SaveOrderPort saveOrderPort,
        SendOrderEventPort sendEventPort
    ) {
        this.loadUserPort = loadUserPort;
        this.saveOrderPort = saveOrderPort;
        this.sendEventPort = sendEventPort;
    }

    @Override
    public CreateOrderResult execute(CreateOrderCommand command) {
        // 1. Domain 객체 로드
        User user = loadUserPort.loadById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // 2. Domain 로직 실행
        Order order = Order.create(user.getId(), command.items());

        // 3. 저장
        Order savedOrder = saveOrderPort.save(order);

        // 4. 이벤트 발행
        sendEventPort.send(new OrderCreatedEvent(savedOrder.getId()));

        return CreateOrderResult.from(savedOrder);
    }
}

// ✅ Read 전용은 readOnly = true
@UseCase
@Transactional(readOnly = true)
public class GetOrderService implements GetOrderUseCase {
    // ...
}
```

### 3. UseCase DTO

#### ✅ Command/Query/Result 패턴
```java
// ✅ Command (쓰기 작업)
public record CreateOrderCommand(
    UserId userId,
    List<OrderItem> items
) {
    public CreateOrderCommand {
        Objects.requireNonNull(userId, "User ID required");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Items required");
        }
    }
}

// ✅ Query (읽기 작업)
public record GetOrderQuery(
    OrderId orderId
) {
    public GetOrderQuery {
        Objects.requireNonNull(orderId, "Order ID required");
    }
}

// ✅ Result
public record CreateOrderResult(
    OrderId orderId,
    OrderStatus status,
    Money total,
    LocalDateTime createdAt
) {
    public static CreateOrderResult from(Order order) {
        return new CreateOrderResult(
            order.getId(),
            order.getStatus(),
            order.calculateTotal(),
            order.getCreatedAt()
        );
    }
}
```

### 4. 의존성 규칙

#### ❌ Bad
```java
// ❌ JPA Repository 직접 의존
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;  // ❌ 구체 구현 의존
}

// ❌ JPA Entity 사용
@Service
public class OrderService {
    public OrderEntity getOrder(Long id) {  // ❌ Entity 노출
        return orderRepository.findById(id);
    }
}
```

#### ✅ Good
```java
// ✅ Port 인터페이스만 의존
@UseCase
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final LoadUserPort loadUserPort;      // ✅ 추상화된 Port
    private final SaveOrderPort saveOrderPort;    // ✅ 추상화된 Port

    // ✅ Domain 객체만 사용
    public CreateOrderResult execute(CreateOrderCommand command) {
        User user = loadUserPort.loadById(command.userId());
        Order order = Order.create(user.getId(), command.items());
        Order savedOrder = saveOrderPort.save(order);
        return CreateOrderResult.from(savedOrder);
    }
}
```

### 5. Test Double 작성 가이드

Port를 테스트하기 위한 Test Double(테스트 대역) 작성 패턴입니다.

#### 패턴 1: Inner Static Class (권장 - 단순한 경우)

```java
@DisplayName("CreateUploadPolicyService 단위 테스트")
class CreateUploadPolicyServiceTest {

    private CreateUploadPolicyService service;
    private TestLoadUploadPolicyPort loadPort;
    private TestSaveUploadPolicyPort savePort;

    @BeforeEach
    void setUp() {
        loadPort = new TestLoadUploadPolicyPort();
        savePort = new TestSaveUploadPolicyPort();
        service = new CreateUploadPolicyService(loadPort, savePort);
    }

    @Test
    @DisplayName("정책 생성 시 기존 정책이 없으면 성공")
    void createPolicy_WhenNoPreviousPolicy_ShouldSucceed() {
        // given
        loadPort.setPolicy(null);  // 기존 정책 없음
        CreatePolicyCommand command = new CreatePolicyCommand(...);

        // when
        PolicyResponse response = service.execute(command);

        // then
        assertThat(response).isNotNull();
        assertThat(savePort.getSavedPolicy()).isNotNull();
    }

    // ✅ Inner Static Class로 Test Double 구현
    static class TestLoadUploadPolicyPort implements LoadUploadPolicyPort {
        private UploadPolicy policy;

        void setPolicy(UploadPolicy policy) {
            this.policy = policy;
        }

        @Override
        public Optional<UploadPolicy> loadByKey(PolicyKey policyKey) {
            return Optional.ofNullable(policy);
        }
    }

    static class TestSaveUploadPolicyPort implements SaveUploadPolicyPort {
        private UploadPolicy savedPolicy;

        @Override
        public UploadPolicy save(UploadPolicy uploadPolicy) {
            this.savedPolicy = uploadPolicy;
            return uploadPolicy;
        }

        UploadPolicy getSavedPolicy() {
            return savedPolicy;
        }
    }
}
```

#### 패턴 2: 별도 Fixture Class (복잡한 경우)

```java
// test/.../fixture/UploadPolicyFixtures.java
public class UploadPolicyFixtures {

    /**
     * 여러 Port 구현을 통합한 In-Memory Test Double
     * 복잡한 상태 관리가 필요한 경우 사용
     */
    public static class InMemoryUploadPolicyPort implements
            LoadUploadPolicyPort,
            SaveUploadPolicyPort,
            UpdateUploadPolicyPort,
            DeleteUploadPolicyPort {

        private final Map<PolicyKey, UploadPolicy> storage = new HashMap<>();

        @Override
        public Optional<UploadPolicy> loadByKey(PolicyKey key) {
            return Optional.ofNullable(storage.get(key));
        }

        @Override
        public UploadPolicy save(UploadPolicy policy) {
            storage.put(policy.getPolicyKey(), policy);
            return policy;
        }

        @Override
        public UploadPolicy update(UploadPolicy policy) {
            // Application Service에서 존재 여부를 검증했다고 가정
            // Test Double은 데이터 저장/수정 작업에만 집중
            storage.put(policy.getPolicyKey(), policy);
            return policy;
        }

        @Override
        public void delete(PolicyKey key) {
            storage.remove(key);
        }

        // 테스트 편의 메서드
        public void clear() {
            storage.clear();
        }

        public int size() {
            return storage.size();
        }
    }
}

// 테스트 클래스에서 사용
@DisplayName("UploadPolicy 통합 테스트")
class UploadPolicyIntegrationTest {
    private InMemoryUploadPolicyPort policyPort;

    @BeforeEach
    void setUp() {
        policyPort = new InMemoryUploadPolicyPort();
    }

    @Test
    void multipleOperations() {
        // given
        UploadPolicy policy = createTestPolicy();

        // when
        policyPort.save(policy);
        UploadPolicy loaded = policyPort.loadByKey(policy.getPolicyKey()).orElseThrow();
        policyPort.delete(policy.getPolicyKey());

        // then
        assertThat(loaded).isEqualTo(policy);
        assertThat(policyPort.size()).isZero();
    }
}
```

#### 패턴 선택 기준

| 상황 | 권장 패턴 | 이유 |
|------|-----------|------|
| 단일 테스트 클래스에서만 사용 | Inner Static Class | 응집도 높음, 간단한 로직 |
| 여러 테스트 클래스에서 공유 | 별도 Fixture Class | 재사용성, 일관성 |
| 간단한 상태 관리 | Inner Static Class | 불필요한 복잡도 방지 |
| 복잡한 상태 관리 (CRUD) | 별도 Fixture Class | 상태 관리 로직 집중화 |
| Port 1-2개 | Inner Static Class | 코드 간결성 |
| Port 3개 이상 | 별도 Fixture Class | 통합 관리 용이 |

#### ❌ Mockito 사용 지양

```java
// ❌ 가능하면 피할 것
@Test
void shouldCreateOrder() {
    LoadOrderPort loadPort = Mockito.mock(LoadOrderPort.class);
    Mockito.when(loadPort.loadById(any())).thenReturn(Optional.of(order));
    // ...
}
```

**이유:**
- Mockito는 구현 세부사항에 의존하게 만듦
- 진짜 객체(Test Double)가 더 신뢰성 높음
- 리팩토링 시 테스트가 깨지기 쉬움

**예외적으로 허용:**
- 외부 시스템 연동 Port (AWS, 결제 게이트웨이 등)
- 복잡한 설정이 필요한 Port
- 테스트 대역 작성이 과도하게 복잡한 경우

---

## 💾 Persistence Adapter 규칙

### 1. JPA Entity 설계

#### ❌ Bad - 연관관계 사용
```java
// ❌ JPA 연관관계 절대 금지
@Entity
public class OrderEntity {
    @Id
    private Long id;

    @ManyToOne  // ❌ 금지!
    private UserEntity user;

    @OneToMany  // ❌ 금지!
    private List<OrderItemEntity> items;

    public void setStatus(String status) {  // ❌ Setter 금지!
        this.status = status;
    }
}
```

#### ✅ Good - 외래키만 사용
```java
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 외래키는 Long 타입 필드로만
    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ✅ JPA 전용 기본 생성자 (protected)
    protected OrderEntity() {}

    // ✅ Private 생성자
    private OrderEntity(Long userId, OrderStatus status, BigDecimal totalAmount) {
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    // ✅ 정적 팩토리 - 새 엔티티 생성
    public static OrderEntity create(Long userId, OrderStatus status, BigDecimal totalAmount) {
        return new OrderEntity(userId, status, totalAmount);
    }

    // ✅ 정적 팩토리 - DB에서 복원
    public static OrderEntity reconstitute(Long id, Long userId, OrderStatus status, BigDecimal totalAmount, LocalDateTime createdAt) {
        OrderEntity entity = new OrderEntity(userId, status, totalAmount);
        entity.id = id;
        entity.createdAt = createdAt;
        return entity;
    }

    // ✅ Getter만 (Setter 금지)
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

### 2. Entity ↔ Domain 매핑

#### ✅ Mapper 클래스 사용
```java
@Component
class OrderEntityMapper {

    // ✅ Entity → Domain
    public Order toDomain(OrderEntity entity) {
        return Order.reconstitute(
            OrderId.of(entity.getId()),
            UserId.of(entity.getUserId()),
            entity.getStatus(),
            Money.of(entity.getTotalAmount()),
            entity.getCreatedAt()
        );
    }

    // ✅ Domain → Entity
    public OrderEntity toEntity(Order domain) {
        if (domain.getId() == null) {
            // 신규 생성
            return OrderEntity.create(
                domain.getUserId().value(),
                domain.getStatus(),
                domain.getTotal().amount()
            );
        } else {
            // 기존 엔티티 복원
            return OrderEntity.reconstitute(
                domain.getId().value(),
                domain.getUserId().value(),
                domain.getStatus(),
                domain.getTotal().amount(),
                domain.getCreatedAt()
            );
        }
    }
}
```

### 3. Repository 구현

#### ✅ Package-Private JpaRepository
```java
// ✅ package-private (외부 노출 금지)
interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    // QueryDSL 사용 권장
}

// ✅ Port 구현 클래스만 public
@Component
public class OrderPersistenceAdapter implements SaveOrderPort, LoadOrderPort {

    private final OrderJpaRepository jpaRepository;
    private final OrderEntityMapper mapper;

    public OrderPersistenceAdapter(
        OrderJpaRepository jpaRepository,
        OrderEntityMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        OrderEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> loadById(OrderId orderId) {
        return jpaRepository.findById(orderId.value())
            .map(mapper::toDomain);
    }
}
```

### 4. QueryDSL 사용

#### ❌ Bad - JPQL 문자열
```java
// ❌ 문자열 쿼리 금지 (타입 안전성 없음)
@Query("SELECT o FROM OrderEntity o WHERE o.userId = :userId")
List<OrderEntity> findByUserId(@Param("userId") Long userId);
```

#### ✅ Good - QueryDSL
```java
@Repository
public class OrderQueryRepository {
    private final JPAQueryFactory queryFactory;

    public List<Order> findByUserId(UserId userId) {
        QOrderEntity order = QOrderEntity.orderEntity;

        return queryFactory
            .selectFrom(order)
            .where(order.userId.eq(userId.value()))
            .fetch()
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
}
```

### 5. 예외 처리

#### ✅ JPA 예외 → Domain 예외 변환
```java
@Component
public class OrderPersistenceAdapter implements LoadOrderPort {

    @Override
    public Order loadById(OrderId orderId) {
        try {
            return jpaRepository.findById(orderId.value())
                .map(mapper::toDomain)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        } catch (DataAccessException e) {
            throw new PersistenceException("Failed to load order", e);
        }
    }
}
```

---

## 🌐 Controller Adapter (Adapter-In-Web) 규칙

### 1. Controller 구조

#### ❌ Bad
```java
// ❌ 내부 클래스 금지
@RestController
public class OrderController {

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        // ...
    }

    // ❌ 내부 클래스 금지!
    public static class OrderRequest {
        public Long userId;
        public List<String> items;
    }

    // ❌ 내부 클래스 금지!
    public static class OrderResponse {
        public Long orderId;
        public String status;
    }
}
```

#### ✅ Good
```java
// ✅ Controller는 얇게 유지
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    // ✅ Constructor Injection
    public OrderController(
        CreateOrderUseCase createOrderUseCase,
        GetOrderUseCase getOrderUseCase
    ) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse createOrder(
        @Valid @RequestBody CreateOrderRequest request
    ) {
        CreateOrderCommand command = request.toCommand();
        CreateOrderResult result = createOrderUseCase.execute(command);
        return CreateOrderResponse.from(result);
    }

    @GetMapping("/{orderId}")
    public GetOrderResponse getOrder(@PathVariable Long orderId) {
        GetOrderQuery query = new GetOrderQuery(OrderId.of(orderId));
        GetOrderResult result = getOrderUseCase.execute(query);
        return GetOrderResponse.from(result);
    }
}
```

### 2. Request/Response DTO

#### ✅ Record 필수
```java
// ✅ 별도 파일: CreateOrderRequest.java
public record CreateOrderRequest(
    @NotNull(message = "User ID is required")
    Long userId,

    @NotEmpty(message = "Items cannot be empty")
    @Valid
    List<OrderItemRequest> items
) {
    // ✅ Compact Constructor에서 추가 검증
    public CreateOrderRequest {
        if (userId != null && userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
    }

    // ✅ Domain Command로 변환
    public CreateOrderCommand toCommand() {
        return new CreateOrderCommand(
            UserId.of(userId),
            items.stream()
                .map(OrderItemRequest::toDomain)
                .toList()
        );
    }
}

// ✅ 별도 파일: CreateOrderResponse.java
public record CreateOrderResponse(
    Long orderId,
    String status,
    BigDecimal totalAmount,
    LocalDateTime createdAt
) {
    public static CreateOrderResponse from(CreateOrderResult result) {
        return new CreateOrderResponse(
            result.orderId().value(),
            result.status().name(),
            result.total().amount(),
            result.createdAt()
        );
    }
}
```

### 3. 전역 예외 처리

#### ✅ @RestControllerAdvice
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleOrderNotFound(OrderNotFoundException e) {
        return new ErrorResponse("ORDER_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidOrderState(InvalidOrderStateException e) {
        return new ErrorResponse("INVALID_ORDER_STATE", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationErrors(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return new ErrorResponse("VALIDATION_ERROR", message);
    }
}

public record ErrorResponse(
    String code,
    String message,
    LocalDateTime timestamp
) {
    public ErrorResponse(String code, String message) {
        this(code, message, LocalDateTime.now());
    }
}
```

---

## ☁️ External System Adapter 규칙

### 1. AWS S3 Adapter 예제

#### ✅ Good
```java
@Component
public class S3FileStorageAdapter implements FileStoragePort {

    private final S3Client s3Client;
    private final S3Properties properties;

    public S3FileStorageAdapter(S3Client s3Client, S3Properties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public FileUrl upload(FileData fileData) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(fileData.path())
                .contentType(fileData.contentType())
                .build();

            s3Client.putObject(request, fileData.content());

            String url = buildUrl(fileData.path());
            return FileUrl.of(url);

        } catch (S3Exception e) {
            throw new FileStorageException("Failed to upload file", e);
        }
    }

    private String buildUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
            properties.bucket(),
            properties.region(),
            key
        );
    }
}

// ✅ Configuration Properties
@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
    @NotBlank String bucket,
    @NotBlank String region
) {
    public S3Properties {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("S3 bucket is required");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("S3 region is required");
        }
    }
}
```

---

## 🔧 공통 규칙

### 1. 패키지 구조 (Aggregate별 수직 슬라이싱)

**모든 레이어에 Aggregate별 서브패키지를 일관되게 적용합니다.**
자세한 내용은 [DDD_AGGREGATE_MIGRATION_GUIDE.md](DDD_AGGREGATE_MIGRATION_GUIDE.md)를 참조하세요.

```
com.example.project
├── domain/
│   ├── order/                      # Order Aggregate
│   │   ├── Order.java              # Aggregate Root
│   │   ├── OrderId.java            # Value Object
│   │   ├── OrderItem.java          # Entity
│   │   ├── OrderStatus.java        # Enum
│   │   ├── vo/                     # Value Objects
│   │   │   ├── Money.java
│   │   │   └── Quantity.java
│   │   ├── event/                  # Domain Events
│   │   │   ├── OrderPlacedEvent.java
│   │   │   └── OrderCancelledEvent.java
│   │   ├── exception/              # Domain Exceptions
│   │   │   └── InvalidOrderException.java
│   │   └── service/                # Domain Services
│   │       └── OrderDomainService.java
│   └── user/                       # User Aggregate
│       ├── User.java
│       ├── UserId.java
│       └── vo/
│
├── application/
│   ├── order/                      # Order Aggregate
│   │   ├── dto/
│   │   │   ├── CreateOrderCommand.java
│   │   │   └── CreateOrderResult.java
│   │   ├── port/
│   │   │   ├── in/
│   │   │   │   ├── CreateOrderUseCase.java
│   │   │   │   └── GetOrderUseCase.java
│   │   │   └── out/
│   │   │       ├── LoadOrderPort.java
│   │   │       ├── SaveOrderPort.java
│   │   │       └── SendOrderEventPort.java
│   │   └── service/
│   │       ├── CreateOrderService.java
│   │       └── GetOrderService.java
│   └── user/                       # User Aggregate
│       ├── dto/
│       ├── port/
│       └── service/
│
├── adapter/
│   ├── in/web/
│   │   ├── order/                  # Order Aggregate
│   │   │   ├── controller/
│   │   │   │   └── OrderController.java
│   │   │   ├── request/
│   │   │   │   └── CreateOrderRequest.java
│   │   │   └── response/
│   │   │       └── CreateOrderResponse.java
│   │   ├── user/                   # User Aggregate
│   │   └── common/                 # 공통 컴포넌트
│   │       └── GlobalExceptionHandler.java
│   └── out/
│       ├── persistence/
│       │   ├── order/              # Order Aggregate
│       │   │   ├── entity/
│       │   │   │   └── OrderEntity.java
│       │   │   ├── repository/
│       │   │   │   └── OrderJpaRepository.java
│       │   │   ├── mapper/
│       │   │   │   └── OrderEntityMapper.java
│       │   │   ├── OrderPersistenceAdapter.java
│       │   │   └── OrderQueryRepository.java
│       │   └── user/               # User Aggregate
│       ├── aws/
│       │   ├── s3/
│       │   │   ├── S3FileStorageAdapter.java
│       │   │   └── S3Properties.java
│       │   └── sqs/
│       │       └── SqsEventPublisher.java
│       └── external/
│           └── payment/
│               └── PaymentGatewayAdapter.java
│
└── bootstrap/
    └── config/
        ├── Application.java
        ├── JpaConfig.java
        ├── SecurityConfig.java
        └── AwsConfig.java
```

**패키지 구조 원칙:**
1. **모든 레이어 일관성**: Domain, Application, Adapter 모두 동일한 Aggregate 기준 적용
2. **비즈니스 경계 명확화**: Aggregate 단위로 코드가 그룹화되어 도메인 경계 가시화
3. **확장성**: 새 Aggregate 추가 시 명확한 위치 파악 가능
4. **응집도**: 관련 코드가 한 Aggregate 디렉토리 내에 모임
5. **공통 컴포넌트**: `common/` 디렉토리에 별도 배치

### 2. 명명 규칙

| 유형 | 패턴 | 예제 |
|------|------|------|
| Domain Entity | `{명사}` | `Order`, `User`, `Product` |
| Value Object | `{명사}` | `OrderId`, `Money`, `Email` |
| UseCase Interface | `{동사}{명사}UseCase` | `CreateOrderUseCase`, `GetUserUseCase` |
| UseCase Impl | `{동사}{명사}Service` | `CreateOrderService`, `GetUserService` |
| Input Port | `{동사}{명사}UseCase` | `CreateOrderUseCase` |
| Output Port | `{동사}{명사}Port` | `LoadOrderPort`, `SaveOrderPort` |
| Adapter | `{시스템}{기능}Adapter` | `OrderPersistenceAdapter`, `S3FileStorageAdapter` |
| Controller | `{리소스}Controller` | `OrderController`, `UserController` |
| JPA Entity | `{명사}Entity` | `OrderEntity`, `UserEntity` |
| DTO | `{동작}{리소스}Request/Response` | `CreateOrderRequest`, `GetOrderResponse` |

### 3. Annotation 규칙

| 레이어 | 허용 Annotations | 금지 Annotations |
|--------|------------------|------------------|
| **Domain** | 없음 (순수 Java) | `@Component`, `@Service`, `@Entity`, `@Data` 등 모든 프레임워크 애노테이션 |
| **Application** | `@UseCase`, `@Transactional` | `@Component`, `@Service`, `@Repository` |
| **Adapter** | `@Component`, `@RestController`, `@Repository` | `@Transactional` (Application에서만) |
| **DTO** | `@NotNull`, `@Valid`, `@Email` (Bean Validation만) | `@Data`, `@Builder` (Lombok 금지) |

### 4. 의존성 주입

#### ❌ Bad
```java
// ❌ Field Injection 금지
@Service
public class OrderService {
    @Autowired
    private OrderRepository repository;
}

// ❌ Setter Injection 금지
@Service
public class OrderService {
    private OrderRepository repository;

    @Autowired
    public void setRepository(OrderRepository repository) {
        this.repository = repository;
    }
}
```

#### ✅ Good
```java
// ✅ Constructor Injection만 사용
@UseCase
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final LoadUserPort loadUserPort;
    private final SaveOrderPort saveOrderPort;

    // ✅ 단일 생성자는 @Autowired 생략
    public CreateOrderService(
        LoadUserPort loadUserPort,
        SaveOrderPort saveOrderPort
    ) {
        this.loadUserPort = loadUserPort;
        this.saveOrderPort = saveOrderPort;
    }
}
```

### 5. Lombok 정책

#### ❌ 전 프로젝트 완전 금지
```java
// ❌ 모든 Lombok 애노테이션 금지
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
```

#### ✅ 수동 작성
```java
// ✅ Getter 수동 작성
public class Order {
    private final OrderId id;

    public OrderId getId() {
        return id;
    }
}

// ✅ Builder 대신 정적 팩토리
public static Order create(OrderId id, List<OrderItem> items) {
    return new Order(id, OrderStatus.PENDING, items);
}
```

### 6. String Case Conversion

**문제**: `String.toLowerCase()` 및 `toUpperCase()`는 기본 Locale에 의존하여 예상치 못한 결과를 초래할 수 있습니다.

**대표적 예시 - Turkish Locale 문제**:
```java
// Turkish Locale 환경에서 실행 시
String input = "IMAGE";
String normalized = input.toLowerCase(); // "ımage" (소문자 i가 다름!)

// 검증 실패 가능성
Set<String> allowed = Set.of("image", "jpeg", "png");
allowed.contains(normalized); // false! 보안 우회 위험
```

#### ❌ Bad
```java
// ❌ Locale 미지정 (기본 Locale 사용)
String format = "IMAGE";
String normalized = format.toLowerCase();

// ❌ HTTP Header 처리
String contentType = header.toUpperCase();
```

#### ✅ Good
```java
import java.util.Locale;

// ✅ 내부 처리용 (비교, 검증) - Locale.ROOT 사용
String normalized = format.toLowerCase(Locale.ROOT).trim();

// ✅ 사용자 표시용 - Locale.getDefault() 사용
String displayText = text.toLowerCase(Locale.getDefault());

// ✅ 프로토콜/표준 (HTTP, SQL) - Locale.ENGLISH 사용
String httpMethod = method.toUpperCase(Locale.ENGLISH);
```

#### 사용 케이스별 가이드

| 사용 목적 | 권장 Locale | 이유 |
|-----------|-------------|------|
| 내부 처리 (비교, 검증) | `Locale.ROOT` | Locale 독립적 동작 보장 |
| 사용자 표시용 | `Locale.getDefault()` | 사용자 언어 환경 반영 |
| 프로토콜/표준 (HTTP, SQL) | `Locale.ENGLISH` | 국제 표준 준수 |

#### 실제 예시

```java
// ✅ Domain: Value Object 검증
public class ImageFormat {
    private static final Set<String> ALLOWED_FORMATS =
        Set.of("image", "jpeg", "png", "gif");

    private final String value;

    private ImageFormat(String value) {
        java.util.Objects.requireNonNull(value, "Image format value cannot be null");
        // ✅ Locale.ROOT로 일관된 검증
        String normalized = value.toLowerCase(java.util.Locale.ROOT).trim();
        if (!ALLOWED_FORMATS.contains(normalized)) {
            throw new IllegalArgumentException("Invalid format: " + value);
        }
        this.value = normalized;
    }

    public static ImageFormat of(String value) {
        return new ImageFormat(value);
    }
}

// ✅ Adapter: HTTP Header 처리
@RestController
public class FileController {
    @PostMapping("/files")
    public ResponseEntity<?> uploadFile(@RequestHeader("Content-Type") String contentType) {
        // ✅ Locale.ENGLISH로 표준 프로토콜 처리
        String normalized = contentType.toLowerCase(Locale.ENGLISH);

        if (normalized.startsWith("image/")) {
            // 이미지 처리
        }
        return ResponseEntity.ok().build();
    }
}
```

**SpotBugs 경고**: `reportLevel = LOW` 설정 시 `DM_CONVERT_CASE` 경고 발생
- **해결**: 항상 명시적으로 Locale 지정
- **참고**: [Turkish i18n Issue](https://haacked.com/archive/2012/07/05/turkish-i-problem-and-why-you-should-care.aspx/)

---

## 🔒 금지 사항 종합

### Domain Layer
- ❌ Spring Framework 의존성
- ❌ JPA/Hibernate 의존성
- ❌ Lombok
- ❌ 인프라 라이브러리
- ❌ Setter 메서드
- ❌ Public 생성자

### Application Layer
- ❌ Adapter 구체 클래스 의존
- ❌ JPA Entity 사용
- ❌ Repository 인터페이스 직접 의존
- ❌ HTTP, AWS SDK 등 인프라 라이브러리

### Adapter Layer
- ❌ `@Transactional` (Application에서만)
- ❌ 다른 Adapter 의존
- ❌ Domain 객체 외부 노출
- ❌ 비즈니스 로직

### Persistence Adapter
- ❌ JPA 연관관계 (`@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany`)
- ❌ Entity에 Setter
- ❌ Entity에 Public 생성자
- ❌ Entity에 비즈니스 로직

### Controller Adapter
- ❌ 내부 클래스
- ❌ Domain 객체 직접 반환
- ❌ Repository/Entity 직접 의존

### 전체 프로젝트
- ❌ Lombok 사용
- ❌ Field Injection
- ❌ Setter Injection
- ❌ 순환 의존성

---

## ✅ 체크리스트

### Domain Layer
- [ ] 모든 필드는 `private final`인가?
- [ ] Setter 메서드가 없는가?
- [ ] Public 생성자가 없는가?
- [ ] 정적 팩토리 메서드를 사용하는가?
- [ ] 비즈니스 로직이 Domain 객체 내부에 있는가?
- [ ] Spring, JPA, Lombok 의존성이 없는가?

### Application Layer
- [ ] `@Transactional`이 UseCase 구현체에 있는가?
- [ ] Port 인터페이스만 의존하는가?
- [ ] Domain 객체만 사용하는가?
- [ ] UseCase별 Command/Query/Result DTO를 정의했는가?

### Persistence Adapter
- [ ] JPA 연관관계 애노테이션이 없는가?
- [ ] 외래키가 Long 타입 필드인가?
- [ ] Entity에 Setter가 없는가?
- [ ] Entity에 Public 생성자가 없는가?
- [ ] Mapper 클래스를 사용하는가?
- [ ] `@Transactional`이 없는가?

### Controller Adapter
- [ ] 내부 클래스가 없는가?
- [ ] Request/Response가 record 타입인가?
- [ ] Record 생성자에 validation이 있는가?
- [ ] Domain 객체를 직접 반환하지 않는가?
- [ ] UseCase(Port)만 의존하는가?

### 공통
- [ ] Lombok을 사용하지 않는가?
- [ ] Constructor Injection을 사용하는가?
- [ ] 순환 의존성이 없는가?
- [ ] 레이어 의존성 방향을 준수하는가?

---

## 📚 참고 문서

- **[DDD_AGGREGATE_MIGRATION_GUIDE.md](DDD_AGGREGATE_MIGRATION_GUIDE.md)** - DDD Aggregate 패턴 전환 가이드
  - Domain 레이어 Aggregate 구조
  - Application/Adapter 레이어 Aggregate별 구조
  - Technical Concern vs DDD Aggregate 패턴 비교
- **[VERSION_MANAGEMENT_GUIDE.md](VERSION_MANAGEMENT_GUIDE.md)** - Gradle Version Catalog 사용법
- **[DYNAMIC_HOOKS_GUIDE.md](DYNAMIC_HOOKS_GUIDE.md)** - Claude Code 동적 훅 시스템
- **[JAVA_RECORD_GUIDE.md](JAVA_RECORD_GUIDE.md)** - Java Record 활용 가이드
- **[README.md](README.md)** - 프로젝트 전체 가이드
- **GitHub Issues**:
  - [#13: Port & Interface 설계 가이드](https://github.com/ryu-qqq/claude-spring-standards/issues/13)
  - [#12: 패키지 구조 개선](https://github.com/ryu-qqq/claude-spring-standards/issues/12)

---

**🎯 이 문서의 모든 규칙은 ArchUnit 테스트, Git 훅, Checkstyle로 자동 검증됩니다.**
