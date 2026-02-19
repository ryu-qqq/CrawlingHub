# E2E 통합 테스트 갭 분석

> 분석 일시: 2026-02-18
> 분석 범위: 15개 도메인 (api-endpoints + api-flow 기반)
> 비교 대상: `integration-test/src/test/java/.../integration/` 기존 E2E 테스트

---

## 1. 전체 요약

| 구분 | 도메인 수 | 엔드포인트 수 | 기존 E2E 테스트 |
|------|-----------|--------------|----------------|
| E2E 테스트 없음 (신규 필요) | 8 | 27 | 0 |
| Query만 커버 (Command 미커버) | 6 | 14 Command | 0 Command |
| 완전 커버 (Read-only) | 1 | 2 Query | 6 tests |
| **총계** | **15** | **43 endpoints** | **40 tests (Query only)** |

### 커버리지 현황

```
전체 엔드포인트: 43개
├── Query: 20개 → 기존 E2E 커버: 14개 (70%)
└── Command: 23개 → 기존 E2E 커버: 0개 (0%) ⚠️
```

---

## 2. 도메인별 상세 갭 분석

### 🔴 Priority 1: E2E 테스트 완전 부재 (8개 도메인)

#### 2.1 productgroup (7 endpoints - 가장 복잡)

| ID | 엔드포인트 | HTTP | 커버 여부 | 우선순위 |
|----|-----------|------|----------|---------|
| Q1 | GET /product-groups | GET | ❌ 미커버 | HIGH |
| Q2 | GET /product-groups/{id} | GET | ❌ 미커버 | HIGH |
| C1 | POST /product-groups | POST | ❌ 미커버 | **CRITICAL** |
| C2 | POST /product-groups/batch | POST | ❌ 미커버 | HIGH |
| C3 | PUT /product-groups/{id} | PUT | ❌ 미커버 | **CRITICAL** |
| C4 | PATCH /product-groups/{id}/basic-info | PATCH | ❌ 미커버 | MEDIUM |
| C5 | PATCH /product-groups/status | PATCH | ❌ 미커버 | HIGH |

**핵심 테스트 시나리오**:
- C1→Q2: 단건 등록 후 상세 조회 → 8개 서브 Aggregate 검증 (이미지, 옵션, 설명, 고시, 상품)
- C1→C3→Q2: 등록 → 전체 수정 → 조회 (diff 기반 이미지/옵션/상품 교체 검증)
- C1→C5: 등록 → 상태 전이 검증 (DRAFT → ACTIVE, 잘못된 전이 시 에러)
- C2: 배치 등록 → 일부 실패 허용 (BatchProcessingResult 구조 검증)
- C4: 기본 정보만 수정 → 이미지/옵션 변경 없음 확인

**api-flow에서 발견된 이슈**:
- FullProductGroupRegistrationCoordinator: 8단계 등록 단일 트랜잭션
- SellerOption diff: ID 보존 전략 검증 필요
- Product soft delete + 재생성 검증 필요

---

#### 2.2 product (4 endpoints)

| ID | 엔드포인트 | HTTP | 커버 여부 | 우선순위 |
|----|-----------|------|----------|---------|
| C1 | PATCH /products/{id}/price | PATCH | ❌ 미커버 | HIGH |
| C2 | PATCH /products/{id}/stock | PATCH | ❌ 미커버 | HIGH |
| C3 | PATCH /products/status | PATCH | ❌ 미커버 | HIGH |
| C4 | PATCH /products | PATCH | ❌ 미커버 | **CRITICAL** |

**핵심 테스트 시나리오**:
- (선행: productgroup C1) → C1: 가격 수정 → Money VO 음수 검증
- (선행: productgroup C1) → C2: 재고 수정 → 음수 불가 검증
- (선행: productgroup C1) → C3: 배치 상태 변경 → 소유권(sellerId) 검증
- (선행: productgroup C1) → C4: 상품 일괄 수정 (가격+재고+옵션 diff) → 가장 복잡

