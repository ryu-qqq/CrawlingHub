# 커밋 메시지 제안

## 커밋 제목
```
feat(seller): 셀러 상세 조회 API 확장 및 CQRS 패턴 적용
```

## 커밋 본문
```
셀러 Bounded Context 리팩토링 - Phase 1-4 완료

### 주요 변경사항

#### Phase 1: Domain Layer
- ✅ ProductCountHistory 도메인 객체 생성
- ✅ ProductCountHistoryId Value Object 생성
- ✅ Domain Layer 단위 테스트 작성

#### Phase 2: Application Layer
- ✅ SaveProductCountHistoryPort / LoadProductCountHistoryPort 생성
- ✅ SellerManager 구현 (Manager 패턴)
- ✅ SellerCommandFacade 구현 (Facade 패턴)
- ✅ GetSellerDetailService 확장 (getDetail 메서드 추가)
- ✅ DTO 생성: ProductCountHistoryResponse, ScheduleInfoResponse, ScheduleHistoryResponse
- ✅ SellerDetailResponse 확장 (PageResponse 필드 추가)
- ✅ SellerAssembler 확장 (변환 메서드 추가)

#### Phase 3: Persistence Layer (CQRS)
- ✅ Flyway 마이그레이션 스크립트 작성 (V2__Create_product_count_history.sql)
- ✅ ProductCountHistoryEntity JPA Entity 생성
- ✅ ProductCountHistoryJpaRepository (Command - JPA)
- ✅ ProductCountHistoryQueryRepository (Query - QueryDSL)
- ✅ ProductCountHistoryMapper (MapStruct)
- ✅ ProductCountHistoryCommandAdapter (Command Adapter)
- ✅ ProductCountHistoryQueryAdapter (Query Adapter)

#### Phase 4: REST API Layer
- ✅ PageApiResponse 공통 DTO 생성
- ✅ API DTO 생성: ProductCountHistoryApiResponse, ScheduleInfoApiResponse, ScheduleHistoryApiResponse
- ✅ SellerDetailApiResponse 생성 (PageApiResponse 필드 포함)
- ✅ SellerApiMapper 확장 (PageResponse → PageApiResponse 변환)
- ✅ SellerController 수정 (상세 조회 API 추가)

### 설계 패턴 적용
- CQRS 패턴: Command/Query 분리
- Manager 패턴: SellerManager로 횡단 관심사 처리
- Facade 패턴: SellerCommandFacade로 여러 UseCase 조율
- PageResponse 통합: Application Layer → REST API Layer 변환

### Zero-Tolerance 준수
- ✅ Lombok 금지 (Pure Java)
- ✅ Long FK 전략 (JPA 관계 어노테이션 없음)
- ✅ Javadoc 필수
- ✅ Law of Demeter 준수

### 향후 작업 (다음 페이즈)
- 스케줄 관련 Port 구현 후 ScheduleInfo, ScheduleHistory 기능 활성화
- 현재는 DTO와 API 구조는 준비되어 있으며, 실제 구현은 다음 페이즈에서 진행

### 관련 이슈
- seller-refactoring-plan.md 참조
```

## PR 제목 제안
```
feat(seller): 셀러 상세 조회 API 확장 및 CQRS 패턴 적용 (Phase 1-4)
```

## PR 설명 제안
```markdown
## 📋 개요
셀러 Bounded Context 리팩토링 작업의 Phase 1-4를 완료했습니다.

## ✨ 주요 기능
- 셀러 상세 조회 API 확장 (상품 수 이력, 스케줄 정보 추가)
- ProductCountHistory 도메인 및 CQRS 패턴 적용
- Manager/Facade 패턴 구현

## 🏗️ 아키텍처 변경사항
- **CQRS 패턴**: Command/Query 분리
- **Manager 패턴**: SellerManager로 횡단 관심사 처리
- **Facade 패턴**: SellerCommandFacade로 여러 UseCase 조율
- **PageResponse 통합**: Application Layer → REST API Layer

## 📝 변경 파일
- Domain Layer: ProductCountHistory, ProductCountHistoryId
- Application Layer: Manager, Facade, Ports, DTOs
- Persistence Layer: Entity, Repository (CQRS), Mapper, Adapter
- REST API Layer: API DTOs, Mapper, Controller

## ⚠️ 주의사항
- 스케줄 관련 기능은 다음 페이즈에서 활성화 예정
- 현재는 DTO와 API 구조만 준비되어 있음

## ✅ 체크리스트
- [x] Zero-Tolerance 규칙 준수
- [x] Javadoc 작성 완료
- [x] CQRS 패턴 적용
- [x] Manager/Facade 패턴 구현
```
