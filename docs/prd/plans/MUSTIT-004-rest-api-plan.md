# MUSTIT-004 TDD Plan

**Task**: REST API Layer 구현
**Layer**: REST API Layer (Adapter-In)
**브랜치**: feature/MUSTIT-004-rest-api
**예상 소요 시간**: 1080분 (72 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ RegisterSellerRequest DTO 구현 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] `RegisterSellerRequestTest.java` 생성
- [ ] `shouldCreateRequestWithValidData()` 작성
- [ ] `shouldRejectInvalidSellerId()` 작성 (Validation 테스트)
- [ ] `shouldRejectInvalidInterval()` 작성 (@Min, @Max 테스트)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: RegisterSellerRequest DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RegisterSellerRequest.java` 생성 (Record)
- [ ] 필드: sellerId, name, crawlingIntervalDays
- [ ] Bean Validation: `@NotBlank`, `@Min(1)`, `@Max(30)`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: RegisterSellerRequest DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Validation 메시지 명확화
- [ ] ArchUnit 테스트 추가 (Request DTO 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: RegisterSellerRequest DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `RegisterSellerRequestFixture.java` 생성
- [ ] `aRegisterSellerRequest()` 메서드 작성
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: RegisterSellerRequestFixture 정리 (Tidy)`

---

### 2️⃣ SellerResponse DTO 구현 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `SellerResponseTest.java` 생성
- [ ] `shouldCreateResponseFromApplication()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SellerResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerResponse.java` 생성 (Record)
- [ ] 필드: sellerId, name, status, crawlingIntervalDays, totalProductCount, createdAt, updatedAt
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SellerResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Response DTO 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SellerResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SellerResponseFixture.java` 생성
- [ ] 커밋: `test: SellerResponseFixture 정리 (Tidy)`

---

### 3️⃣ ErrorResponse DTO 구현 (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `ErrorResponseTest.java` 생성
- [ ] `shouldCreateErrorResponse()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ErrorResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ErrorResponse.java` 생성 (Record)
- [ ] 필드: errorCode, message, timestamp, path
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ErrorResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ErrorResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ErrorResponseFixture.java` 생성
- [ ] 커밋: `test: ErrorResponseFixture 정리 (Tidy)`

---

### 4️⃣ GlobalExceptionHandler 구현 - Part 1 (SellerNotFoundException) (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `GlobalExceptionHandlerTest.java` 생성
- [ ] `shouldHandle404NotFound()` 작성
- [ ] Mock SellerNotFoundException 발생
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GlobalExceptionHandler 404 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GlobalExceptionHandler.java` 생성 (`@RestControllerAdvice`)
- [ ] `@ExceptionHandler(SellerNotFoundException.class)` 메서드 구현
- [ ] ErrorResponse 생성 및 반환 (404 Not Found)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GlobalExceptionHandler 404 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 에러 코드 상수화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GlobalExceptionHandler 404 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Exception Fixture 정리
- [ ] 커밋: `test: GlobalExceptionHandler 404 테스트 정리 (Tidy)`

---

### 5️⃣ GlobalExceptionHandler 구현 - Part 2 (DuplicateSellerIdException) (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `shouldHandle409Conflict()` 작성
- [ ] Mock DuplicateSellerIdException 발생
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GlobalExceptionHandler 409 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `@ExceptionHandler(DuplicateSellerIdException.class)` 메서드 구현
- [ ] ErrorResponse 생성 (409 Conflict)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GlobalExceptionHandler 409 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 에러 코드 상수화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GlobalExceptionHandler 409 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Exception Fixture 정리
- [ ] 커밋: `test: GlobalExceptionHandler 409 테스트 정리 (Tidy)`

---

### 6️⃣ GlobalExceptionHandler 구현 - Part 3 (MethodArgumentNotValidException) (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `shouldHandle400BadRequest()` 작성
- [ ] Mock MethodArgumentNotValidException 발생
- [ ] 필드별 에러 메시지 검증 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GlobalExceptionHandler 400 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `@ExceptionHandler(MethodArgumentNotValidException.class)` 메서드 구현
- [ ] BindingResult에서 필드별 에러 메시지 추출
- [ ] ErrorResponse 생성 (400 Bad Request)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GlobalExceptionHandler 400 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 에러 메시지 조합 로직 개선
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GlobalExceptionHandler 400 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Validation Error Fixture 정리
- [ ] 커밋: `test: GlobalExceptionHandler 400 테스트 정리 (Tidy)`

---

### 7️⃣ GlobalExceptionHandler 구현 - Part 4 (일반 Exception) (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `shouldHandle500InternalServerError()` 작성
- [ ] Mock Exception 발생
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GlobalExceptionHandler 500 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `@ExceptionHandler(Exception.class)` 메서드 구현
- [ ] ErrorResponse 생성 (500 Internal Server Error)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GlobalExceptionHandler 500 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 로깅 추가 (에러 스택 트레이스)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GlobalExceptionHandler 500 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Exception Fixture 정리
- [ ] 커밋: `test: GlobalExceptionHandler 500 테스트 정리 (Tidy)`

---

### 8️⃣ POST /api/v1/sellers - 셀러 등록 (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `SellerApiControllerTest.java` 생성 (@SpringBootTest + TestRestTemplate)
- [ ] `shouldRegisterSeller201Created()` 작성
- [ ] TestRestTemplate.postForEntity() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: POST /api/v1/sellers 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SellerApiController.java` 생성 (`@RestController`, `@RequestMapping("/api/v1/sellers")`)
- [ ] RegisterSellerUseCase 주입
- [ ] `@PostMapping` 메서드 구현
- [ ] `@Valid @RequestBody RegisterSellerRequest` 받기
- [ ] RegisterSellerCommand 생성 → UseCase 호출
- [ ] ResponseEntity.status(HttpStatus.CREATED).body(response) 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: POST /api/v1/sellers 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증 (리소스 기반 URL)
- [ ] ArchUnit 테스트 추가 (Controller 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: POST /api/v1/sellers 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Request/Response Fixture 사용
- [ ] 커밋: `test: POST /api/v1/sellers 테스트 정리 (Tidy)`

---

### 9️⃣ POST /api/v1/sellers - Validation 테스트 (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `shouldRejectInvalidSellerIdWith400BadRequest()` 작성
- [ ] `shouldRejectInvalidIntervalWith400BadRequest()` 작성
- [ ] 잘못된 RegisterSellerRequest → 400 Bad Request 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: POST /api/v1/sellers Validation 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `@Validated` 어노테이션 추가 (Controller)
- [ ] Bean Validation 자동 동작 확인
- [ ] GlobalExceptionHandler가 400 반환 확인
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: POST /api/v1/sellers Validation 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Validation 메시지 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: POST /api/v1/sellers Validation 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Invalid Request Fixture 생성
- [ ] 커밋: `test: POST /api/v1/sellers Validation 테스트 정리 (Tidy)`

---

### 🔟 POST /api/v1/sellers - 중복 에러 테스트 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `shouldRejectDuplicateSellerIdWith409Conflict()` 작성
- [ ] 이미 존재하는 sellerId → 409 Conflict 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: POST /api/v1/sellers 중복 에러 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] DuplicateSellerIdException 발생 시나리오 구현 (UseCase에서)
- [ ] GlobalExceptionHandler가 409 반환 확인
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: POST /api/v1/sellers 중복 에러 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 에러 메시지 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: POST /api/v1/sellers 중복 에러 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Duplicate Seller Fixture 생성
- [ ] 커밋: `test: POST /api/v1/sellers 중복 에러 테스트 정리 (Tidy)`