**api-flow에서 발견된 이슈**:
- C4: dual domain diff (SellerOption + Product) - 옵션 값 변경 시 ID 보존 vs 삭제+재생성
- sellerId 기반 소유권 검증: MarketAccessChecker → DB 쿼리 조건

---

#### 2.3 shipment (6 endpoints)

| ID | 엔드포인트 | HTTP | 커버 여부 | 우선순위 |
|----|-----------|------|----------|---------|
| Q1 | GET /shipments/summary | GET | ❌ 미커버 | MEDIUM |
| Q2 | GET /shipments | GET | ❌ 미커버 | HIGH |
| Q3 | GET /shipments/{orderId} | GET | ❌ 미커버 | MEDIUM |
| C1 | POST /shipments/confirm | POST | ❌ 미커버 | HIGH |
| C2 | POST /shipments/ship | POST | ❌ 미커버 | HIGH |
| C3 | POST /shipments/{id}/ship | POST | ❌ 미커버 | MEDIUM |

**핵심 테스트 시나리오**:
- C1→Q2: 발주확인 → 목록 조회 (상태 CONFIRMED 확인)
- C1→C2→Q2: 발주확인 → 송장등록 → 상태 SHIPPED 확인
- C1 부분 실패: 일부 ID 없는 경우 BatchResult 검증
- C3→Q3: 단건 송장등록 → orderId로 상세 조회

**api-flow에서 발견된 이슈**:
- 배치 partial success 패턴: 200 OK with 항목별 success/failure
- orderId 기반 단건 조회 (shipmentId 아님)

---

#### 2.4 productgroupdescription (2 endpoints)

| ID | 엔드포인트 | HTTP | 커버 여부 | 우선순위 |
|----|-----------|------|----------|---------|
| Q1 | GET /product-groups/{id}/description/publish-status | GET | ❌ 미커버 | MEDIUM |
| C1 | PUT /product-groups/{id}/description | PUT | ❌ 미커버 | HIGH |

**핵심 테스트 시나리오**:
- (선행: productgroup C1) → C1: 설명 수정 → 이미지 diff (originUrl 기반)
- C1→Q1: 설명 수정 후 게시 상태 조회 → descriptionImage Outbox 생성 확인

**api-flow에서 발견된 이슈**:
- Dual Coordinator+Facade 패턴
- DescriptionImage Outbox: CDN 업로드 비동기 처리

---

#### 2.5 productgroupimage (2 endpoints)

| ID | 엔드포인트 | HTTP | 커버 여부 | 우선순위 |
|----|-----------|------|----------|---------|
| Q1 | GET /product-groups/{id}/images/upload-status | GET | ❌ 미커버 | MEDIUM |
| C1 | PUT /product-groups/{id}/images | PUT | ❌ 미커버 | HIGH |

**핵심 테스트 시나리오**:
- (선행: productgroup C1) → C1: 이미지 교체 → diff 기반 (originUrl+imageType key)
- C1→Q1: 이미지 수정 후 업로드 상태 조회

**api-flow에서 발견된 이슈**:
- diff-based update: 기존 삭제 + 신규 삽입 (hard replace)
- ImageUpload Outbox 생성 검증 필요

---

#### 2.6 productnotice (1 endpoint)

| ID | 엔드포인트 | HTTP | 커버 여부 | 우선순위 |
|----|-----------|------|----------|---------|
| C1 | PUT /product-groups/{id}/notice | PUT | ❌ 미커버 | HIGH |

**핵심 테스트 시나리오**:
- (선행: productgroup C1) → C1: 고시정보 수정 → Entry 목록 교체

**api-flow에서 발견된 이슈**:
- ⚠️ **Entry 삭제 미구현**: 기존 Entry가 삭제되지 않고 누적됨 (DB 정합성 위험)

---

#### 2.7 imagevariant (2 endpoints)

| ID | 엔드포인트 | HTTP | 커버 여부 | 우선순위 |
|----|-----------|------|----------|---------|
| Q1 | GET /image-variants | GET | ❌ 미커버 | MEDIUM |
| C1 | POST /image-variants/transform | POST | ❌ 미커버 | HIGH |

