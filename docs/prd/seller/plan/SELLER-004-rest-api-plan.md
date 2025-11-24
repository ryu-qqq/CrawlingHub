# SELLER-004 TDD Plan

**Task**: Seller REST API Layer 구현
**Layer**: REST API (Adapter-In)
**브랜치**: feature/SELLER-004-rest-api
**예상 소요 시간**: 180분 (12 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ RegisterSellerApiRequest DTO 구현 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] `RegisterSellerApiRequestTest.java` 파일 생성
- [ ] `shouldCreateRequestWithValidData()` 테스트 작성
- [ ] `shouldFailValidationWhenMustItSellerIdIsBlank()` 테스트 작성
- [ ] `shouldFailValidationWhenSellerNameIsBlank()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: RegisterSellerApiRequest 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RegisterSellerApiRequest.java` 파일 생성 (Record)
- [ ] `@NotBlank` 어노테이션 추가 (mustItSellerId, sellerName)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: RegisterSellerApiRequest 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Bean Validation 어노테이션 검증
- [ ] Request DTO ArchUnit 테스트 추가 및 통과
- [ ] API DTO 네이밍 규칙 검증 (*ApiRequest 패턴)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: RegisterSellerApiRequest 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `RegisterSellerApiRequestFixture.java` 생성 (Object Mother 패턴)
- [ ] `RegisterSellerApiRequestFixture.aValidRequest()` 메서드 작성
- [ ] `RegisterSellerApiRequestTest` → Fixture 사용으로 리팩토링
- [ ] 커밋: `test: RegisterSellerApiRequestFixture 정리 (Tidy)`

---

### 2️⃣ ChangeSellerStatusApiRequest DTO 구현 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `ChangeSellerStatusApiRequestTest.java` 파일 생성
- [ ] `shouldCreateRequestWithTargetStatus()` 테스트 작성
- [ ] `shouldFailValidationWhenTargetStatusIsNull()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ChangeSellerStatusApiRequest 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ChangeSellerStatusApiRequest.java` 파일 생성 (Record)
- [ ] `@NotNull` 어노테이션 추가 (targetStatus)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ChangeSellerStatusApiRequest 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Request DTO ArchUnit 테스트 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: ChangeSellerStatusApiRequest 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ChangeSellerStatusApiRequestFixture` 생성
- [ ] 테스트 → Fixture 사용으로 리팩토링
- [ ] 커밋: `test: ChangeSellerStatusApiRequestFixture 정리 (Tidy)`

---

### 3️⃣ SellerApiResponse DTO 구현 (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `SellerApiResponseTest.java` 파일 생성
- [ ] `shouldCreateResponseWithAllFields()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerApiResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerApiResponse.java` 파일 생성 (Record)
- [ ] 필드 정의 (sellerId, mustItSellerId, sellerName, status, createdAt)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SellerApiResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Response DTO ArchUnit 테스트 추가 및 통과
- [ ] API DTO 네이밍 규칙 검증 (*ApiResponse 패턴)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: SellerApiResponse 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SellerApiResponseFixture` 생성
- [ ] 테스트 → Fixture 사용으로 리팩토링
- [ ] 커밋: `test: SellerApiResponseFixture 정리 (Tidy)`

---

### 4️⃣ SellerDetailApiResponse + SellerSummaryApiResponse (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `SellerDetailApiResponseTest.java` 파일 생성
- [ ] `shouldCreateDetailResponseWithSchedulerCounts()` 테스트 작성
- [ ] `SellerSummaryApiResponseTest.java` 파일 생성
- [ ] `shouldCreateSummaryResponse()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Seller Detail/Summary Response 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerDetailApiResponse.java` 파일 생성 (Record)
- [ ] `SellerSummaryApiResponse.java` 파일 생성 (Record)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Seller Detail/Summary Response 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Response DTO ArchUnit 테스트 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: Seller Detail/Summary Response 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 생성 및 테스트 정리
- [ ] 커밋: `test: Seller Response Fixture 정리 (Tidy)`

---