---

### 1️⃣1️⃣ GET /api/v1/sellers/{sellerId} - 셀러 조회 (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `shouldGetSeller200OK()` 작성
- [ ] TestRestTemplate.getForEntity() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/sellers/{sellerId} 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] GetSellerUseCase 주입
- [ ] `@GetMapping("/{sellerId}")` 메서드 구현
- [ ] `@PathVariable String sellerId` 받기
- [ ] GetSellerQuery 생성 → UseCase 호출
- [ ] ResponseEntity.ok(response) 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/sellers/{sellerId} 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/sellers/{sellerId} 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Query/Response Fixture 사용
- [ ] 커밋: `test: GET /api/v1/sellers/{sellerId} 테스트 정리 (Tidy)`

---

### 1️⃣2️⃣ GET /api/v1/sellers/{sellerId} - 404 테스트 (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `shouldReturn404WhenSellerNotFound()` 작성
- [ ] 존재하지 않는 sellerId → 404 Not Found 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/sellers/{sellerId} 404 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] SellerNotFoundException 발생 시나리오 (UseCase에서)
- [ ] GlobalExceptionHandler가 404 반환 확인
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/sellers/{sellerId} 404 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 에러 메시지 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/sellers/{sellerId} 404 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Not Found Scenario Fixture 생성
- [ ] 커밋: `test: GET /api/v1/sellers/{sellerId} 404 테스트 정리 (Tidy)`

---

### 1️⃣3️⃣ ListSellersRequest DTO 구현 (Cycle 13)

#### 🔴 Red: 테스트 작성
- [ ] `ListSellersRequestTest.java` 생성
- [ ] `shouldCreateRequestWithPaging()` 작성
- [ ] Validation 테스트 (@Min, @Max)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ListSellersRequest DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ListSellersRequest.java` 생성 (Record)
- [ ] 필드: status (Nullable), page, size
- [ ] Bean Validation: `@Min(0)`, `@Min(1)`, `@Max(100)`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ListSellersRequest DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ListSellersRequest DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ListSellersRequestFixture.java` 생성
- [ ] 커밋: `test: ListSellersRequestFixture 정리 (Tidy)`

---

### 1️⃣4️⃣ PageResponse DTO 구현 (Cycle 14)

#### 🔴 Red: 테스트 작성
- [ ] `PageResponseTest.java` 생성
- [ ] `shouldCreatePageResponse()` 작성
- [ ] Generic 타입 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: PageResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `PageResponse.java` 생성 (Generic Record)
- [ ] 필드: content, page, size, totalElements, totalPages
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: PageResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: PageResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `PageResponseFixture.java` 생성
- [ ] 커밋: `test: PageResponseFixture 정리 (Tidy)`

---

### 1️⃣5️⃣ GET /api/v1/sellers - 셀러 목록 조회 (Cycle 15)

#### 🔴 Red: 테스트 작성
- [ ] `shouldListSellersWithPaging()` 작성
- [ ] Query Parameters: status, page, size
- [ ] TestRestTemplate.getForEntity() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/sellers 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ListSellersUseCase 주입
- [ ] `@GetMapping` 메서드 구현
- [ ] `@Valid ListSellersRequest` 받기 (Query Parameters)
- [ ] ListSellersQuery 생성 → UseCase 호출
- [ ] PageResponse 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/sellers 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 페이징 기본값 설정 (page=0, size=20)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/sellers 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Paging Request/Response Fixture 사용
- [ ] 커밋: `test: GET /api/v1/sellers 테스트 정리 (Tidy)`

---

### 1️⃣6️⃣ UpdateSellerIntervalRequest DTO 구현 (Cycle 16)

#### 🔴 Red: 테스트 작성
- [ ] `UpdateSellerIntervalRequestTest.java` 생성
- [ ] `shouldCreateRequestWithValidInterval()` 작성
- [ ] Validation 테스트 (@Min, @Max)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: UpdateSellerIntervalRequest DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UpdateSellerIntervalRequest.java` 생성 (Record)
- [ ] 필드: crawlingIntervalDays
- [ ] Bean Validation: `@Min(1)`, `@Max(30)`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: UpdateSellerIntervalRequest DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: UpdateSellerIntervalRequest DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `UpdateSellerIntervalRequestFixture.java` 생성
- [ ] 커밋: `test: UpdateSellerIntervalRequestFixture 정리 (Tidy)`

---

### 1️⃣7️⃣ PATCH /api/v1/sellers/{sellerId}/interval - 셀러 주기 변경 (Cycle 17)

#### 🔴 Red: 테스트 작성
- [ ] `shouldUpdateSellerInterval200OK()` 작성
- [ ] TestRestTemplate.patchForObject() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: PATCH /api/v1/sellers/{sellerId}/interval 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] UpdateSellerIntervalUseCase 주입
- [ ] `@PatchMapping("/{sellerId}/interval")` 메서드 구현
- [ ] `@Valid @RequestBody UpdateSellerIntervalRequest` 받기
- [ ] UpdateSellerIntervalCommand 생성 → UseCase 호출
- [ ] ResponseEntity.ok(response) 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: PATCH /api/v1/sellers/{sellerId}/interval 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증 (PATCH 사용)
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: PATCH /api/v1/sellers/{sellerId}/interval 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Request/Response Fixture 사용
- [ ] 커밋: `test: PATCH /api/v1/sellers/{sellerId}/interval 테스트 정리 (Tidy)`

---

### 1️⃣8️⃣ POST /api/v1/sellers/{sellerId}/activate - 셀러 활성화 (Cycle 18)

#### 🔴 Red: 테스트 작성
- [ ] `shouldActivateSeller200OK()` 작성
- [ ] TestRestTemplate.postForEntity() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: POST /api/v1/sellers/{sellerId}/activate 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ActivateSellerUseCase 주입 (또는 UpdateSellerStatusUseCase)
- [ ] `@PostMapping("/{sellerId}/activate")` 메서드 구현
- [ ] ActivateSellerCommand 생성 → UseCase 호출
- [ ] ResponseEntity.ok(response) 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: POST /api/v1/sellers/{sellerId}/activate 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: POST /api/v1/sellers/{sellerId}/activate 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Activate Request/Response Fixture 사용
- [ ] 커밋: `test: POST /api/v1/sellers/{sellerId}/activate 테스트 정리 (Tidy)`

---

### 1️⃣9️⃣ POST /api/v1/sellers/{sellerId}/deactivate - 셀러 비활성화 (Cycle 19)

#### 🔴 Red: 테스트 작성
- [ ] `shouldDeactivateSeller200OK()` 작성
- [ ] TestRestTemplate.postForEntity() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: POST /api/v1/sellers/{sellerId}/deactivate 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] DeactivateSellerUseCase 주입
- [ ] `@PostMapping("/{sellerId}/deactivate")` 메서드 구현
- [ ] DeactivateSellerCommand 생성 → UseCase 호출
- [ ] ResponseEntity.ok(response) 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: POST /api/v1/sellers/{sellerId}/deactivate 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: POST /api/v1/sellers/{sellerId}/deactivate 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Deactivate Request/Response Fixture 사용
- [ ] 커밋: `test: POST /api/v1/sellers/{sellerId}/deactivate 테스트 정리 (Tidy)`

---

### 2️⃣0️⃣ GetCrawlingMetricsRequest DTO 구현 (Cycle 20)

#### 🔴 Red: 테스트 작성
- [ ] `GetCrawlingMetricsRequestTest.java` 생성
- [ ] `shouldCreateRequestWithSellerIdAndDate()` 작성
- [ ] Validation 테스트 (@NotBlank, @NotNull)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GetCrawlingMetricsRequest DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetCrawlingMetricsRequest.java` 생성 (Record)
- [ ] 필드: sellerId, date
- [ ] Bean Validation: `@NotBlank`, `@NotNull`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GetCrawlingMetricsRequest DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GetCrawlingMetricsRequest DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `GetCrawlingMetricsRequestFixture.java` 생성
- [ ] 커밋: `test: GetCrawlingMetricsRequestFixture 정리 (Tidy)`