**핵심 테스트 시나리오**:
- C1→Q1: 이미지 변환 요청 (202 Accepted) → 변환 결과 조회
- C1: Outbox 패턴 검증 (비동기 처리, 즉시 응답)

**api-flow에서 발견된 이슈**:
- 3-stage Outbox scheduler (ProcessPending → PollProcessing → RecoverTimeout)
- 낙관적 잠금 기반 동시성 제어

---

#### 2.8 notice (2 endpoints - Read-only)

| ID | 엔드포인트 | HTTP | 커버 여부 | 우선순위 |
|----|-----------|------|----------|---------|
| Q1 | GET /notices | GET | ❌ 미커버 | MEDIUM |
| Q2 | GET /notices/{id} | GET | ❌ 미커버 | MEDIUM |

**핵심 테스트 시나리오**:
- Q1: 목록 조회 + 페이징
- Q2: 상세 조회 → NoticeField 포함 2-query 전략 검증
- Q1→Q2: 목록→상세 플로우

**api-flow에서 발견된 이슈**:
- 2-query 전략: NoticeCategoryAssembler에서 Field 배치 조회

---

### 🟡 Priority 2: Command E2E 미커버 (6개 도메인 - Query만 있음)

#### 2.9 shop (2 Command 미커버)

| ID | 엔드포인트 | HTTP | 커버 여부 | 기존 E2E |
|----|-----------|------|----------|---------|
| Q1 | GET /shops | GET | ✅ 커버 | 6 tests |
| C1 | POST /shops | POST | ❌ 미커버 | - |
| C2 | PUT /shops/{id} | PUT | ❌ 미커버 | - |

**필요한 E2E 테스트**:
- C1→Q1: 외부몰 등록 → 목록 조회 확인
- C1→C2→Q1: 등록 → 수정 → 조회 (변경 반영 확인)
- C1 중복 등록: shopName unique 제약 조건 검증

**api-flow에서 발견된 이슈**:
- Race condition 이중 방어: app validation + DB unique constraint

---

#### 2.10 brandpreset (3 Command 미커버)

| ID | 엔드포인트 | HTTP | 커버 여부 | 기존 E2E |
|----|-----------|------|----------|---------|
| Q1 | GET /brand-presets | GET | ✅ 커버 | 4 tests |
| Q2 | GET /brand-presets/{id} | GET | ✅ 커버 | 3 tests |
| C1 | POST /brand-presets | POST | ❌ 미커버 | - |
| C2 | PUT /brand-presets/{id} | PUT | ❌ 미커버 | - |
| C3 | DELETE /brand-presets | DELETE | ❌ 미커버 | - |

**필요한 E2E 테스트**:
- C1→Q2: 프리셋 등록 → 상세 조회
- C1→C2→Q2: 등록 → 수정 → 조회
- C1→C3→Q1: 등록 → 벌크 삭제 → 목록에서 제거 확인
- C3: 비대칭 삭제 전략 검증 (parent soft delete + child hard delete)

---

#### 2.11 categorypreset (3 Command 미커버)

| ID | 엔드포인트 | HTTP | 커버 여부 | 기존 E2E |
|----|-----------|------|----------|---------|
| Q1 | GET /category-presets | GET | ✅ 커버 | 4 tests |
| Q2 | GET /category-presets/{id} | GET | ✅ 커버 | 3 tests |
| C1 | POST /category-presets | POST | ❌ 미커버 | - |
| C2 | PUT /category-presets/{id} | PUT | ❌ 미커버 | - |
| C3 | DELETE /category-presets | DELETE | ❌ 미커버 | - |

**필요한 E2E 테스트**: brandpreset과 동일 패턴

---

#### 2.12 saleschannel (2 Command 미커버)

| ID | 엔드포인트 | HTTP | 커버 여부 | 기존 E2E |
|----|-----------|------|----------|---------|
| Q1 | GET /sales-channels | GET | ✅ 커버 | 4 tests |
| C1 | POST /sales-channels | POST | ❌ 미커버 | - |
| C2 | PUT /sales-channels/{id} | PUT | ❌ 미커버 | - |

**필요한 E2E 테스트**:
- C1→Q1: 채널 등록 → 목록 조회
- C1→C2→Q1: 등록 → 수정 → 조회