### 5️⃣ SellerApiMapper 구현 (DI 패턴) (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `SellerApiMapperTest.java` 파일 생성
- [ ] `shouldMapRequestToCommand()` 테스트 작성 (RegisterSellerApiRequest → RegisterSellerCommand)
- [ ] `shouldMapResponseFromApplicationResponse()` 테스트 작성 (SellerResponse → SellerApiResponse)
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SellerApiMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerApiMapper.java` 파일 생성 (@Component)
- [ ] `toCommand(RegisterSellerApiRequest)` 메서드 구현
- [ ] `toCommand(ChangeSellerStatusApiRequest, Long)` 메서드 구현
- [ ] `toResponse(SellerResponse)` 메서드 구현
- [ ] `toDetailResponse(SellerDetailResponse)` 메서드 구현
- [ ] ⚠️ **DI 패턴**: Static 메서드 금지, @Component Bean으로 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SellerApiMapper 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Mapper ArchUnit 테스트 추가 및 통과
- [ ] Static 메서드 없음 검증
- [ ] @Component 어노테이션 검증
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: SellerApiMapper DI 패턴 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mapper 테스트용 Fixture 추가
- [ ] 커밋: `test: SellerApiMapper Fixture 정리 (Tidy)`

---

### 6️⃣ GlobalExceptionHandler - RFC 7807 구현 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `GlobalExceptionHandlerTest.java` 파일 생성
- [ ] `shouldHandleDuplicateMustItSellerIdException()` 테스트 작성 (409 Conflict)
- [ ] `shouldHandleDuplicateSellerNameException()` 테스트 작성 (409 Conflict)
- [ ] `shouldHandleSellerHasActiveSchedulersException()` 테스트 작성 (400 Bad Request)
- [ ] `shouldHandleSellerNotFoundException()` 테스트 작성 (404 Not Found)
- [ ] RFC 7807 형식 검증 (type, title, status, detail, instance, timestamp)
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: GlobalExceptionHandler 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GlobalExceptionHandler.java` 파일 생성 (@RestControllerAdvice)
- [ ] `ProblemDetail` 사용 (RFC 7807 표준)
- [ ] 각 Exception Handler 메서드 구현
  - `@ExceptionHandler(DuplicateMustItSellerIdException.class)`
  - `@ExceptionHandler(DuplicateSellerNameException.class)`
  - `@ExceptionHandler(SellerHasActiveSchedulersException.class)`
  - `@ExceptionHandler(SellerNotFoundException.class)`
  - `@ExceptionHandler(MethodArgumentNotValidException.class)`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: GlobalExceptionHandler RFC 7807 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Content-Type: `application/problem+json` 검증
- [ ] Exception Handler ArchUnit 테스트 추가 및 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: GlobalExceptionHandler 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Exception 시나리오별 Fixture 추가
- [ ] 커밋: `test: GlobalExceptionHandler Fixture 정리 (Tidy)`

---

### 7️⃣ SellerCommandController - POST /api/v1/sellers (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `SellerCommandControllerTest.java` 파일 생성 (TestRestTemplate 사용)
- [ ] `shouldRegisterSellerSuccessfully()` 테스트 작성 (201 Created)
- [ ] `shouldReturn409WhenDuplicateMustItSellerId()` 테스트 작성
- [ ] `shouldReturn409WhenDuplicateSellerName()` 테스트 작성
- [ ] `shouldReturn400WhenInvalidRequest()` 테스트 작성 (Bean Validation)
- [ ] ⚠️ **MockMvc 금지**: TestRestTemplate 사용
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: POST /api/v1/sellers 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerCommandController.java` 파일 생성 (@RestController)
- [ ] `RegisterSellerUseCase` 의존성 주입
- [ ] `SellerApiMapper` 의존성 주입
- [ ] `POST /api/v1/sellers` 엔드포인트 구현
  - `@Valid RegisterSellerApiRequest` 파라미터
  - RegisterSellerUseCase 호출
  - 201 Created 응답
- [ ] ⚠️ **CQRS 분리**: Command 전용 Controller
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: POST /api/v1/sellers 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증 (URI, HTTP Method, Status Code)
- [ ] Controller ArchUnit 테스트 추가 및 통과
- [ ] Application Layer만 의존 확인 (Domain Layer 직접 의존 금지)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: POST /api/v1/sellers 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Controller 테스트용 Fixture 추가
- [ ] 커밋: `test: SellerCommandController Fixture 정리 (Tidy)`

---

### 8️⃣ SellerCommandController - PATCH /api/v1/sellers/{sellerId}/status (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `shouldChangeSellerStatusSuccessfully()` 테스트 작성 (200 OK)
- [ ] `shouldReturn400WhenActiveSchedulersExist()` 테스트 작성
- [ ] `shouldReturn404WhenSellerNotFound()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: PATCH /api/v1/sellers/{sellerId}/status 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ChangeSellerStatusUseCase` 의존성 주입
- [ ] `PATCH /api/v1/sellers/{sellerId}/status` 엔드포인트 구현
  - `@Valid ChangeSellerStatusApiRequest` 파라미터
  - `@PathVariable Long sellerId` 파라미터
  - ChangeSellerStatusUseCase 호출
  - 200 OK 응답
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: PATCH /api/v1/sellers/{sellerId}/status 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] Controller ArchUnit 테스트 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: PATCH /api/v1/sellers/{sellerId}/status 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 상태 변경 시나리오별 Fixture 추가
- [ ] 커밋: `test: SellerCommandController 상태 변경 Fixture 정리 (Tidy)`

---

### 9️⃣ SellerQueryController - GET /api/v1/sellers/{sellerId} (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `SellerQueryControllerTest.java` 파일 생성 (TestRestTemplate 사용)
- [ ] `shouldGetSellerSuccessfully()` 테스트 작성 (200 OK)
- [ ] `shouldReturn404WhenSellerNotFound()` 테스트 작성
- [ ] ⚠️ **CQRS 분리**: Query 전용 Controller
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: GET /api/v1/sellers/{sellerId} 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerQueryController.java` 파일 생성 (@RestController)
- [ ] `GetSellerUseCase` 의존성 주입
- [ ] `SellerApiMapper` 의존성 주입
- [ ] `GET /api/v1/sellers/{sellerId}` 엔드포인트 구현
  - `@PathVariable Long sellerId` 파라미터
  - GetSellerUseCase 호출
  - 200 OK 응답
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: GET /api/v1/sellers/{sellerId} 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] Query Controller ArchUnit 테스트 추가 및 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: GET /api/v1/sellers/{sellerId} 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Query Controller 테스트용 Fixture 추가
- [ ] 커밋: `test: SellerQueryController Fixture 정리 (Tidy)`

