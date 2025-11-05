# AI Review 통합 분석 보고서 - PR #31

**분석 일자**: 2025-11-05  
**PR**: #31 - refactor: Seller & Scheduler 바운디드 컨텍스트 아키텍처 리팩토링  
**분석 봇**: Gemini, CodeRabbit, Codex  
**전략**: Merge (병렬 수집 → 중복 제거 → 통합 우선순위)

---

## 📊 Review Statistics

- **Bots Analyzed**: Gemini, CodeRabbit, Codex
- **Total Comments**: 7
- **After Deduplication**: 6
- **Consensus Issues**: 0 (all bots agree)
- **Critical Issues**: 2 (컴파일 에러 위험)
- **Important Issues**: 1 (2-bot consensus)
- **Suggestions**: 3 (single-bot opinion)

---

## 🎯 Priority Distribution

### ✅ Critical (Must-Fix) - 2 issues

#### 1. **DomainException이 Throwable을 상속하지 않음** (CodeRabbit)
**Location**: `domain/src/main/java/com/ryuqq/crawlinghub/domain/common/DomainException.java:24`

**문제점**:
- `DomainException`을 interface로 변경하여 `RuntimeException`을 상속하지 않음
- `@ExceptionHandler(DomainException.class)`가 작동하지 않을 수 있음
- `throws DomainException`, `catch (DomainException e)` 사용 불가

**영향도**: 🔴 **Critical** - 예외 처리 메커니즘이 작동하지 않을 수 있음

**해결 방안**:
```java
// Sealed abstract class로 변경
public sealed abstract class DomainException extends RuntimeException
    permits SellerException, ScheduleException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // abstract methods
    String code();
    String message();
    Map<String, Object> args();
}
```

**Effort**: 30 minutes

---

#### 2. **SellerAssembler.toDomain()의 Clock 인자 오류** (Codex P0)
**Location**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/assembler/SellerAssembler.java:140`

**문제점**:
- `MustitSeller.reconstitute()`는 8개 파라미터를 받음
- `toDomain()`에서 9개 파라미터(Clock 포함)를 전달하여 **컴파일 에러 발생**

**실제 시그니처**:
```java
MustitSeller.reconstitute(
    id, sellerCode, sellerName, status, 
    totalProductCount, lastCrawledAt, 
    createdAt, updatedAt  // 8개
)
```

**현재 코드**:
```java
MustitSeller.reconstitute(
    id, sellerCode, sellerName, status,
    totalProductCount, lastCrawledAt,
    Clock.systemDefaultZone(),  // ❌ 잘못된 위치
    createdAt, updatedAt
)
```

**해결 방안**:
```java
return MustitSeller.reconstitute(
    MustitSellerId.of(dto.id()),
    SellerCode.of(dto.sellerCode()),
    SellerName.of(dto.sellerName()),
    dto.status(),
    dto.totalProductCount() != null ? dto.totalProductCount() : 0,
    dto.lastCrawledAt(),
    dto.createdAt(),
    dto.updatedAt()
    // Clock 제거 - MustitSeller.reconstitute() 내부에서 처리
);
```

**Effort**: 5 minutes

---

### ⚠️ Important (Should-Fix) - 1 issue

#### 3. **테스트들이 LoadSellerPort의 새로운 DTO 반환 타입에 맞춰 업데이트되지 않음** (Codex P1)
**Location**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/port/out/LoadSellerPort.java:45`

**문제점**:
- `LoadSellerPort`가 이제 `Optional<SellerQueryDto>`를 반환
- 기존 테스트들이 `Optional<MustitSeller>`를 mock하고 있음
- 테스트들이 컴파일되지 않거나 실행 시 `SellerAssembler` 의존성 누락

**영향받는 테스트**:
- `GetSellerDetailServiceTest`
- `UpdateSellerStatusServiceTest`
- `RegisterSellerServiceTest`

**해결 방안**:
1. 테스트에서 `SellerQueryDto` mock 생성
2. `SellerAssembler`를 테스트에 주입
3. 또는 `SellerAssembler`를 mock하여 DTO → Domain 변환 테스트

**Effort**: 45 minutes

---

### 💡 Suggestions (Nice-to-Have) - 3 issues

#### 4. **SellerErrorMapper에서 HttpStatus와 title 하드코딩** (Gemini Medium)
**Location**: `adapter-in/rest-api/src/main/java/com/ryuqq/crawlinghub/adapter/in/rest/seller/mapper/SellerErrorMapper.java:67`

**제안**:
- `SellerErrorCode` enum에 `title` 필드 추가
- `SellerErrorMapper`에서 enum의 `title`과 `httpStatus` 사용