**api-flow에서 발견된 이슈**:
- ⚠️ **수정 시 채널명 중복 검증 미호출**: `existsByChannelNameExcluding()` 정의만 되어 있고 C2에서 호출 안 함

---

#### 2.13 saleschannelbrand (1 Command 미커버)

| ID | 엔드포인트 | HTTP | 커버 여부 | 기존 E2E |
|----|-----------|------|----------|---------|
| Q1 | GET /sales-channels/{id}/brands | GET | ✅ 커버 | 5 tests |
| C1 | PUT /sales-channels/{id}/brands | PUT | ❌ 미커버 | - |

**필요한 E2E 테스트**:
- C1→Q1: 브랜드 매핑 등록/수정 → 조회 확인

---

#### 2.14 saleschannelcategory (1 Command 미커버)

| ID | 엔드포인트 | HTTP | 커버 여부 | 기존 E2E |
|----|-----------|------|----------|---------|
| Q1 | GET /sales-channels/{id}/categories | GET | ✅ 커버 | 5 tests |
| C1 | PUT /sales-channels/{id}/categories | PUT | ❌ 미커버 | - |

**필요한 E2E 테스트**:
- C1→Q1: 카테고리 매핑 등록/수정 → 조회 확인

---

### 🟢 Priority 3: 완전 커버 (스킵 가능)

#### 2.15 canonicaloption (Read-only - ✅ 완전 커버)

| ID | 엔드포인트 | HTTP | 커버 여부 | 기존 E2E |
|----|-----------|------|----------|---------|
| Q1 | GET /canonical-option-groups | GET | ✅ 커버 | 3 tests |
| Q2 | GET /canonical-option-groups/{id} | GET | ✅ 커버 | 3 tests |

**결론**: Command 없는 Read-only 도메인, Query E2E 이미 커버. **추가 테스트 불필요**.

---

## 3. E2E 테스트 작성 우선순위 (권장 순서)

### Phase 1: CRITICAL - 핵심 비즈니스 플로우 (가장 먼저)

| 순서 | 도메인 | 시나리오 | 복잡도 | 이유 |
|------|--------|---------|--------|------|
| 1 | productgroup | C1→Q2 단건 등록→조회 | ⭐⭐⭐⭐⭐ | 8단계 Coordinator, 전체 시스템의 기반 |
| 2 | productgroup | C1→C3→Q2 등록→전체수정→조회 | ⭐⭐⭐⭐⭐ | diff 기반 업데이트, soft delete |
| 3 | product | C4 상품 일괄 수정 | ⭐⭐⭐⭐ | dual domain diff (SellerOption+Product) |
| 4 | productgroup | C5 배치 상태 변경 | ⭐⭐⭐ | 상태 전이 불변식 검증 |

### Phase 2: HIGH - 주요 Command 플로우

| 순서 | 도메인 | 시나리오 | 복잡도 | 이유 |
|------|--------|---------|--------|------|
| 5 | product | C1~C3 가격/재고/상태 수정 | ⭐⭐⭐ | Money VO, 소유권 검증 |
| 6 | shipment | C1→C2→Q2 발주→출하→조회 | ⭐⭐⭐ | 배치 partial success |
| 7 | productgroupdescription | C1→Q1 설명수정→상태조회 | ⭐⭐⭐ | 이미지 diff + Outbox |
| 8 | productgroupimage | C1→Q1 이미지교체→상태조회 | ⭐⭐⭐ | diff-based replace |
| 9 | productnotice | C1 고시정보 수정 | ⭐⭐ | Entry 삭제 미구현 이슈 확인 |
| 10 | productgroup | C2 배치 등록 | ⭐⭐⭐ | CompletableFuture 병렬처리 |

### Phase 3: MEDIUM - Command 보완