---

### 🔟 SellerQueryController - GET /api/v1/sellers (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `shouldListSellersWithPagination()` 테스트 작성 (200 OK)
- [ ] `shouldFilterSellersByStatus()` 테스트 작성
- [ ] `shouldReturnEmptyPageWhenNoSellers()` 테스트 작성
- [ ] Query Parameter 검증 (status, page, size)
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: GET /api/v1/sellers 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ListSellersUseCase` 의존성 주입
- [ ] `GET /api/v1/sellers` 엔드포인트 구현
  - `@RequestParam(required = false) SellerStatus status` 파라미터
  - `@RequestParam(defaultValue = "0") int page` 파라미터
  - `@RequestParam(defaultValue = "10") int size` 파라미터
  - ListSellersUseCase 호출
  - PageApiResponse 변환
  - 200 OK 응답
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: GET /api/v1/sellers 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] 페이징 파라미터 검증 로직 추가
- [ ] Controller ArchUnit 테스트 통과
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: GET /api/v1/sellers 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 페이징 시나리오별 Fixture 추가
- [ ] 커밋: `test: SellerQueryController 목록 조회 Fixture 정리 (Tidy)`

---

### 1️⃣1️⃣ Spring REST Docs 문서화 (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `SellerApiDocumentationTest.java` 파일 생성
- [ ] POST /api/v1/sellers 문서화 테스트
- [ ] PATCH /api/v1/sellers/{sellerId}/status 문서화 테스트
- [ ] GET /api/v1/sellers/{sellerId} 문서화 테스트
- [ ] GET /api/v1/sellers 문서화 테스트
- [ ] 테스트 실행 → AsciiDoc 생성 확인
- [ ] 커밋: `test: Seller API 문서화 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Spring REST Docs 설정 추가 (build.gradle)
- [ ] API 문서 스니펫 생성
- [ ] 각 엔드포인트별 문서화 코드 추가
  - Request Fields 문서화
  - Response Fields 문서화
  - Path Parameters 문서화
  - Query Parameters 문서화
