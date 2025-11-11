# TODO List - Test Failures

**Last Updated**: 2025-11-11

---

## 🔴 High Priority

### 1. UserAgent Test Fixture - ID 중복 문제 해결
**파일**: `domain/src/testFixtures/java/com/ryuqq/crawlinghub/domain/useragent/UserAgentFixture.java`

**문제**:
- `createCanMakeRequest()` 메서드가 항상 `DEFAULT_ID = 1L` 사용
- 여러 UserAgent 객체 생성 시 ID 중복으로 JPA OptimisticLockingFailureException 발생

**해결 방법**:
```java
// 현재 (잘못된 방식):
public static UserAgent createCanMakeRequest(int remainingRequests) {
    return UserAgent.reconstitute(
        UserAgentId.of(DEFAULT_ID),  // ❌ 고정 ID
        ...
    );
}

// 수정 필요:
public static UserAgent createCanMakeRequest(int remainingRequests) {
    UserAgent userAgent = UserAgent.forNew(DEFAULT_USER_AGENT);
    LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
    Token token = Token.of(DEFAULT_TOKEN, now, now.plusHours(24));
    userAgent.issueNewToken(token);
    return userAgent;
}
```

**영향받는 테스트**:
- `UserAgentTokenRotationIntegrationTest`
  - `it_selects_user_agent_with_most_remaining_requests`
  - `it_selects_user_agent_after_consuming_requests`
  - `it_recovers_rate_limit_and_resets_status` (RateLimitRecoveryFlow)

**예상 소요 시간**: 15분

---

## 🟡 Medium Priority

### 2. ScheduleOutboxQueryAdapterTest - Spring Context 문제 조사
**파일**: `adapter-out/persistence-mysql/.../schedule/adapter/ScheduleOutboxQueryAdapterTest.java`

**문제**:
- 14개 테스트 실패
- Spring context initialization 또는 Bean wiring 문제로 추정

**조사 필요**:
- `@DataJpaTest` 설정 확인
- Repository Bean 등록 상태 확인
- QueryDSL 설정 확인

**실패 테스트**:
- `existsByIdemKey` (2)
- `findByIdemKey` (2)
- `findByOpId` (2)
- `findByOperationStateFailed` (2)
- `findByWalStateCompleted` (1)
- `findByWalStatePending` (2)
- `findLatestBySellerId` (2)
- QueryDSL projections (1)

**예상 소요 시간**: 1-2시간

---

### 3. ScheduleOutboxMapperTest - 양방향 변환 테스트 실패
**파일**: `adapter-out/persistence-mysql/.../schedule/mapper/ScheduleOutboxMapperTest.java`

**문제**:
- `bidirectional_conversion` 테스트 1개 실패

**조사 필요**:
- Domain ↔ Entity 변환 로직 확인
- Audit 필드 보존 여부 확인 (UserAgent와 유사한 문제일 가능성)

**예상 소요 시간**: 30분

---

### 4. SellerPersistenceIntegrationTest - 통합 테스트 실패
**파일**: `adapter-out/persistence-mysql/.../seller/.../SellerPersistenceIntegrationTest.java`

**문제**:
- 7개 테스트 실패 (IntegratedScenario, SellerCreationFlow, SellerQueryFlow, SellerStatusChangeFlow)
- Schedule 테스트와 유사한 Spring context 문제로 추정

**조사 필요**:
- Spring context 설정
- Repository/Adapter Bean 등록 상태

**예상 소요 시간**: 1시간

---

## 📊 Progress Summary

- **Total Failures**: 25
- **High Priority**: 3 (UserAgent rotation)
- **Medium Priority**: 22 (Schedule: 15, Seller: 7)
- **Current Success Rate**: 66% (49/74)
- **Target Success Rate**: 100% (74/74)

---

## ✅ Completed

- ✅ UserAgentQueryAdapterTest: 7/7 통과
- ✅ UserAgentJpaRepositoryTest: 5/5 통과
- ✅ ScheduleQueryAdapterTest: 7/7 통과
- ✅ ScheduleMapperTest: 12/12 통과
- ✅ UserAgent Audit 필드 보존 문제 해결 (이전 세션)
- ✅ UserAgent Token 타임스탬프 정밀도 문제 해결 (이전 세션)

---

## 📝 Notes

- Main 브랜치 기준으로 리팩토링 예정
- CI/CD 실패 예상 (테스트 실패로 인해)
- Persistence Layer 테스트 안정화가 최우선 목표