---

### 2️⃣1️⃣ CrawlingMetricsResponse DTO 구현 (Cycle 21)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlingMetricsResponseTest.java` 생성
- [ ] `shouldCreateResponseWithMetrics()` 작성
- [ ] Nested TaskStats Record 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: CrawlingMetricsResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlingMetricsResponse.java` 생성 (Record)
- [ ] 필드: sellerId, date, successRate, progressRate, taskStats
- [ ] Nested `TaskStats` Record (total, completed, failed, inProgress)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlingMetricsResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlingMetricsResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `CrawlingMetricsResponseFixture.java` 생성
- [ ] 커밋: `test: CrawlingMetricsResponseFixture 정리 (Tidy)`

---

### 2️⃣2️⃣ GET /api/v1/metrics/crawling - 크롤링 메트릭 조회 (Cycle 22)

#### 🔴 Red: 테스트 작성
- [ ] `MetricsApiControllerTest.java` 생성
- [ ] `shouldGetCrawlingMetrics200OK()` 작성
- [ ] Query Parameters: sellerId, date
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/metrics/crawling 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `MetricsApiController.java` 생성 (`@RestController`, `@RequestMapping("/api/v1/metrics")`)
- [ ] GetCrawlingMetricsUseCase 주입
- [ ] `@GetMapping("/crawling")` 메서드 구현
- [ ] `@Valid GetCrawlingMetricsRequest` 받기 (Query Parameters)
- [ ] GetCrawlingMetricsQuery 생성 → UseCase 호출
- [ ] ResponseEntity.ok(response) 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/metrics/crawling 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/metrics/crawling 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Request/Response Fixture 사용
- [ ] 커밋: `test: GET /api/v1/metrics/crawling 테스트 정리 (Tidy)`

---

### 2️⃣3️⃣ UserAgentPoolStatusResponse DTO 구현 (Cycle 23)

#### 🔴 Red: 테스트 작성
- [ ] `UserAgentPoolStatusResponseTest.java` 생성
- [ ] `shouldCreateResponseWithPoolStatus()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: UserAgentPoolStatusResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UserAgentPoolStatusResponse.java` 생성 (Record)
- [ ] 필드: totalCount, activeCount, suspendedCount, blockedCount
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: UserAgentPoolStatusResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: UserAgentPoolStatusResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `UserAgentPoolStatusResponseFixture.java` 생성
- [ ] 커밋: `test: UserAgentPoolStatusResponseFixture 정리 (Tidy)`

---

### 2️⃣4️⃣ GET /api/v1/user-agents/status - UserAgent 풀 상태 조회 (Cycle 24)

#### 🔴 Red: 테스트 작성
- [ ] `UserAgentApiControllerTest.java` 생성
- [ ] `shouldGetUserAgentPoolStatus200OK()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/user-agents/status 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UserAgentApiController.java` 생성 (`@RestController`, `@RequestMapping("/api/v1/user-agents")`)
- [ ] GetUserAgentPoolStatusUseCase 주입
- [ ] `@GetMapping("/status")` 메서드 구현
- [ ] GetUserAgentPoolStatusQuery 생성 → UseCase 호출
- [ ] ResponseEntity.ok(response) 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/user-agents/status 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/user-agents/status 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Response Fixture 사용
- [ ] 커밋: `test: GET /api/v1/user-agents/status 테스트 정리 (Tidy)`

---

### 2️⃣5️⃣ TriggerCrawlingRequest DTO 구현 (Cycle 25)

#### 🔴 Red: 테스트 작성
- [ ] `TriggerCrawlingRequestTest.java` 생성
- [ ] `shouldCreateRequestWithSellerId()` 작성
- [ ] Validation 테스트 (@NotBlank)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: TriggerCrawlingRequest DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `TriggerCrawlingRequest.java` 생성 (Record)
- [ ] 필드: sellerId
- [ ] Bean Validation: `@NotBlank`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: TriggerCrawlingRequest DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: TriggerCrawlingRequest DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `TriggerCrawlingRequestFixture.java` 생성
- [ ] 커밋: `test: TriggerCrawlingRequestFixture 정리 (Tidy)`

---

### 2️⃣6️⃣ CrawlingTriggeredResponse DTO 구현 (Cycle 26)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlingTriggeredResponseTest.java` 생성
- [ ] `shouldCreateResponseWithTaskCount()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: CrawlingTriggeredResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlingTriggeredResponse.java` 생성 (Record)
- [ ] 필드: taskCount
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlingTriggeredResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlingTriggeredResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `CrawlingTriggeredResponseFixture.java` 생성
- [ ] 커밋: `test: CrawlingTriggeredResponseFixture 정리 (Tidy)`

---

### 2️⃣7️⃣ POST /api/internal/crawling/trigger - 크롤링 트리거 (Cycle 27)

