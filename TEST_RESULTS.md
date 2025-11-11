# Test Results Summary

**Date**: 2025-11-11
**Module**: adapter-out/persistence-mysql

## Overall Results
- **Total Tests**: 74
- **Passed**: 49 (66%)
- **Failed**: 25 (34%)
- **Duration**: 0.935s

---

## ✅ Passing Tests (49)

### UserAgent Tests (100% pass rate)
- ✅ UserAgentQueryAdapterTest: 7/7
- ✅ UserAgentJpaRepositoryTest: 5/5
- ✅ UserAgentTokenRotationIntegrationTest - IntegratedScenario: 1/1
- ✅ UserAgentTokenRotationIntegrationTest - TokenIssuanceFlow: 2/2

### Schedule Tests (partial pass)
- ✅ ScheduleQueryAdapterTest: 7/7
- ✅ ScheduleMapperTest: 12/12
- ✅ ScheduleOutboxMapperTest: 13/14 (1 실패)

---

## ❌ Failing Tests (25)

### 1. ScheduleOutboxQueryAdapterTest - 14 failures
**Root Cause**: Spring context initialization or Bean wiring issues

- ❌ existsByIdemKey (2 tests)
- ❌ findByIdemKey (2 tests)
- ❌ findByOpId (2 tests)
- ❌ findByOperationStateFailed (2 tests)
- ❌ findByWalStateCompleted (1 test)
- ❌ findByWalStatePending (2 tests)
- ❌ findLatestBySellerId (2 tests)
- ❌ QueryDSL projections (1 test)

### 2. ScheduleOutboxMapperTest - 1 failure
- ❌ bidirectional_conversion test

### 3. SellerPersistenceIntegrationTest - 7 failures
**Root Cause**: Likely similar to Schedule tests (Spring context issues)

- ❌ IntegratedScenario (1 test)
- ❌ SellerCreationFlow (2 tests)
- ❌ SellerQueryFlow (2 tests)
- ❌ SellerStatusChangeFlow (2 tests)

### 4. UserAgentTokenRotationIntegrationTest - 3 failures
**Root Cause**: UserAgentFixture creates objects with duplicate ID (DEFAULT_ID = 1L)

- ❌ RateLimitRecoveryFlow (1 test)
- ❌ RotationMechanism (2 tests)
  - it_selects_user_agent_with_most_remaining_requests
  - it_selects_user_agent_after_consuming_requests

**Problem**: `createCanMakeRequest()` always uses `UserAgentId.of(1L)`, causing JPA OptimisticLockingFailureException when saving multiple objects.

**Solution**: Modify `UserAgentFixture.createCanMakeRequest()` to use `UserAgent.forNew()` instead of `UserAgent.reconstitute()` with fixed ID.

---

## 📝 Notes

- UserAgent tests: Most tests passing after fixing audit field preservation in Mapper
- Schedule/Seller tests: Need investigation into Spring context and Bean configuration
- UserAgent rotation tests: Known issue with test fixture - requires ID handling fix

---

## Next Steps

See TODO.md for action items.
