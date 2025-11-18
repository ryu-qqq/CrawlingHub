# SELLER-001: Seller Domain Layer TDD Plan

**Issue**: SELLER-001-domain
**Layer**: Domain Layer
**Goal**: Seller Aggregate 새로운 요구사항 반영

---

## TDD Cycles (Kent Beck + Tidy First)

### Cycle 1: CrawlingInterval 제거 (Structural)
- [ ] 🟨 **struct**: Seller에서 CrawlingInterval 필드 제거
  - crawlingInterval 필드 삭제
  - updateInterval() 메서드 삭제
  - getCrawlingIntervalDays() 메서드 삭제
  - 생성자에서 crawlingInterval 파라미터 제거
  - ✅ 기존 테스트 모두 통과 유지

### Cycle 2: 초기 상태 INACTIVE 변경 (Red → Green → Refactor)
- [ ] 🔴 **test**: Seller 생성 시 INACTIVE 상태 테스트
  - `셀러_생성_시_INACTIVE_상태()` 테스트 추가
  - 실패 확인 (현재는 ACTIVE)
- [ ] 🟢 **feat**: forNew() 메서드에서 초기 상태를 INACTIVE로 변경
  - `this.status = SellerStatus.INACTIVE;`
  - 테스트 통과 확인
- [ ] ♻️ **struct** (if needed): 코드 구조 개선

### Cycle 3: updateName() 메서드 추가 (Red → Green → Refactor)
- [ ] 🔴 **test**: 이름 변경 테스트
  - `이름_변경_성공()` 테스트 추가
  - `이름_null_시_예외()` 테스트 추가
  - `이름_빈값_시_예외()` 테스트 추가
  - `이름_100자_초과_시_예외()` 테스트 추가
  - 실패 확인 (메서드 없음)
- [ ] 🟢 **feat**: updateName(String newName) 메서드 구현
  - 이름 검증 로직 (null, blank, 길이 체크)
  - updatedAt 갱신
  - 테스트 통과 확인
- [ ] ♻️ **struct** (if needed): 검증 로직 메서드 추출

### Cycle 4: activate() 메서드 개선 (Red → Green → Refactor)
- [ ] 🔴 **test**: 이미 활성화된 셀러 활성화 시도 예외 테스트
  - `이미_활성화된_셀러_활성화_시도_시_예외()` 테스트 추가
  - 실패 확인 (현재는 예외 없음)
- [ ] 🟢 **feat**: activate() 메서드에 상태 검증 추가
  - ACTIVE 상태면 SellerInvalidStateException 발생
  - 테스트 통과 확인
- [ ] ♻️ **struct** (if needed): 상태 검증 로직 정리

### Cycle 5: deactivate() 메서드 개선 (Red → Green → Refactor)
- [ ] 🔴 **test**: 이미 비활성화된 셀러 비활성화 시도 예외 테스트
  - `이미_비활성화된_셀러_비활성화_시도_시_예외()` 테스트 추가
  - 실패 확인 (현재는 예외 없음)
- [ ] 🟢 **feat**: deactivate() 메서드에 상태 검증 추가
  - INACTIVE 상태면 SellerInvalidStateException 발생
  - 테스트 통과 확인
- [ ] ♻️ **struct** (if needed): 상태 검증 로직 정리

### Cycle 6: SellerInvalidStateException 추가 (Red → Green)
- [ ] 🔴 **test**: SellerInvalidStateException 테스트
  - activate/deactivate 테스트에서 예외 타입 검증
  - 실패 확인 (예외 클래스 없음)
- [ ] 🟢 **feat**: SellerInvalidStateException 클래스 생성
  - domain/seller/exception/ 패키지에 생성
  - 테스트 통과 확인

### Cycle 7: TestFixture 업데이트 (Structural)
- [ ] 🟨 **struct**: SellerFixture 변경사항 반영
  - createInactive() 메서드 (CrawlingInterval 제거)
  - createActive() 메서드 (CrawlingInterval 제거)
  - ✅ 기존 테스트 모두 통과 유지

---

## 완료 조건

- ✅ CrawlingInterval 완전 제거
- ✅ 초기 상태 INACTIVE
- ✅ updateName() 메서드 구현
- ✅ activate/deactivate 상태 검증 추가
- ✅ SellerInvalidStateException 구현
- ✅ TestFixture 업데이트
- ✅ 모든 테스트 통과 (커버리지 > 80%)
- ✅ Zero-Tolerance 규칙 준수

---

## 참고

**Tidy First 원칙**:
- Structural Changes 먼저 (CrawlingInterval 제거)
- Behavioral Changes 나중 (새 메서드 추가)
- 절대 섞지 않기

**커밋 규칙**:
- `struct:` - 구조 개선 (동작 변경 없음)
- `test:` - 테스트 추가 (Red Phase)
- `feat:` - 구현 (Green Phase)
