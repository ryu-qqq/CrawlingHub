# A/B 테스트 결과: Hook ON

## 테스트 환경
- 날짜: YYYY-MM-DD HH:MM:SS
- Hook 상태: ✅ 활성화
- settings.local.json: 존재함

---

## 시나리오 1: Domain Aggregate 생성

### 생성된 코드
```java
// 여기에 생성된 코드 붙여넣기
```

### 체크리스트
- [ ] Lombok 미사용
- [ ] Getter 체이닝 미사용
- [ ] Tell, Don't Ask 패턴 준수
- [ ] 비즈니스 로직은 Domain에 위치
- [ ] Javadoc 포함

### 측정 지표
- 컨벤션 위반: ___ 건
- 토큰 사용: ___ tokens
- 생성 시간: ___ 초
- 코드 줄 수: ___ 줄

---

## 시나리오 2: UseCase 생성

### 생성된 코드
```java
// 여기에 생성된 코드 붙여넣기
```

### 체크리스트
- [ ] `@Transactional` 내 외부 API 호출 없음
- [ ] Private 메서드에 `@Transactional` 없음
- [ ] 트랜잭션 경계 명확
- [ ] UseCase는 얇게 유지
- [ ] Command/Query 분리

### 측정 지표
- 컨벤션 위반: ___ 건
- 토큰 사용: ___ tokens
- 생성 시간: ___ 초
- 코드 줄 수: ___ 줄

---

## 시나리오 3: JPA Entity 생성

### 생성된 코드
```java
// 여기에 생성된 코드 붙여넣기
```

### 체크리스트
- [ ] Long FK 전략 사용
- [ ] Setter 메서드 없음
- [ ] Public constructor 없음
- [ ] BaseAuditEntity 상속
- [ ] Lombok 미사용

### 측정 지표
- 컨벤션 위반: ___ 건
- 토큰 사용: ___ tokens
- 생성 시간: ___ 초
- 코드 줄 수: ___ 줄

---

## 전체 요약

### 정량적 지표
- 총 컨벤션 위반: ___ 건
- 평균 토큰 사용: ___ tokens
- 평균 생성 시간: ___ 초

### 정성적 평가
- 코드 품질: ⭐⭐⭐⭐⭐ (5점 만점)
- 가독성: ⭐⭐⭐⭐⭐
- 완성도: ⭐⭐⭐⭐⭐

