# Coding Standards - Quick Reference

**Full Version**: [CODING_STANDARDS.md](./CODING_STANDARDS.md)

## 핵심 원칙

### 1. SOLID 원칙
- **SRP**: 한 클래스는 하나의 책임만
- **OCP**: 확장에 열려있고 수정에 닫혀있어야 함
- **LSP**: 자식 클래스는 부모 클래스를 대체 가능해야 함
- **ISP**: 사용하지 않는 인터페이스에 의존하지 말 것
- **DIP**: 구체가 아닌 추상에 의존

### 2. Law of Demeter (데미터의 법칙)
```java
// ❌ Getter 체이닝 금지
order.getCustomer().getAddress().getZipCode()

// ✅ Tell, Don't Ask
order.getCustomerZipCode()
```

### 3. Spring Proxy Limitations
**프록시가 작동하지 않는 경우:**
- Private 메서드에 `@Transactional`
- Final 클래스/메서드
- 같은 클래스 내부 메서드 호출 (`this.method()`)

```java
// ❌ 내부 호출 - 프록시 우회
@Transactional
public void processOrder() {
    this.saveOrder(); // @Transactional 무시됨!
}

// ✅ 별도 빈으로 분리
@Service
public class OrderService {
    private final OrderPersistenceService persistenceService;
    
    public void processOrder() {
        persistenceService.saveOrder(); // 프록시 정상 작동
    }
}
```

### 4. Transaction Boundaries
**규칙:**
- `@Transactional` 메서드 내에서 외부 API 호출 금지
- 트랜잭션은 짧게 유지
- 외부 호출은 트랜잭션 밖에서

```java
// ❌ 트랜잭션 내 외부 API 호출
@Transactional
public void processOrder(OrderCommand cmd) {
    orderRepository.save(order);
    s3Client.uploadFile(); // ❌ 외부 API
}

// ✅ 트랜잭션 분리
public void processOrder(OrderCommand cmd) {
    orderService.saveOrder(cmd); // @Transactional
    s3Port.uploadFile(); // 트랜잭션 밖
}
```

### 5. 헥사고날 아키텍처 레이어
```
domain/          → 비즈니스 로직 (Framework 독립)
├─ model/       → Entity, Aggregate, Value Object
├─ port/        → 인터페이스 정의
│  ├─ in/      → UseCase (Inbound)
│  └─ out/     → Repository, External API (Outbound)
└─ service/     → Domain Service

application/     → 어댑터 구현
├─ in/web/      → Controller
├─ out/         → Repository 구현, External API 구현
└─ config/      → 빈 설정
```

### 6. 네이밍 컨벤션
- **Port**: `~Port` (인터페이스)
- **Adapter**: `~Adapter` (구현체)
- **UseCase**: `~UseCase` (Inbound Port)
- **Command/Query**: CQRS 패턴
- **DTO**: `~Request`, `~Response`, `~Command`, `~Query`

### 7. 예외 처리
```java
// ✅ 도메인 예외 정의
public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException(Long orderId) {
        super(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId);
    }
}

// ✅ GlobalExceptionHandler로 변환
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus())
            .body(ErrorResponse.of(e));
    }
}
```

### 8. 테스트 작성
- **단위 테스트**: Domain Layer (비즈니스 로직)
- **통합 테스트**: Application Layer (API, DB 연동)
- **ArchUnit 테스트**: 아키텍처 규칙 검증

### 9. 금지 패턴
❌ Entity에서 다른 Entity 직접 참조
❌ Controller에서 Repository 직접 호출
❌ Domain Layer에 Framework 의존성
❌ `@Transactional` 내에서 외부 API 호출
❌ Private/Final 메서드에 `@Transactional`
❌ Getter 체이닝 (Law of Demeter 위반)

### 10. 권장 패턴
✅ DDD Aggregate 단위 트랜잭션
✅ Port-Adapter 패턴
✅ CQRS (Command-Query 분리)
✅ DTO 변환 레이어
✅ 도메인 이벤트
✅ 불변 객체 (Record, Value Object)

---

**상세 내용은 [CODING_STANDARDS.md](./CODING_STANDARDS.md)를 참조하세요.**
