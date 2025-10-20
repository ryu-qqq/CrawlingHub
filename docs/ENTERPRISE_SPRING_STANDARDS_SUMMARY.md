# Enterprise Spring Standards - Quick Reference

**Full Version**: [ENTERPRISE_SPRING_STANDARDS_PROMPT.md](./ENTERPRISE_SPRING_STANDARDS_PROMPT.md)

## 아키텍처 핵심

### 헥사고날 아키텍처 (Ports & Adapters)
```
외부 세계 → Adapter (in) → Port (in) → Domain Logic
Domain Logic → Port (out) → Adapter (out) → 외부 세계
```

**레이어 의존성 규칙:**
- Domain은 어떤 레이어에도 의존하지 않음
- Application은 Domain에만 의존
- 모든 의존성은 Domain을 향함 (의존성 역전)

### DDD 전술적 패턴

**Entity**
- 식별자로 구분되는 도메인 객체
- 생명주기 동안 변경 가능
- 비즈니스 로직 포함

**Value Object**
- 식별자 없이 속성으로만 구분
- 불변 객체
- Java Record 사용 권장

**Aggregate**
- Entity와 Value Object의 집합
- 트랜잭션 경계
- Aggregate Root를 통해서만 접근

**Repository**
- Aggregate 단위 저장/조회
- Port (인터페이스)는 Domain에, 구현체는 Application에

**Domain Service**
- 여러 Aggregate에 걸친 비즈니스 로직
- Stateless

**Domain Event**
- Aggregate 상태 변경 알림
- 비동기 처리 활용

### CQRS (Command-Query Separation)

**Command (쓰기)**
```java
public interface CreateOrderUseCase {
    OrderId createOrder(CreateOrderCommand command);
}
```

**Query (읽기)**
```java
public interface GetOrderQuery {
    OrderResponse getOrder(GetOrderQuery query);
}
```

**분리 이유:**
- 성능 최적화 (읽기/쓰기 분리)
- 복잡도 감소
- 확장성 향상

### 트랜잭션 전략

**원칙:**
1. Aggregate 단위 트랜잭션
2. 하나의 트랜잭션에서 하나의 Aggregate만 수정
3. 여러 Aggregate 수정 필요 시 → 도메인 이벤트 사용

```java
// ✅ 단일 Aggregate 트랜잭션
@Transactional
public void createOrder(CreateOrderCommand cmd) {
    Order order = Order.create(cmd);
    orderRepository.save(order);
    eventPublisher.publish(new OrderCreatedEvent(order.getId()));
}

// ❌ 여러 Aggregate 동시 수정
@Transactional
public void createOrder(CreateOrderCommand cmd) {
    Order order = orderRepository.save(Order.create(cmd));
    Payment payment = paymentRepository.save(Payment.create(order)); // ❌
}
```

### DTO 변환 계층

**계층별 DTO:**
- **Request/Response**: API 계층 (Controller)
- **Command/Query**: Application 계층 (UseCase)
- **Entity/Value Object**: Domain 계층

```java
// Controller → UseCase
@PostMapping("/orders")
public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
    CreateOrderCommand command = OrderMapper.toCommand(request);
    OrderId orderId = createOrderUseCase.createOrder(command);
    return ResponseEntity.ok(OrderMapper.toResponse(orderId));
}
```

### 예외 처리 계층

**Domain Exception** → **Application Handler** → **API Response**

```java
// Domain
public class InsufficientStockException extends BusinessException {
    public InsufficientStockException(ProductId productId, int requested, int available) {
        super(ErrorCode.INSUFFICIENT_STOCK, 
              String.format("Product %s: requested %d, available %d", 
                            productId, requested, available));
    }
}

// Application
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handle(InsufficientStockException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse.of(e.getErrorCode(), e.getMessage()));
    }
}
```

### 테스트 전략

**Domain Layer**
- 단위 테스트 (JUnit)
- 비즈니스 로직 검증
- Mocking 최소화

**Application Layer**
- 통합 테스트 (@SpringBootTest)
- API 테스트 (@WebMvcTest)
- DB 연동 테스트

**Architecture**
- ArchUnit 테스트
- 레이어 의존성 검증
- 네이밍 규칙 검증

### 성능 최적화

**N+1 문제 해결:**
- Fetch Join
- Entity Graph
- Query Projection

**캐싱 전략:**
- Look-Aside 패턴
- Write-Through 패턴
- 적절한 TTL 설정

**비동기 처리:**
- `@Async` (간단한 비동기)
- Domain Event + Message Queue (복잡한 비동기)

### 보안 원칙

**인증/인가:**
- Spring Security
- JWT 토큰
- Method Security (`@PreAuthorize`)

**입력 검증:**
- `@Valid` + Bean Validation
- Domain Layer에서 추가 검증

**민감 정보:**
- 환경 변수 사용
- AWS Secrets Manager
- 로그에 민감 정보 제외

---

**상세 내용은 [ENTERPRISE_SPRING_STANDARDS_PROMPT.md](./ENTERPRISE_SPRING_STANDARDS_PROMPT.md)를 참조하세요.**