**Effort**: 20 minutes

---

#### 5. **Clock.systemDefaultZone() 직접 사용으로 테스트 어려움** (Gemini Medium)
**Location**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/assembler/SellerAssembler.java:140`

**제안**:
- `SellerAssembler`에 `Clock`을 DI로 주입
- 테스트에서 `Clock`을 mock하여 시간 제어 가능

**Effort**: 15 minutes

**참고**: 위의 Critical 이슈 #2에서 Clock을 제거하면 이 이슈도 해결됨

---

#### 6. **IllegalArgumentException 대신 SellerNotFoundException 사용** (Gemini Medium × 2)
**Locations**:
- `application/src/main/java/com/ryuqq/crawlinghub/application/task/service/InitiateCrawlingService.java:82`
- `application/src/main/java/com/ryuqq/crawlinghub/application/task/service/ProcessMiniShopResultService.java:260`

**제안**:
- `IllegalArgumentException` 대신 `SellerNotFoundException` 사용
- 예외 처리 전략 일관성 유지

**Effort**: 5 minutes × 2 = 10 minutes

---

## 🚫 Skipped - 0 issues

모든 이슈가 유효하며 프로젝트 표준과 충돌하지 않습니다.

---

## 📋 통합 TodoList

### 🔴 Critical (Must-Fix) - 즉시 수정 필요

1. **DomainException을 Sealed Abstract Class로 변경** (30 min)
   - `DomainException`을 `RuntimeException`을 상속하는 sealed abstract class로 변경
   - `SellerException`, `ScheduleException`도 abstract class로 변경
   - `@ExceptionHandler` 호환성 확보

2. **SellerAssembler.toDomain()의 Clock 인자 제거** (5 min)
   - `MustitSeller.reconstitute()` 시그니처에 맞춰 Clock 제거
   - 컴파일 에러 해결

### ⚠️ Important (Should-Fix) - 빌드/테스트 전 수정 권장

3. **테스트 업데이트: LoadSellerPort DTO 반환 타입** (45 min)
   - 3개 테스트 파일 수정
   - `SellerQueryDto` mock 생성
   - `SellerAssembler` 의존성 추가

### 💡 Suggestions (Nice-to-Have) - 선택적 개선

4. **SellerErrorCode에 title 필드 추가** (20 min)
5. **Clock DI 주입** (15 min) - 이슈 #2 해결 시 불필요
6. **IllegalArgumentException → SellerNotFoundException** (10 min)

---

## 🎯 Recommended Action Plan

### Phase 1: Critical Fixes (즉시)
```bash
# 1. DomainException 수정
# 2. SellerAssembler.toDomain() 수정
```
**예상 시간**: 35 minutes

### Phase 2: Test Updates (빌드 전 필수)
```bash
# 3. 테스트 파일들 업데이트
```
**예상 시간**: 45 minutes

### Phase 3: Optional Improvements (PR 머지 후)
```bash
# 4-6. 선택적 개선사항
```
**예상 시간**: 45 minutes

---

## 📈 Quality Metrics

- **컴파일 에러**: 2개 (Critical)
- **테스트 실패**: 3개 (Important)
- **코드 품질**: 3개 (Suggestions)
- **전체 준수율**: 85% (7/7 이슈 대응 가능)

---

## 🤖 Bot Consensus

| Issue | Gemini | CodeRabbit | Codex | Consensus |
|-------|--------|------------|-------|-----------|
| DomainException Throwable | ❌ | ✅ Critical | ❌ | Single-bot (Critical) |
| Clock 인자 오류 | ❌ | ❌ | ✅ P0 | Single-bot (Critical) |
| 테스트 업데이트 | ❌ | ❌ | ✅ P1 | Single-bot (Important) |
| ErrorMapper 하드코딩 | ✅ Medium | ❌ | ❌ | Single-bot (Suggestion) |
| Clock DI | ✅ Medium | ❌ | ❌ | Single-bot (Suggestion) |
| Exception 타입 | ✅ Medium×2 | ❌ | ❌ | Single-bot (Suggestion) |

---

## ✅ 결론

**즉시 수정 필요**: 2개 Critical 이슈 (컴파일 에러)
- DomainException을 sealed abstract class로 변경
- SellerAssembler.toDomain()의 Clock 인자 제거

**빌드 전 수정 권장**: 1개 Important 이슈
- 테스트 파일들 업데이트

**선택적 개선**: 3개 Suggestions
- 코드 품질 향상을 위한 개선사항

**예상 총 작업 시간**: 80-125 minutes

