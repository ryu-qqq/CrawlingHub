# A/B 테스트 시나리오

## 시나리오 1: Domain Aggregate 생성

**프롬프트**:
```
Order Aggregate를 생성해줘.
- 주문 생성 (placeOrder)
- 주문 취소 (cancelOrder)
- 고객 정보, 배송 주소 포함
```

**체크리스트**:
- [ ] Lombok 미사용 (`@Data`, `@Getter`, `@Setter` 없음)
- [ ] Getter 체이닝 미사용 (`order.getCustomer().getAddress()` 없음)
- [ ] Tell, Don't Ask 패턴 준수
- [ ] 비즈니스 로직은 Domain에 위치
- [ ] Javadoc 포함 (`@author`, `@since`)

---

## 시나리오 2: UseCase 생성

**프롬프트**:
```
PlaceOrderUseCase를 생성해줘.
- 외부 결제 API 호출 필요
- 주문 생성 후 이메일 발송
```

**체크리스트**:
- [ ] `@Transactional` 내 외부 API 호출 없음
- [ ] Private 메서드에 `@Transactional` 없음
- [ ] 트랜잭션 경계가 명확히 분리됨
- [ ] UseCase는 얇게 유지 (비즈니스 로직은 Domain에)
- [ ] Command/Query 분리

---

## 시나리오 3: JPA Entity 생성

**프롬프트**:
```
OrderEntity를 생성해줘.
- Order와 Customer 관계 표현
- Audit 정보 포함
```

**체크리스트**:
- [ ] `@ManyToOne`, `@OneToMany` 미사용 (Long FK 전략)
- [ ] Setter 메서드 없음
- [ ] Public constructor 없음 (protected + static factory)
- [ ] BaseAuditEntity 상속
- [ ] Lombok 미사용

---

## 측정 기준

### 정량적 지표
1. **컨벤션 위반 건수**: 체크리스트에서 ❌ 개수
2. **토큰 사용량**: 프롬프트 제출 시 예상 토큰 (로그에서 확인)
3. **생성 시간**: 요청 → 응답 완료까지 시간
4. **코드 줄 수**: 생성된 코드의 총 라인 수

### 정성적 지표
1. **코드 품질**: Zero-Tolerance 규칙 준수 여부
2. **가독성**: 명확한 네이밍, 구조
3. **완성도**: 즉시 사용 가능한 수준인지

---

## 실행 방법

### Test A: Hook ON (현재 상태)
```bash
# settings.local.json 활성화 (현재 상태 유지)
# Claude Code에서 각 시나리오 실행
# 결과를 test-results-hook-on.md에 기록
```

### Test B: Hook OFF
```bash
# 1. Hook 비활성화
mv .claude/settings.local.json .claude/settings.local.json.backup

# 2. 새 Claude Code 세션 시작
# 3. 동일 시나리오 실행
# 4. 결과를 test-results-hook-off.md에 기록

# 5. Hook 복구
mv .claude/settings.local.json.backup .claude/settings.local.json
```

---

## 결과 분석

### 예상 결과 (Hook ON vs Hook OFF)

| 지표 | Hook ON | Hook OFF | 개선율 |
|------|---------|----------|--------|
| 컨벤션 위반 | 0-2회 | 8-12회 | 78-100% |
| 토큰 사용 | 500-1,000 | 50,000 | 90% 절감 |
| Zero-Tolerance 준수 | 95-100% | 50-70% | 40-50%p |

### 성공 기준
- **컨벤션 위반 감소**: 50% 이상
- **토큰 효율**: 80% 이상 절감
- **Zero-Tolerance 준수**: 90% 이상