- [ ] 테스트 실행 → AsciiDoc 생성 확인
- [ ] 커밋: `feat: Seller API 문서화 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 문서화 코드 가독성 개선
- [ ] 공통 문서 스니펫 추출
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: Seller API 문서화 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 문서화 테스트용 Fixture 정리
- [ ] 커밋: `test: Seller API 문서화 Fixture 정리 (Tidy)`

---

### 1️⃣2️⃣ ArchUnit 종합 테스트 (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `SellerRestApiArchitectureTest.java` 파일 생성
- [ ] Controller는 Application Layer만 의존 검증
- [ ] Controller는 Domain Layer 직접 의존 금지 검증
- [ ] DTO는 Record 타입 검증
- [ ] Request DTO는 *ApiRequest 네이밍 검증
- [ ] Response DTO는 *ApiResponse 네이밍 검증
- [ ] Mapper는 @Component 어노테이션 검증
- [ ] Mapper는 Static 메서드 없음 검증
- [ ] Controller는 CQRS 분리 검증 (Command/Query)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Seller REST API ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 모든 ArchUnit 규칙 통과하도록 수정
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Seller REST API ArchUnit 규칙 준수 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 추가 검증
- [ ] RESTful 설계 원칙 최종 검증
- [ ] RFC 7807 준수 최종 검증
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: Seller REST API 최종 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 모든 Fixture 최종 정리
- [ ] 테스트 코드 가독성 개선
- [ ] 테스트 코드 중복 제거
- [ ] 커밋: `test: Seller REST API Fixture 최종 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (12 사이클 × 4단계 = 48개 체크박스 모두 ✅)
- [ ] 모든 단위 테스트 통과
  - Request/Response DTO 테스트
  - SellerApiMapper 테스트
  - GlobalExceptionHandler 테스트
  - SellerCommandController 테스트
  - SellerQueryController 테스트
- [ ] Integration Test 통과 (TestRestTemplate)
  - 성공 케이스 (201, 200)
  - 실패 케이스 (400, 404, 409)
  - Validation 테스트
  - Exception Handling 테스트
- [ ] ArchUnit 테스트 통과
  - Controller ArchUnit 검증
  - DTO ArchUnit 검증
  - Mapper ArchUnit 검증
  - REST API Layer 의존성 검증
  - CQRS 분리 검증
- [ ] Zero-Tolerance 규칙 준수 확인
  - RESTful 설계 원칙 (URI, HTTP Method, Status Code)
  - RFC 7807 Problem Details 준수 (필수!)
  - Bean Validation 필수
  - TestRestTemplate 사용 (MockMvc 금지)
  - DI Mapper 패턴 (@Component, Static 메서드 금지)
  - CQRS Controller 분리 (Command/Query)
  - API DTO 네이밍 규칙 (*ApiRequest/*ApiResponse)
- [ ] TestFixture 모두 정리 완료
  - Request/Response DTO Fixture
  - SellerApiMapper Fixture
  - Controller 테스트 Fixture
- [ ] Spring REST Docs 문서화 완료
  - 각 API 엔드포인트 문서화
- [ ] 테스트 커버리지 > 80%

---

## 📊 사이클 요약

| Cycle | 요구사항 | Red | Green | Refactor | Tidy |
|-------|----------|-----|-------|----------|------|
| 1 | RegisterSellerApiRequest | test: | feat: | struct: | test: |
| 2 | ChangeSellerStatusApiRequest | test: | feat: | struct: | test: |
| 3 | SellerApiResponse | test: | feat: | struct: | test: |
| 4 | Detail/Summary Response | test: | feat: | struct: | test: |
| 5 | SellerApiMapper (DI) | test: | feat: | struct: | test: |
| 6 | GlobalExceptionHandler (RFC 7807) | test: | feat: | struct: | test: |
| 7 | POST /api/v1/sellers | test: | feat: | struct: | test: |
| 8 | PATCH /sellers/{id}/status | test: | feat: | struct: | test: |
| 9 | GET /api/v1/sellers/{id} | test: | feat: | struct: | test: |
| 10 | GET /api/v1/sellers | test: | feat: | struct: | test: |
| 11 | Spring REST Docs | test: | feat: | struct: | test: |
| 12 | ArchUnit 종합 테스트 | test: | feat: | struct: | test: |

**총 커밋 수**: 48개 (12 사이클 × 4단계)

---

## 🔗 관련 문서

- **Task**: `/Users/sangwon-ryu/crawlinghub/docs/prd/seller/SELLER-004-rest-api.md`
- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **코딩 규칙**: `docs/coding_convention/01-adapter-in-layer/rest-api/`

---

## 🎯 다음 단계

1. `/kb/rest-api/go` - REST API Layer TDD 시작 (Cycle 1부터)
2. 각 사이클마다 Red → Green → Refactor → Tidy 순서로 진행
3. 모든 사이클 완료 후 PR 생성