#### 🔴 Red: 테스트 작성
- [ ] `InternalCrawlingApiControllerTest.java` 생성
- [ ] `shouldTriggerCrawling200OK()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: POST /api/internal/crawling/trigger 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `InternalCrawlingApiController.java` 생성 (`@RestController`, `@RequestMapping("/api/internal/crawling")`)
- [ ] TriggerCrawlingUseCase 주입
- [ ] `@PostMapping("/trigger")` 메서드 구현
- [ ] `@Valid @RequestBody TriggerCrawlingRequest` 받기
- [ ] TriggerCrawlingCommand 생성 → UseCase 호출
- [ ] ResponseEntity.ok(response) 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: POST /api/internal/crawling/trigger 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: POST /api/internal/crawling/trigger 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Request/Response Fixture 사용
- [ ] 커밋: `test: POST /api/internal/crawling/trigger 테스트 정리 (Tidy)`

---

### 2️⃣8️⃣ API Key 인증 필터 구현 (Cycle 28)

#### 🔴 Red: 테스트 작성
- [ ] `ApiKeyAuthenticationFilterTest.java` 생성
- [ ] `shouldAuthenticateWithValidApiKey()` 작성
- [ ] `shouldReject401UnauthorizedWithInvalidApiKey()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: API Key 인증 필터 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ApiKeyAuthenticationFilter.java` 생성 (`OncePerRequestFilter`)
- [ ] X-API-Key 헤더 검증
- [ ] `/api/internal/**` 경로에만 적용
- [ ] 유효한 API Key → 요청 통과
- [ ] 잘못된 API Key → 401 Unauthorized 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: API Key 인증 필터 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] API Key 설정 외부화 (`@Value("${api.internal.key}")`)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: API Key 인증 필터 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] API Key Test Fixture 생성
- [ ] 커밋: `test: API Key 인증 필터 테스트 정리 (Tidy)`

---

### 2️⃣9️⃣ POST /api/internal/crawling/trigger - API Key 인증 테스트 (Cycle 29)

#### 🔴 Red: 테스트 작성
- [ ] `shouldReject401UnauthorizedWithoutApiKey()` 작성
- [ ] X-API-Key 헤더 없음 → 401 Unauthorized 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: POST /api/internal/crawling/trigger API Key 인증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ApiKeyAuthenticationFilter 적용 확인
- [ ] SecurityFilterChain에 필터 등록
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: POST /api/internal/crawling/trigger API Key 인증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Security Config 정리
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: POST /api/internal/crawling/trigger API Key 인증 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] API Key Header Fixture 생성
- [ ] 커밋: `test: POST /api/internal/crawling/trigger API Key 인증 테스트 정리 (Tidy)`

---

### 3️⃣0️⃣ JWT 인증 설정 (Cycle 30)

#### 🔴 Red: 테스트 작성
- [ ] `JwtAuthenticationTest.java` 생성
- [ ] `shouldAuthenticateWithValidJwt()` 작성
- [ ] `shouldReject401UnauthorizedWithoutJwt()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: JWT 인증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SecurityConfig.java` 생성
- [ ] `/api/v1/**` 경로에 JWT 인증 적용
- [ ] `/api/internal/**` 경로는 permitAll
- [ ] OAuth2 Resource Server 설정 (JWT)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: JWT 인증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] JWT 설정 외부화 (application.yml)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: JWT 인증 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] JWT Token Fixture 생성
- [ ] 커밋: `test: JWT 인증 테스트 정리 (Tidy)`

---

### 3️⃣1️⃣ POST /api/v1/sellers - JWT 인증 테스트 (Cycle 31)

#### 🔴 Red: 테스트 작성
- [ ] `shouldReject401UnauthorizedWithoutJwt()` 작성
- [ ] Authorization 헤더 없음 → 401 Unauthorized 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: POST /api/v1/sellers JWT 인증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] SecurityConfig 적용 확인
- [ ] `/api/v1/sellers` 경로에 JWT 인증 필요
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: POST /api/v1/sellers JWT 인증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Security Config 정리
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: POST /api/v1/sellers JWT 인증 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] JWT Bearer Token Fixture 생성
- [ ] 커밋: `test: POST /api/v1/sellers JWT 인증 테스트 정리 (Tidy)`

---

### 3️⃣2️⃣ ArchUnit 테스트 - RESTful 설계 검증 (Cycle 32)

#### 🔴 Red: 테스트 작성
- [ ] `RestApiArchUnitTest.java` 생성
- [ ] `shouldFollowRestfulUrlDesign()` 작성
- [ ] 리소스 기반 URL 검증 (동작 기반 URL 금지)
- [ ] 테스트 실행 → 통과 확인 (이미 준수 중)
- [ ] 커밋: `test: RESTful 설계 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] `@RequestMapping` 값 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: RESTful 설계 ArchUnit 테스트 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: RESTful 설계 ArchUnit 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ArchUnit 테스트 정리
- [ ] 커밋: `test: RESTful 설계 ArchUnit 테스트 정리 (Tidy)`

---

### 3️⃣3️⃣ ArchUnit 테스트 - Controller 규칙 검증 (Cycle 33)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFollowControllerNamingConvention()` 작성
- [ ] Controller는 반드시 `@RestController` 어노테이션 필수
- [ ] Controller 클래스명은 반드시 `Controller`로 끝남
- [ ] 테스트 실행 → 통과 확인 (이미 준수 중)
- [ ] 커밋: `test: Controller 규칙 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] `classes().that().haveSimpleNameEndingWith("Controller").should().beAnnotatedWith(RestController.class)`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Controller 규칙 ArchUnit 테스트 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Controller 규칙 ArchUnit 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ArchUnit 테스트 정리
- [ ] 커밋: `test: Controller 규칙 ArchUnit 테스트 정리 (Tidy)`

---

### 3️⃣4️⃣ ArchUnit 테스트 - DTO 규칙 검증 (Cycle 34)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFollowDtoNamingConvention()` 작성
- [ ] Request DTO는 반드시 `Request`로 끝남
- [ ] Response DTO는 반드시 `Response`로 끝남
- [ ] 모든 DTO는 Record 타입
- [ ] 테스트 실행 → 통과 확인 (이미 준수 중)
- [ ] 커밋: `test: DTO 규칙 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] `classes().that().haveSimpleNameEndingWith("Request").should().beRecords()`
- [ ] `classes().that().haveSimpleNameEndingWith("Response").should().beRecords()`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: DTO 규칙 ArchUnit 테스트 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: DTO 규칙 ArchUnit 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ArchUnit 테스트 정리
- [ ] 커밋: `test: DTO 규칙 ArchUnit 테스트 정리 (Tidy)`

---

### 3️⃣5️⃣ ArchUnit 테스트 - Validation 규칙 검증 (Cycle 35)

#### 🔴 Red: 테스트 작성
- [ ] `shouldUseValidationAnnotations()` 작성
- [ ] Request DTO는 반드시 Bean Validation 어노테이션 사용
- [ ] Controller 메서드는 `@Valid` 또는 `@Validated` 사용
- [ ] 테스트 실행 → 통과 확인 (이미 준수 중)
- [ ] 커밋: `test: Validation 규칙 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] Bean Validation 어노테이션 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Validation 규칙 ArchUnit 테스트 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Validation 규칙 ArchUnit 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ArchUnit 테스트 정리
- [ ] 커밋: `test: Validation 규칙 ArchUnit 테스트 정리 (Tidy)`

---

### 3️⃣6️⃣ 나머지 Request/Response DTO 구현 (Cycle 36-40)

**Note**: 아래 DTO들은 이미 Application Layer Plan에서 정의되었을 가능성이 높지만, REST API Layer에서 재확인 및 통합 테스트를 위해 추가합니다.

#### Cycle 36: SellerDetailResponse DTO
- [ ] Red → Green → Refactor → Tidy

#### Cycle 37: SellerSummaryResponse DTO
- [ ] Red → Green → Refactor → Tidy

#### Cycle 38: UpdateSellerStatusRequest DTO (Activate/Deactivate 공통)
- [ ] Red → Green → Refactor → Tidy

#### Cycle 39: REST API 통합 테스트 - 성공 시나리오
- [ ] Red → Green → Refactor → Tidy
- [ ] 모든 엔드포인트 성공 시나리오 통합 테스트

#### Cycle 40: REST API 통합 테스트 - 실패 시나리오
- [ ] Red → Green → Refactor → Tidy
- [ ] 모든 엔드포인트 실패 시나리오 (400, 401, 404, 409, 500) 통합 테스트

---

---

## 📅 스케줄러 관리 API (Cycle 41-72)

### 4️⃣1️⃣ CrawlingScheduleResponse DTO 구현 (Cycle 41)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlingScheduleResponseTest.java` 생성
- [ ] `shouldCreateResponseFromApplication()` 작성
- [ ] 모든 필드 검증 (scheduleId, sellerId, scheduleExpression, isActive, lastExecutionAt, createdAt, updatedAt)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: CrawlingScheduleResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlingScheduleResponse.java` 생성 (Record)
- [ ] 필드: scheduleId, sellerId, scheduleExpression, isActive, lastExecutionAt, createdAt, updatedAt
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: CrawlingScheduleResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Response DTO 규칙)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: CrawlingScheduleResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `CrawlingScheduleResponseFixture.java` 생성
- [ ] 커밋: `test: CrawlingScheduleResponseFixture 정리 (Tidy)`

---

### 4️⃣2️⃣ ScheduleExecutionResponse DTO 구현 (Cycle 42)

#### 🔴 Red: 테스트 작성
- [ ] `ScheduleExecutionResponseTest.java` 생성
- [ ] `shouldCreateResponseWithExecutionDetails()` 작성
- [ ] 필드 검증: executionId, scheduleId, sellerId, status, totalTasksCreated, completedTasks, failedTasks, progressRate, successRate, startedAt, completedAt, createdAt
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ScheduleExecutionResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ScheduleExecutionResponse.java` 생성 (Record)
- [ ] 모든 필드 정의 (Tell Don't Ask: progressRate, successRate 포함)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ScheduleExecutionResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ScheduleExecutionResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ScheduleExecutionResponseFixture.java` 생성
- [ ] 커밋: `test: ScheduleExecutionResponseFixture 정리 (Tidy)`

---

### 4️⃣3️⃣ SchedulerOutboxStatusResponse DTO 구현 (Cycle 43)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerOutboxStatusResponseTest.java` 생성
- [ ] `shouldCreateResponseWithOutboxStatus()` 작성
- [ ] 필드 검증: waitingCount, sendingCount, completedCount, failedCount, totalCount
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SchedulerOutboxStatusResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerOutboxStatusResponse.java` 생성 (Record)
- [ ] 필드: waitingCount, sendingCount, completedCount, failedCount, totalCount
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SchedulerOutboxStatusResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SchedulerOutboxStatusResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SchedulerOutboxStatusResponseFixture.java` 생성
- [ ] 커밋: `test: SchedulerOutboxStatusResponseFixture 정리 (Tidy)`

---

### 4️⃣4️⃣ ListSchedulesRequest DTO 구현 (Cycle 44)

#### 🔴 Red: 테스트 작성
- [ ] `ListSchedulesRequestTest.java` 생성
- [ ] `shouldCreateRequestWithPaging()` 작성
- [ ] Validation 테스트 (isActive nullable, page/size 검증)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ListSchedulesRequest DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ListSchedulesRequest.java` 생성 (Record)
- [ ] 필드: isActive (Nullable), page, size
- [ ] Bean Validation: `@Min(0)` (page), `@Min(1)` (size), `@Max(100)` (size)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ListSchedulesRequest DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ListSchedulesRequest DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ListSchedulesRequestFixture.java` 생성
- [ ] 커밋: `test: ListSchedulesRequestFixture 정리 (Tidy)`

---

### 4️⃣5️⃣ GET /api/v1/admin/schedules - 스케줄 목록 조회 (Cycle 45)

#### 🔴 Red: 테스트 작성
- [ ] `AdminScheduleApiControllerTest.java` 생성 (@SpringBootTest + TestRestTemplate)
- [ ] `shouldListSchedulesWithPaging200OK()` 작성
- [ ] Query Parameters: isActive, page, size
- [ ] TestRestTemplate.getForEntity() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/admin/schedules 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `AdminScheduleApiController.java` 생성 (`@RestController`, `@RequestMapping("/api/v1/admin/schedules")`)
- [ ] ListCrawlingSchedulesUseCase 주입 (Application Layer에서 구현)
- [ ] `@GetMapping` 메서드 구현
- [ ] `@Valid ListSchedulesRequest` 받기 (Query Parameters)
- [ ] ListCrawlingSchedulesQuery 생성 → UseCase 호출
- [ ] PageResponse<CrawlingScheduleResponse> 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/admin/schedules 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 페이징 기본값 설정 (page=0, size=20)
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/admin/schedules 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Request/Response Fixture 사용
- [ ] 커밋: `test: GET /api/v1/admin/schedules 테스트 정리 (Tidy)`

---

### 4️⃣6️⃣ GET /api/v1/admin/schedules/{scheduleId} - 스케줄 상세 조회 (Cycle 46)

#### 🔴 Red: 테스트 작성
- [ ] `shouldGetScheduleDetail200OK()` 작성
- [ ] PathVariable: scheduleId (UUID)
- [ ] TestRestTemplate.getForEntity() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/admin/schedules/{scheduleId} 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] GetCrawlingScheduleUseCase 주입 (MUSTIT-002 Cycle 52에서 구현)
- [ ] `@GetMapping("/{scheduleId}")` 메서드 구현
- [ ] `@PathVariable UUID scheduleId` 받기
- [ ] GetCrawlingScheduleQuery 생성 → UseCase 호출
- [ ] ResponseEntity.ok(response) 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/admin/schedules/{scheduleId} 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/admin/schedules/{scheduleId} 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Query/Response Fixture 사용
- [ ] 커밋: `test: GET /api/v1/admin/schedules/{scheduleId} 테스트 정리 (Tidy)`

---

### 4️⃣7️⃣ GET /api/v1/admin/schedules/{scheduleId} - 404 테스트 (Cycle 47)

#### 🔴 Red: 테스트 작성
- [ ] `shouldReturn404WhenScheduleNotFound()` 작성
- [ ] 존재하지 않는 scheduleId → 404 Not Found 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/admin/schedules/{scheduleId} 404 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] CrawlingScheduleNotFoundException 발생 시나리오 (UseCase에서)
- [ ] GlobalExceptionHandler에 CrawlingScheduleNotFoundException 핸들러 추가
- [ ] 404 Not Found 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/admin/schedules/{scheduleId} 404 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 에러 메시지 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/admin/schedules/{scheduleId} 404 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Not Found Scenario Fixture 생성
- [ ] 커밋: `test: GET /api/v1/admin/schedules/{scheduleId} 404 테스트 정리 (Tidy)`

---

### 4️⃣8️⃣ ScheduleStatusResponse DTO 구현 (Cycle 48)

#### 🔴 Red: 테스트 작성
- [ ] `ScheduleStatusResponseTest.java` 생성
- [ ] `shouldCreateResponseWithScheduleStatus()` 작성
- [ ] 필드 검증: scheduleId, isActive, totalExecutions, completedExecutions, failedExecutions, lastExecutionAt, nextExecutionAt
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ScheduleStatusResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ScheduleStatusResponse.java` 생성 (Record)
- [ ] 모든 필드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ScheduleStatusResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ScheduleStatusResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ScheduleStatusResponseFixture.java` 생성
- [ ] 커밋: `test: ScheduleStatusResponseFixture 정리 (Tidy)`

---

### 4️⃣9️⃣ GET /api/v1/admin/schedules/{scheduleId}/status - 스케줄 상태 조회 (Cycle 49)

#### 🔴 Red: 테스트 작성
- [ ] `shouldGetScheduleStatus200OK()` 작성
- [ ] PathVariable: scheduleId (UUID)
- [ ] 실행 통계 포함 (총 실행 수, 완료/실패 수, 마지막 실행 시간)
- [ ] TestRestTemplate.getForEntity() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/admin/schedules/{scheduleId}/status 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] GetCrawlingScheduleStatusUseCase 주입 (Application Layer에서 구현 필요)
- [ ] `@GetMapping("/{scheduleId}/status")` 메서드 구현
- [ ] `@PathVariable UUID scheduleId` 받기
- [ ] GetCrawlingScheduleStatusQuery 생성 → UseCase 호출
- [ ] ResponseEntity.ok(response) 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/admin/schedules/{scheduleId}/status 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/admin/schedules/{scheduleId}/status 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Query/Response Fixture 사용
- [ ] 커밋: `test: GET /api/v1/admin/schedules/{scheduleId}/status 테스트 정리 (Tidy)`

---

### 5️⃣0️⃣ ListScheduleExecutionsRequest DTO 구현 (Cycle 50)

#### 🔴 Red: 테스트 작성
- [ ] `ListScheduleExecutionsRequestTest.java` 생성
- [ ] `shouldCreateRequestWithPaging()` 작성
- [ ] Validation 테스트 (status nullable, page/size 검증)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ListScheduleExecutionsRequest DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ListScheduleExecutionsRequest.java` 생성 (Record)
- [ ] 필드: status (Nullable), page, size
- [ ] Bean Validation: `@Min(0)` (page), `@Min(1)` (size), `@Max(100)` (size)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ListScheduleExecutionsRequest DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ListScheduleExecutionsRequest DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ListScheduleExecutionsRequestFixture.java` 생성
- [ ] 커밋: `test: ListScheduleExecutionsRequestFixture 정리 (Tidy)`

---

### 5️⃣1️⃣ GET /api/v1/admin/schedules/{scheduleId}/executions - 실행 이력 조회 (Cycle 51)

#### 🔴 Red: 테스트 작성
- [ ] `shouldListScheduleExecutions200OK()` 작성
- [ ] PathVariable: scheduleId (UUID)
- [ ] Query Parameters: status, page, size
- [ ] TestRestTemplate.getForEntity() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/admin/schedules/{scheduleId}/executions 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ListScheduleExecutionsUseCase 주입 (Application Layer에서 구현 필요)
- [ ] `@GetMapping("/{scheduleId}/executions")` 메서드 구현
- [ ] `@PathVariable UUID scheduleId`, `@Valid ListScheduleExecutionsRequest` 받기
- [ ] ListScheduleExecutionsQuery 생성 → UseCase 호출
- [ ] PageResponse<ScheduleExecutionResponse> 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/admin/schedules/{scheduleId}/executions 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 페이징 기본값 설정 (page=0, size=20)
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/admin/schedules/{scheduleId}/executions 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Request/Response Fixture 사용
- [ ] 커밋: `test: GET /api/v1/admin/schedules/{scheduleId}/executions 테스트 정리 (Tidy)`

---

### 5️⃣2️⃣ GET /api/v1/admin/executions/{executionId} - 실행 상세 조회 (Cycle 52)

#### 🔴 Red: 테스트 작성
- [ ] `AdminExecutionApiControllerTest.java` 생성
- [ ] `shouldGetExecutionDetail200OK()` 작성
- [ ] PathVariable: executionId (UUID)
- [ ] 진행률/성공률 포함 (Tell Don't Ask)
- [ ] TestRestTemplate.getForEntity() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/admin/executions/{executionId} 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `AdminExecutionApiController.java` 생성 (`@RestController`, `@RequestMapping("/api/v1/admin/executions")`)
- [ ] GetScheduleExecutionUseCase 주입 (Application Layer에서 구현 필요)
- [ ] `@GetMapping("/{executionId}")` 메서드 구현
- [ ] `@PathVariable UUID executionId` 받기
- [ ] GetScheduleExecutionQuery 생성 → UseCase 호출
- [ ] ResponseEntity.ok(response) 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/admin/executions/{executionId} 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/admin/executions/{executionId} 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Query/Response Fixture 사용
- [ ] 커밋: `test: GET /api/v1/admin/executions/{executionId} 테스트 정리 (Tidy)`

---

### 5️⃣3️⃣ GET /api/v1/admin/executions/{executionId} - 404 테스트 (Cycle 53)

#### 🔴 Red: 테스트 작성
- [ ] `shouldReturn404WhenExecutionNotFound()` 작성
- [ ] 존재하지 않는 executionId → 404 Not Found 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/admin/executions/{executionId} 404 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] CrawlingScheduleExecutionNotFoundException 발생 시나리오 (UseCase에서)
- [ ] GlobalExceptionHandler에 CrawlingScheduleExecutionNotFoundException 핸들러 추가
- [ ] 404 Not Found 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/admin/executions/{executionId} 404 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 에러 메시지 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/admin/executions/{executionId} 404 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Not Found Scenario Fixture 생성
- [ ] 커밋: `test: GET /api/v1/admin/executions/{executionId} 404 테스트 정리 (Tidy)`

---

### 5️⃣4️⃣ OutboxDetailResponse DTO 구현 (Cycle 54)

#### 🔴 Red: 테스트 작성
- [ ] `OutboxDetailResponseTest.java` 생성
- [ ] `shouldCreateResponseWithOutboxDetail()` 작성
- [ ] 필드 검증: outboxId, scheduleId, eventType, payload, status, retryCount, errorMessage, createdAt, updatedAt
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: OutboxDetailResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `OutboxDetailResponse.java` 생성 (Record)
- [ ] 모든 필드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: OutboxDetailResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: OutboxDetailResponse DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `OutboxDetailResponseFixture.java` 생성
- [ ] 커밋: `test: OutboxDetailResponseFixture 정리 (Tidy)`

---

### 5️⃣5️⃣ GET /api/v1/admin/scheduler/outbox/status - Outbox 상태 조회 (Cycle 55)

#### 🔴 Red: 테스트 작성
- [ ] `AdminSchedulerApiControllerTest.java` 생성
- [ ] `shouldGetOutboxStatus200OK()` 작성
- [ ] 상태별 카운트 검증 (WAITING, SENDING, COMPLETED, FAILED)
- [ ] TestRestTemplate.getForEntity() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/admin/scheduler/outbox/status 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `AdminSchedulerApiController.java` 생성 (`@RestController`, `@RequestMapping("/api/v1/admin/scheduler")`)
- [ ] GetSchedulerOutboxStatusUseCase 주입 (MUSTIT-002 Cycle 66에서 구현)
- [ ] `@GetMapping("/outbox/status")` 메서드 구현
- [ ] GetSchedulerOutboxStatusQuery 생성 → UseCase 호출
- [ ] ResponseEntity.ok(response) 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/admin/scheduler/outbox/status 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/admin/scheduler/outbox/status 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Response Fixture 사용
- [ ] 커밋: `test: GET /api/v1/admin/scheduler/outbox/status 테스트 정리 (Tidy)`

---

### 5️⃣6️⃣ ListOutboxRequest DTO 구현 (Cycle 56)

#### 🔴 Red: 테스트 작성
- [ ] `ListOutboxRequestTest.java` 생성
- [ ] `shouldCreateRequestWithPaging()` 작성
- [ ] Validation 테스트 (status nullable, page/size 검증)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: ListOutboxRequest DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ListOutboxRequest.java` 생성 (Record)
- [ ] 필드: status (Nullable), page, size
- [ ] Bean Validation: `@Min(0)` (page), `@Min(1)` (size), `@Max(100)` (size)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: ListOutboxRequest DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: ListOutboxRequest DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ListOutboxRequestFixture.java` 생성
- [ ] 커밋: `test: ListOutboxRequestFixture 정리 (Tidy)`

---

### 5️⃣7️⃣ GET /api/v1/admin/scheduler/outbox - Outbox 목록 조회 (Cycle 57)

#### 🔴 Red: 테스트 작성
- [ ] `shouldListOutbox200OK()` 작성
- [ ] Query Parameters: status, page, size
- [ ] TestRestTemplate.getForEntity() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: GET /api/v1/admin/scheduler/outbox 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ListSchedulerOutboxUseCase 주입 (Application Layer에서 구현 필요)
- [ ] `@GetMapping("/outbox")` 메서드 구현
- [ ] `@Valid ListOutboxRequest` 받기 (Query Parameters)
- [ ] ListSchedulerOutboxQuery 생성 → UseCase 호출
- [ ] PageResponse<OutboxDetailResponse> 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: GET /api/v1/admin/scheduler/outbox 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 페이징 기본값 설정 (page=0, size=20)
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: GET /api/v1/admin/scheduler/outbox 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Request/Response Fixture 사용
- [ ] 커밋: `test: GET /api/v1/admin/scheduler/outbox 테스트 정리 (Tidy)`

---

### 5️⃣8️⃣ RetryFailedOutboxRequest DTO 구현 (Cycle 58)

#### 🔴 Red: 테스트 작성
- [ ] `RetryFailedOutboxRequestTest.java` 생성
- [ ] `shouldCreateRequestWithOutboxId()` 작성
- [ ] Validation 테스트 (@NotNull)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: RetryFailedOutboxRequest DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RetryFailedOutboxRequest.java` 생성 (Record)
- [ ] 필드: outboxId (UUID)
- [ ] Bean Validation: `@NotNull`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: RetryFailedOutboxRequest DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: RetryFailedOutboxRequest DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `RetryFailedOutboxRequestFixture.java` 생성
- [ ] 커밋: `test: RetryFailedOutboxRequestFixture 정리 (Tidy)`

---

### 5️⃣9️⃣ POST /api/v1/admin/scheduler/outbox/{outboxId}/retry - Outbox 수동 재시도 (Cycle 59)

#### 🔴 Red: 테스트 작성
- [ ] `shouldRetryFailedOutbox200OK()` 작성
- [ ] PathVariable: outboxId (UUID)
- [ ] FAILED 상태 Outbox만 재시도 가능
- [ ] TestRestTemplate.postForEntity() 사용
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: POST /api/v1/admin/scheduler/outbox/{outboxId}/retry 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] RetryFailedOutboxUseCase 주입 (MUSTIT-002 Cycle 67에서 구현)
- [ ] `@PostMapping("/outbox/{outboxId}/retry")` 메서드 구현
- [ ] `@PathVariable UUID outboxId` 받기
- [ ] RetryFailedOutboxCommand 생성 → UseCase 호출
- [ ] ResponseEntity.ok() 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: POST /api/v1/admin/scheduler/outbox/{outboxId}/retry 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] RESTful 설계 검증
- [ ] ArchUnit 테스트 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: POST /api/v1/admin/scheduler/outbox/{outboxId}/retry 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Request/Response Fixture 사용
- [ ] 커밋: `test: POST /api/v1/admin/scheduler/outbox/{outboxId}/retry 테스트 정리 (Tidy)`

---

### 6️⃣0️⃣ POST /api/v1/admin/scheduler/outbox/{outboxId}/retry - 예외 테스트 (Cycle 60)

#### 🔴 Red: 테스트 작성
- [ ] `shouldReject400WhenOutboxNotFailed()` 작성
- [ ] WAITING/SENDING/COMPLETED 상태에서 재시도 → 400 Bad Request
- [ ] `shouldReturn404WhenOutboxNotFound()` 작성
- [ ] 존재하지 않는 outboxId → 404 Not Found 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: POST /api/v1/admin/scheduler/outbox/{outboxId}/retry 예외 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] SchedulerOutboxInvalidStateException 발생 시나리오 (UseCase에서)
- [ ] GlobalExceptionHandler에 SchedulerOutboxInvalidStateException 핸들러 추가 (400 Bad Request)
- [ ] SchedulerOutboxNotFoundException 핸들러 추가 (404 Not Found)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: POST /api/v1/admin/scheduler/outbox/{outboxId}/retry 예외 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 에러 메시지 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: POST /api/v1/admin/scheduler/outbox/{outboxId}/retry 예외 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Exception Scenario Fixture 생성
- [ ] 커밋: `test: POST /api/v1/admin/scheduler/outbox/{outboxId}/retry 예외 테스트 정리 (Tidy)`

---

### 6️⃣1️⃣ ArchUnit 테스트 - 스케줄러 API 규칙 검증 (Cycle 61)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerApiArchUnitTest.java` 생성
- [ ] `shouldFollowAdminApiNamingConvention()` 작성
- [ ] `/api/v1/admin/**` 경로에만 Admin API 존재 검증
- [ ] 테스트 실행 → 통과 확인 (이미 준수 중)
- [ ] 커밋: `test: 스케줄러 API ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] Admin Controller는 `/admin` prefix 필수
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 스케줄러 API ArchUnit 테스트 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 스케줄러 API ArchUnit 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ArchUnit 테스트 정리
- [ ] 커밋: `test: 스케줄러 API ArchUnit 테스트 정리 (Tidy)`

---

### 6️⃣2️⃣ 스케줄러 API 통합 테스트 - 성공 시나리오 (Cycle 62)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerApiIntegrationTest.java` 생성
- [ ] `shouldCompleteSchedulerWorkflow()` 작성
- [ ] 시나리오: 스케줄 목록 조회 → 상세 조회 → 실행 이력 조회 → Outbox 상태 확인
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 스케줄러 API 통합 테스트 (성공) 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 모든 엔드포인트 순차 호출
- [ ] 각 단계별 응답 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 스케줄러 API 통합 테스트 (성공) 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 시나리오 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 스케줄러 API 통합 테스트 (성공) 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Integration Test Fixture 정리
- [ ] 커밋: `test: 스케줄러 API 통합 테스트 (성공) 정리 (Tidy)`

---

### 6️⃣3️⃣ 스케줄러 API 통합 테스트 - 실패 시나리오 (Cycle 63)

#### 🔴 Red: 테스트 작성
- [ ] `shouldHandleSchedulerApiFailures()` 작성
- [ ] 404: 존재하지 않는 scheduleId/executionId/outboxId
- [ ] 400: FAILED 아닌 Outbox 재시도
- [ ] 모든 실패 시나리오 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 스케줄러 API 통합 테스트 (실패) 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 모든 실패 시나리오 테스트
- [ ] ErrorResponse 검증
- [ ] HTTP Status Code 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 스케줄러 API 통합 테스트 (실패) 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 시나리오 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 스케줄러 API 통합 테스트 (실패) 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Failure Scenario Fixture 정리
- [ ] 커밋: `test: 스케줄러 API 통합 테스트 (실패) 정리 (Tidy)`

---

### 6️⃣4️⃣ JWT 인증 - 스케줄러 관리 API 테스트 (Cycle 64)

#### 🔴 Red: 테스트 작성
- [ ] `shouldRequireJwtForAdminSchedulerApis()` 작성
- [ ] Authorization 헤더 없음 → 401 Unauthorized 검증
- [ ] 모든 `/api/v1/admin/schedules/**` 경로 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 스케줄러 관리 API JWT 인증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] SecurityConfig 적용 확인
- [ ] `/api/v1/admin/**` 경로에 JWT 인증 필요
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 스케줄러 관리 API JWT 인증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Security Config 정리
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 스케줄러 관리 API JWT 인증 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] JWT Bearer Token Fixture 재사용
- [ ] 커밋: `test: 스케줄러 관리 API JWT 인증 테스트 정리 (Tidy)`

---

### 6️⃣5️⃣-7️⃣2️⃣ 추가 Response DTO 및 예외 핸들러 구현 (Cycle 65-72)

**Note**: 나머지 사이클은 스케줄러 관련 세부 DTO 및 예외 처리를 보완합니다.

#### Cycle 65: ExecutionTaskStatsResponse DTO (중첩 DTO)
- [ ] Red → Green → Refactor → Tidy
- [ ] 필드: total, completed, failed, inProgress

#### Cycle 66: ScheduleEventHistoryResponse DTO
- [ ] Red → Green → Refactor → Tidy
- [ ] EventBridge 이벤트 이력 (Outbox 기반)

#### Cycle 67: GlobalExceptionHandler - CrawlingScheduleNotFoundException
- [ ] Red → Green → Refactor → Tidy

#### Cycle 68: GlobalExceptionHandler - CrawlingScheduleExecutionNotFoundException
- [ ] Red → Green → Refactor → Tidy

#### Cycle 69: GlobalExceptionHandler - SchedulerOutboxNotFoundException
- [ ] Red → Green → Refactor → Tidy

#### Cycle 70: GlobalExceptionHandler - SchedulerOutboxInvalidStateException
- [ ] Red → Green → Refactor → Tidy

#### Cycle 71: 스케줄러 API 성능 테스트
- [ ] Red → Green → Refactor → Tidy
- [ ] 페이징 성능 (1000개 데이터)

#### Cycle 72: 스케줄러 API 최종 통합 검증
- [ ] Red → Green → Refactor → Tidy
- [ ] 모든 엔드포인트 통합 검증
- [ ] TestRestTemplate 사용
- [ ] ArchUnit 규칙 통과

---

## ✅ 완료 조건

- [ ] 72개 TDD 사이클 모두 완료 (288개 체크박스 모두 ✅)
- [ ] 모든 테스트 통과 (TestRestTemplate, Integration Test)
- [ ] ArchUnit 테스트 통과 (RESTful 설계, Controller 규칙, DTO 규칙, Validation 규칙, 스케줄러 API 규칙)
- [ ] Zero-Tolerance 규칙 준수
  - [ ] RESTful 설계 원칙 (리소스 기반 URL)
  - [ ] 일관된 Error Response 형식
  - [ ] Validation 필수 (@Valid, @Validated)
  - [ ] TestRestTemplate 사용 (MockMvc 금지)
  - [ ] 스케줄러 관리 API는 `/api/v1/admin/**` prefix 사용
- [ ] JWT 인증/인가 구현 완료 (관리 API)
- [ ] API Key 인증 구현 완료 (내부 API)
- [ ] 스케줄러 관리 API 완료
  - [ ] CrawlingSchedule 조회/상태/이력 API
  - [ ] CrawlingScheduleExecution 조회 API
  - [ ] SchedulerOutbox 상태/목록/재시도 API
- [ ] TestFixture 모두 정리 (Object Mother 패턴)
- [ ] 테스트 커버리지 > 80%

---

## 🔗 관련 문서

- Task: docs/prd/tasks/MUSTIT-004.md
- PRD: docs/prd/mustit-seller-crawler.md
- REST API Layer 규칙: docs/coding_convention/01-adapter-rest-api-layer/

---

## 📚 참고사항

### Controller 예시

```java
@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerApiController {
    private final RegisterSellerUseCase registerSellerUseCase;

    @PostMapping
    public ResponseEntity<SellerResponse> registerSeller(
        @Valid @RequestBody RegisterSellerRequest request) {

        RegisterSellerCommand command = new RegisterSellerCommand(
            request.sellerId(),
            request.name(),
            request.crawlingIntervalDays()
        );

        SellerResponse response = registerSellerUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### GlobalExceptionHandler 예시

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SellerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSellerNotFound(
        SellerNotFoundException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
            "SELLER_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
}
```

### TestRestTemplate 예시

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SellerApiControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerSeller_success() {
        // Given
        RegisterSellerRequest request = new RegisterSellerRequest(
            "seller_123",
            "셀러명",
            1
        );

        // When
        ResponseEntity<SellerResponse> response = restTemplate.postForEntity(
            "/api/v1/sellers",
            request,
            SellerResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().sellerId()).isEqualTo("seller_123");
    }
}
```