| 순서 | 도메인 | 시나리오 | 복잡도 | 이유 |
|------|--------|---------|--------|------|
| 11 | shop | C1→C2→Q1 등록→수정→조회 | ⭐⭐ | unique 제약 검증 |
| 12 | brandpreset | C1→C2→C3→Q1 CRUD 전체 | ⭐⭐ | 비대칭 삭제 전략 |
| 13 | categorypreset | C1→C2→C3→Q1 CRUD 전체 | ⭐⭐ | brandpreset과 동일 패턴 |
| 14 | saleschannel | C1→C2→Q1 등록→수정→조회 | ⭐⭐ | 중복 검증 누락 이슈 |
| 15 | saleschannelbrand | C1→Q1 매핑→조회 | ⭐ | 단순 매핑 |
| 16 | saleschannelcategory | C1→Q1 매핑→조회 | ⭐ | 단순 매핑 |

### Phase 4: LOW - Query 보완 / Read-only

| 순서 | 도메인 | 시나리오 | 복잡도 | 이유 |
|------|--------|---------|--------|------|
| 17 | notice | Q1→Q2 목록→상세 | ⭐ | Read-only, 시드 데이터 기반 |
| 18 | imagevariant | C1→Q1 변환요청→조회 | ⭐⭐ | Outbox 비동기, 외부 서비스 의존 |

---

## 4. 선행 의존성 맵

```
productgroup C1 (등록)
├── product C1~C4 (가격/재고/상태/일괄 수정) ← productgroup 등록이 선행 조건
├── productgroupdescription C1 (설명 수정) ← productgroup 등록이 선행 조건
├── productgroupimage C1 (이미지 교체) ← productgroup 등록이 선행 조건
├── productnotice C1 (고시정보 수정) ← productgroup 등록이 선행 조건
└── productgroup C3, C4, C5 (수정/상태변경) ← productgroup 등록이 선행 조건

shop C1 (등록)
├── saleschannel C1 (채널 등록) ← shop이 선행 조건 (가능)
│   ├── saleschannelbrand C1 ← saleschannel 등록이 선행
│   └── saleschannelcategory C1 ← saleschannel 등록이 선행

brandpreset/categorypreset는 독립적 (seller + brand/category 시드만 필요)
shipment는 독립적 (주문 시드 데이터 필요)
notice는 독립적 (시드 데이터 기반)
imagevariant는 독립적 (이미지 URL만 필요)
```

---

## 5. api-flow에서 발견된 주요 이슈 (E2E에서 검증 필요)

| # | 도메인 | 이슈 | 심각도 | E2E 검증 방법 |
|---|--------|------|--------|-------------|
| 1 | productnotice | Entry 삭제 미구현 (누적) | 🔴 HIGH | 수정 전후 Entry 수 비교 |
| 2 | saleschannel | 수정 시 채널명 중복 검증 미호출 | 🟡 MEDIUM | 동일 채널명으로 수정 시도 → 성공하면 버그 |
| 3 | productgroup C3 | Product soft delete + 재생성 | 🟡 MEDIUM | 수정 전후 productId 변경 확인 |
| 4 | product C4 | SellerOption diff ID 보존 | 🟡 MEDIUM | 옵션값 변경 후 optionValueId 유지 확인 |
| 5 | saleschannelcategory | depth/parentId/mapped 파라미터 미사용 | 🟢 LOW | 파라미터 전달 시 필터링 작동 확인 |

---

## 6. 추정 작업량

| Phase | E2E 테스트 파일 수 | 예상 테스트 메서드 수 |
|-------|-------------------|---------------------|
| Phase 1 (CRITICAL) | 2~3 | 15~20 |
| Phase 2 (HIGH) | 5~7 | 25~35 |
| Phase 3 (MEDIUM) | 5~6 | 15~20 |
| Phase 4 (LOW) | 2 | 5~8 |
| **총계** | **14~18** | **60~83** |

---

## 7. 스킵 가능한 테스트

| 도메인 | 이유 |
|--------|------|
| canonicaloption | Read-only + Query E2E 이미 완전 커버 (6 tests) |
| notice Q1/Q2 | Read-only 시드 데이터 조회, 우선순위 낮음 (단, 2-query 전략 검증 가치 있음) |
| imagevariant | 외부 FileFlow 서비스 의존, Mock 필요 → 단위 테스트로 대체 가능 |
