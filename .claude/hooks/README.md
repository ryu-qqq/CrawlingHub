# 🤖 Claude Code Dynamic Hooks

**최적화된 동적 훅 시스템 - 토큰 사용량 30-50% 감소, 응답 속도 개선**

Claude가 코드를 생성/수정할 때 자동으로 실행되는 동적 훅 시스템입니다.

> ⚡ **2025년 1월 최적화 완료**: Hook 인라인 텍스트 94% 감소, 요약 문서 시스템 도입

> ⚠️ **중요**: 이것은 **Claude Code 동적 훅**입니다. **Git Hooks**와는 다릅니다.
> - **Claude Hooks** (`.claude/hooks/scripts/`): Claude가 코드 생성/수정 시 실행 (이 문서)
> - **Git Hooks** (`hooks/`): `git commit` 실행 시 검증 ([문서](../../hooks/README.md))

---

## 📋 목차

- [개요](#개요)
- [최적화 시스템](#최적화-시스템)
- [훅 파일 설명](#훅-파일-설명)
- [실행 흐름](#실행-흐름)
- [모듈별 가이드라인](#모듈별-가이드라인)
- [검증 규칙](#검증-규칙)
- [커스터마이징](#커스터마이징)

---

## 🎯 개요

### 목적
Claude가 **코드를 생성하는 시점**에 헥사고날 아키텍처 규칙을 주입하여, 처음부터 올바른 코드를 생성하도록 유도합니다.

### 동작 방식 (최적화됨)
```
세션 시작 → init-session.sh → user-prompt-submit.sh → Claude 코드 생성 → after-tool-use.sh
    ↓            ↓ 요약본 로딩       ↓ 핵심 규칙 주입           ↓                ↓ 실시간 검증
 Claude 실행   컨텍스트 구성    문서 참조 유도           코드 생성 완료        위반 감지 시 경고
```

### 주요 기능
- ✅ **세션 최적화**: 요약본 우선 로딩으로 빠른 컨텍스트 구성
- ✅ **사전 예방**: 요청 단계에서 핵심 규칙 + 문서 참조 주입
- ✅ **실시간 검증**: 코드 생성 직후 즉시 검증
- ✅ **모듈별 컨텍스트**: Domain, Application, Adapter 별 맞춤 가이드
- ✅ **경고 시스템**: 위반 발견 시 사용자에게 즉시 알림
- ✅ **컨텍스트 보존**: 압박 시에도 핵심 규칙 유지

---

## ⚡ 최적화 시스템

### 성능 개선 결과

| 항목 | Before | After | 개선율 |
|------|--------|-------|--------|
| `init-session.sh` | N/A (없음) | 30줄 요약본 참조 | **신규 추가** |
| `preserve-rules.sh` | N/A (없음) | 핵심 규칙 보존 | **신규 추가** |
| `user-prompt-submit.sh` (Domain) | ~80줄 | ~15줄 | **81% 감소** |
| `user-prompt-submit.sh` (Application) | ~70줄 | ~15줄 | **79% 감소** |
| `user-prompt-submit.sh` (Adapter) | ~60줄 | ~15줄 | **75% 감소** |
| 전체 토큰 사용량 | 기준 | 30-50% 감소 | **대폭 절감** |

### 최적화 전략

**문제점 (Before)**:
```
Hook이 매번 전체 가이드라인을 인라인으로 주입
  ↓
수백 줄의 텍스트가 매 요청마다 컨텍스트 차지
  ↓
토큰 과다 사용, 응답 지연, 유지보수 어려움
```

**해결책 (After)**:
```
요약 문서 시스템 도입
  ↓
핵심 규칙 134-186줄로 압축 (SUMMARY.md)
  ↓
Hook은 간결한 리마인더 + 문서 링크만 제공
  ↓
토큰 30-50% 절감, 유지보수성 향상
```

### 문서 계층 구조

```
📚 요약본 (Quick Reference) - Hook이 참조
├── docs/CODING_STANDARDS_SUMMARY.md (134줄)
└── docs/ENTERPRISE_SPRING_STANDARDS_SUMMARY.md (186줄)
     ↓ 상세 내용 필요 시
📖 전체 문서 (Complete Reference) - Claude가 필요 시 참조
├── docs/CODING_STANDARDS.md (2,676줄)
├── docs/ENTERPRISE_SPRING_STANDARDS_PROMPT.md (3,361줄)
└── 특화 가이드
    ├── DDD_AGGREGATE_MIGRATION_GUIDE.md
    ├── DTO_PATTERNS_GUIDE.md
    ├── EXCEPTION_HANDLING_GUIDE.md
    └── JAVA_RECORD_GUIDE.md
```

### 효과

**정량적**:
- ⚡ 토큰 사용량: 30-50% 감소
- 🚀 응답 속도: 컨텍스트 로딩 시간 단축
- 📉 Hook 텍스트: 94% 감소 (500줄 → 30줄)

**정성적**:
- 🔧 유지보수성: 규칙 변경 시 문서만 수정 (단일 진실 공급원)
- 📈 확장성: 새 가이드라인 추가 용이
- 🎯 일관성: 모든 Hook이 동일한 문서 참조

---

## 📦 훅 파일 설명

### 1. `init-session.sh` ✨ NEW (최적화 추가)

**실행 시점**: Claude Code 세션 시작 시 (SessionStart Hook)

**역할**: 프로젝트 컨텍스트를 경량화하여 로딩하고 세션 정보 생성

**처리 과정**:
```bash
1. Git 브랜치 정보 파싱
   현재 브랜치: feature/USER-123-order-management

2. Jira 태스크 자동 파싱
   Jira Task: USER-123 (브랜치명에서 추출)

3. 요약본 문서 경로 설정
   RULES_DOC="docs/ENTERPRISE_SPRING_STANDARDS_SUMMARY.md" (186줄)
   전체 문서: docs/ENTERPRISE_SPRING_STANDARDS_PROMPT.md (3,361줄)

4. 세션 컨텍스트 생성
   /tmp/claude-session-context.md
   - 현재 작업 정보 (브랜치, Jira)
   - 핵심 규칙 요약
   - 문서 참조 경로 (요약본 → 전체)
```

**최적화**:
- Before: 전체 문서 참조 (3,361줄) 또는 하드코딩된 긴 가이드라인
- After: 요약본 우선 (186줄) + 전체 문서 경로 안내
- 효과: 세션 로딩 속도 대폭 개선, 토큰 절감

**출력 예시**:
```
✅ 세션 초기화 완료
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📋 브랜치: feature/USER-123-order-management
🎫 Jira Task: USER-123
📄 세션 컨텍스트: /tmp/claude-session-context.md
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

### 2. `preserve-rules.sh` ✨ NEW (최적화 추가)

**실행 시점**: Claude가 컨텍스트 압박 시 (PreCompact Hook)

**역할**: 컨텍스트 압축 전에 핵심 규칙을 보존하여 압축 후에도 규칙 유지

**처리 과정**:
```bash
1. 핵심 아키텍처 규칙 출력
   - Hexagonal Architecture 계층별 규칙
   - Domain/Application/Adapter 금지 사항

2. Zero-Tolerance 규칙 강조
   - Lombok 금지
   - Javadoc 필수
   - Scope 준수
   - 트랜잭션 경계

3. 문서 계층 구조 명시
   요약본 (빠른 참조용)
   ├── CODING_STANDARDS_SUMMARY.md
   └── ENTERPRISE_SPRING_STANDARDS_SUMMARY.md
        ↓
   전체 문서 (상세 참조용)
   ├── CODING_STANDARDS.md
   └── ENTERPRISE_SPRING_STANDARDS_PROMPT.md
```

**효과**:
- 컨텍스트 압축 후에도 핵심 규칙 유지
- Claude가 장시간 작업 시에도 규칙 준수
- 문서 참조 경로 항상 유지

---

### 3. `user-prompt-submit.sh` (최적화됨)

**실행 시점**: 사용자가 Claude에게 요청을 제출할 때 (코드 생성 **전**)

**역할**: 요청을 분석하고 해당 모듈의 핵심 규칙 + 문서 참조를 프롬프트에 주입

**최적화**:
- Before: 모듈별 60-80줄의 상세 가이드라인 인라인 주입
- After: 모듈별 15줄 핵심 규칙 + docs 링크
- 효과: 토큰 사용량 75-81% 감소

**처리 과정**:
```bash
1. 사용자 요청 분석
   "Order 클래스를 domain에 만들어줘"

2. 모듈 컨텍스트 감지
   키워드: "domain" → MODULE_CONTEXT="domain"

3. 해당 모듈 가이드라인 주입
   Domain 규칙 프롬프트 추가

4. Claude에게 전달
   원래 요청 + 주입된 가이드라인
```

**모듈 감지 키워드**:
```bash
# Domain 모듈
"domain", "도메인", "비즈니스 로직"

# Application 모듈
"usecase", "application", "서비스", "유즈케이스"

# Persistence Adapter
"repository", "jpa", "database", "persistence", "entity"

# Controller Adapter
"controller", "rest", "api", "request", "response", "dto"
```

**주입되는 가이드라인 예시 (최적화됨)**:

**Domain 모듈**:
```
# 🏛️ DOMAIN MODULE - 핵심 규칙

## ❌ 절대 금지
- Spring Framework (org.springframework.*)
- JPA/Hibernate (jakarta.persistence.*, org.hibernate.*)
- Lombok, Jackson 애노테이션
- 인프라 의존성

## ✅ 허용
- Pure Java (java.*, javax.validation.*)
- Apache Commons Lang3
- 비즈니스 로직만

## 📚 상세 가이드
- **아키텍처**: docs/ENTERPRISE_SPRING_STANDARDS_SUMMARY.md (Domain Layer)
- **DDD 패턴**: docs/DDD_AGGREGATE_MIGRATION_GUIDE.md
- **Value Object**: docs/JAVA_RECORD_GUIDE.md (Record 권장)
- **예외 처리**: docs/EXCEPTION_HANDLING_GUIDE.md

## 🎯 테스트: 90%+ 커버리지
```

**Before (80줄)**과 비교하여 **After (15줄)**로 **81% 감소**

---

### 4. `after-tool-use.sh`

**실행 시점**: Claude가 코드를 생성/수정한 직후 (코드 생성 **후**)

**역할**: 생성된 코드를 즉시 검증하고 규칙 위반 시 경고

**처리 과정**:
```bash
1. 도구 사용 감지
   Read, Write, Edit 등 파일 작업 도구 사용 확인

2. 대상 파일 경로 분석
   domain/Order.java → Domain 모듈

3. 모듈별 검증 함수 실행
   validate_domain_layer()

4. 위반 발견 시 경고 출력
   ❌ VIOLATION: domain/Order.java contains Lombok import
```

**검증 레이어별 함수**:
- `validate_domain_layer()` - Domain 순수성 검증
- `validate_application_layer()` - Application 의존성 검증
- `validate_persistence_layer()` - Persistence 규칙 검증
- `validate_controller_layer()` - Controller DTO 규칙 검증

---

## 🔄 실행 흐름

### 전체 흐름도

```
┌──────────────────────────────────────────────────────────┐
│           사용자: "Order 엔티티 만들어줘"                   │
└──────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────┐
│          user-prompt-submit.sh 실행                       │
│  1. 요청 분석: "entity" 키워드 발견                         │
│  2. 컨텍스트: MODULE_CONTEXT="persistence"                │
│  3. Persistence Adapter 가이드라인 주입:                    │
│     - NO JPA relationships                               │
│     - Use Long foreign keys                              │
│     - NO setters, NO public constructors                 │
│     - Static factory methods required                    │
└──────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────┐
│              Claude가 코드 생성                             │
│  주입된 가이드라인을 참고하여:                                │
│  - Long userId 필드 사용 (NOT @ManyToOne)                 │
│  - protected 생성자 + static create()                     │
│  - NO setter 메서드                                       │
└──────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────┐
│           after-tool-use.sh 실행                          │
│  1. Write 도구 사용 감지                                   │
│  2. 파일 경로: adapter-out-persistence-jpa/OrderEntity.java│
│  3. validate_persistence_layer() 실행:                    │
│     ✅ NO JPA relationships                               │
│     ✅ NO setters                                         │
│     ✅ NO public constructor                              │
│     ✅ Static factory method exists                       │
│  4. 검증 통과 → 완료                                       │
└──────────────────────────────────────────────────────────┘
```

### 위반 발견 시 흐름

```
Claude 코드 생성
    ↓
after-tool-use.sh 검증
    ↓
위반 발견!
    ↓
┌─────────────────────────────────────────┐
│ ❌ PERSISTENCE VIOLATION:               │
│ OrderEntity.java contains @ManyToOne    │
│                                         │
│ ⚠️  POLICY:                             │
│ - NO JPA relationships                  │
│ - Use Long foreign keys instead         │
│ - See: CODING_STANDARDS.md              │
└─────────────────────────────────────────┘
    ↓
사용자에게 경고 표시 (코드는 생성됨)
    ↓
사용자가 수정하거나 Claude에게 재생성 요청
```

---

## 📚 모듈별 가이드라인

### Domain 모듈

**주입되는 규칙**:
```markdown
## ❌ ABSOLUTELY FORBIDDEN
- NO Spring Framework (org.springframework.*)
- NO JPA/Hibernate (jakarta.persistence.*)
- NO Lombok (@Data, @Builder, etc.)
- NO infrastructure concerns

## ✅ REQUIRED
- Pure Java only
- Immutable objects (private final fields, NO setters)
- Static factory methods (create, of, from, reconstitute)
- Business logic in domain objects

## 📝 PATTERN
public class Order {
    private final OrderId id;

    private Order(OrderId id) { ... }

    public static Order create(OrderId id) { ... }

    public Order confirm() {  // Returns new instance
        return new Order(this.id, OrderStatus.CONFIRMED);
    }
}
```

**검증 항목**:
- ❌ Spring/JPA/Lombok import 감지
- ❌ `@Component`, `@Entity` 등 어노테이션
- ⚠️ Jackson 직렬화 어노테이션

---

### Application 모듈

**주입되는 규칙**:
```markdown
## ❌ FORBIDDEN
- NO Adapter 직접 참조 (adapter.* import)
- NO Lombok

## ✅ REQUIRED
- Port 인터페이스 정의 (port.in.*, port.out.*)
- UseCase 인터페이스 구현
- @Transactional on UseCase implementations

## 📝 PATTERN
// Port definition
public interface CreateOrderUseCase {
    Order execute(CreateOrderCommand command);
}

// Implementation
@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final LoadOrderPort loadOrderPort;  // Outbound port
    ...
}
```

**검증 항목**:
- ❌ `import com.company.template.adapter.*` 발견
- ❌ Lombok 사용
- ⚠️ UseCase 인터페이스 미구현
- ⚠️ @Transactional 누락

---

### Persistence Adapter

**주입되는 규칙**:
```markdown
## ❌ STRICTLY FORBIDDEN
- NO JPA relationships (@OneToMany, @ManyToOne, @OneToOne, @ManyToMany)
- NO setter methods in entities
- NO public constructors in entities
- NO @Transactional (belongs in Application layer)

## ✅ REQUIRED
- Use Long foreign key fields (userId, orderId, NOT @ManyToOne)
- Entity: protected constructor + static factory methods
- Mapper class for Entity ↔ Domain conversion

## 📝 PATTERN
@Entity
public class OrderEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private Long userId;  // ✅ Long FK, NOT @ManyToOne User user

    protected OrderEntity() {}  // JPA only

    private OrderEntity(Long userId, ...) { ... }

    public static OrderEntity create(Long userId, ...) { ... }

    // ✅ Getter only, NO setters
    public Long getUserId() { return userId; }
}
```

**검증 항목**:
- ❌ JPA 관계 어노테이션 (`@OneToMany`, `@ManyToOne`)
- ❌ Setter 메서드 (`public void setXxx()`)
- ❌ Public 생성자
- ❌ `@Transactional` 어노테이션

---

### Controller Adapter

**주입되는 규칙**:
```markdown
## ❌ FORBIDDEN
- NO inner classes for Request/Response
- Request/Response must be Java records
- NO Repository/Entity dependencies

## ✅ REQUIRED
- Request/Response as separate record files
- Record compact constructor validation
- UseCase interface dependencies ONLY

## 📝 PATTERN
// Separate file: CreateOrderRequest.java
public record CreateOrderRequest(
    String orderId,
    int amount
) {
    // Compact constructor validation
    public CreateOrderRequest {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    public CreateOrderCommand toCommand() {
        return new CreateOrderCommand(orderId, amount);
    }
}

// Controller
@RestController
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;  // UseCase only

    @PostMapping("/orders")
    public OrderResponse create(@RequestBody CreateOrderRequest request) {
        Order order = createOrderUseCase.execute(request.toCommand());
        return OrderResponse.from(order);
    }
}
```

**검증 항목**:
- ❌ Controller 내부 클래스로 Request/Response 정의
- ❌ Request/Response가 class (record여야 함)
- ❌ Repository/Entity 직접 의존
- ⚠️ Request에 `toCommand()` 누락
- ⚠️ Response에 `from()` 누락

---

## 🔍 검증 규칙

### 실시간 검증 (after-tool-use.sh)

| 모듈 | 검증 항목 | 액션 |
|------|----------|------|
| Domain | Spring/JPA/Lombok import | ❌ 경고 |
| Application | Adapter import | ❌ 경고 |
| Persistence | JPA relationships | ❌ 경고 |
| Persistence | Setter methods | ❌ 경고 |
| Persistence | Public constructors | ❌ 경고 |
| Persistence | @Transactional | ❌ 경고 |
| Controller | Inner classes | ❌ 경고 |
| Controller | Non-record DTO | ❌ 경고 |
| Controller | Repository dependency | ❌ 경고 |

### 경고 vs 차단

**Dynamic Hook (이 시스템)**:
- ⚠️ **경고만 제공** (코드는 생성됨)
- 사용자가 수정하거나 Claude에게 재생성 요청 가능

**Git Hook** (`hooks/pre-commit`):
- ❌ **커밋 차단** (강제 수정 필요)
- 최종 안전망 역할

---

## 🛠️ 커스터마이징

### 새로운 모듈 컨텍스트 추가

**`user-prompt-submit.sh` 수정**:
```bash
# 새로운 모듈 감지 추가
elif echo "$USER_PROMPT" | grep -qi "batch\|스케줄러"; then
    MODULE_CONTEXT="batch"
fi

# 가이드라인 케이스 추가
case $MODULE_CONTEXT in
    batch)
        cat << 'EOF'
# 🔄 BATCH MODULE GUIDELINES
...
EOF
        ;;
esac
```

### 새로운 검증 규칙 추가

**`after-tool-use.sh` 수정**:
```bash
# 새로운 검증 함수 정의
validate_batch_layer() {
    local file="$1"

    if grep -q "@Scheduled" "$file"; then
        if ! grep -q "@Transactional" "$file"; then
            log_error "$file: @Scheduled must have @Transactional"
        fi
    fi
}

# 검증 라우팅에 추가
if [[ "$file" == *"batch"* ]]; then
    validate_batch_layer "$file"
fi
```

---

## 📚 관련 문서

### 요약본 (Hook이 참조)
- **[CODING_STANDARDS_SUMMARY.md](../../docs/CODING_STANDARDS_SUMMARY.md)** - 134줄 코딩 표준 요약
- **[ENTERPRISE_SPRING_STANDARDS_SUMMARY.md](../../docs/ENTERPRISE_SPRING_STANDARDS_SUMMARY.md)** - 186줄 아키텍처 요약

### 전체 문서 (상세 참조용)
- **[CODING_STANDARDS.md](../../docs/CODING_STANDARDS.md)** - 2,676줄, 87개 규칙
- **[ENTERPRISE_SPRING_STANDARDS_PROMPT.md](../../docs/ENTERPRISE_SPRING_STANDARDS_PROMPT.md)** - 3,361줄, 96개 규칙
- **[DYNAMIC_HOOKS_GUIDE.md](../../docs/DYNAMIC_HOOKS_GUIDE.md)** - Dynamic Hook 상세 가이드

### 특화 가이드
- **[DDD_AGGREGATE_MIGRATION_GUIDE.md](../../docs/DDD_AGGREGATE_MIGRATION_GUIDE.md)** - DDD Aggregate 패턴
- **[DTO_PATTERNS_GUIDE.md](../../docs/DTO_PATTERNS_GUIDE.md)** - DTO 변환 패턴
- **[EXCEPTION_HANDLING_GUIDE.md](../../docs/EXCEPTION_HANDLING_GUIDE.md)** - 예외 처리 전략
- **[JAVA_RECORD_GUIDE.md](../../docs/JAVA_RECORD_GUIDE.md)** - Java Record 활용

### 시스템 문서
- **[Git Hooks README](../../hooks/README.md)** - Git Pre-commit Hook 문서

---

## 🎯 효과

### Before (최적화 전)
```
사용자: "Order 엔티티 만들어줘"
    ↓
user-prompt-submit.sh: 80줄 가이드라인 전체 주입
    ↓
토큰 과다 사용, 컨텍스트 압박
    ↓
Claude: 코드 생성 (느림)
    ↓
git commit 시도
    ↓
규칙 변경 시 Hook 스크립트도 수정 필요
```

### After (최적화 후)
```
세션 시작
    ↓
init-session.sh: 요약본 로딩 (186줄)
    ↓
사용자: "Order 엔티티 만들어줘"
    ↓
user-prompt-submit.sh: 핵심 규칙 (15줄) + 문서 링크
    ↓
토큰 75% 절감, 빠른 응답
    ↓
Claude: 규칙 준수 코드 생성 + 필요 시 SUMMARY 참조
    ↓
after-tool-use.sh: ✅ 검증 통과
    ↓
git commit: ✅ 통과
    ↓
규칙 변경: docs만 수정 (Hook은 변경 불필요)
```

**결과**: 
- 처음부터 올바른 코드 생성
- 토큰 30-50% 절감
- 응답 속도 개선
- 유지보수 용이

---

**🎯 목표**: Claude가 아키텍처 규칙을 이해하고 준수하는 코드를 생성하도록 지속적으로 가이드

---

## 📊 최적화 타임라인

- **2025년 1월**: 최적화 프로젝트 완료
  - ✅ 요약 문서 시스템 도입 (SUMMARY.md 생성)
  - ✅ Hook 스크립트 최적화 (인라인 텍스트 94% 감소)
  - ✅ CLAUDE.md 구조 개선 (문서 계층화)
  - ✅ init-session.sh, preserve-rules.sh 신규 추가
  - 📊 결과: 토큰 30-50% 절감, 응답 속도 개선

© 2024 Ryu-qqq. All Rights Reserved.
