# API Response 구조 마이그레이션 가이드

> **PR #90**: `refactor: ApiResponse 구조 개선 및 RFC 7807 에러 응답 통일`

이 문서는 API 응답 구조 변경에 따른 클라이언트 마이그레이션 가이드입니다.

---

## 변경 요약

| 항목 | Before | After |
|------|--------|-------|
| 성공 응답 | `success`, `data`, `error`, `timestamp`, `requestId` | `data`, `timestamp`, `requestId` |
| 에러 응답 | 동일 구조 + `error` 필드 | RFC 7807 ProblemDetail |
| `success` 필드 | ✅ 존재 | ❌ 제거 |
| `error` 필드 | ✅ `ErrorInfo` 객체 | ❌ 제거 (별도 형식) |
| `timestamp` 타입 | `LocalDateTime` | ISO 8601 `String` |
| `requestId` 형식 | `"req-123456"` | UUID `"550e8400-..."` |
| 에러 표준 | 자체 형식 | RFC 7807 |

---

## 1. 성공 응답 변경

### Before (이전)

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "상품명"
  },
  "error": null,
  "timestamp": "2025-10-23T10:30:00",
  "requestId": "req-123456"
}
```

### After (이후)

```json
{
  "data": {
    "id": 1,
    "name": "상품명"
  },
  "timestamp": "2025-12-22T10:30:00",
  "requestId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 클라이언트 수정 사항

```javascript
// Before
if (response.success) {
  const data = response.data;
}

// After - HTTP Status Code로 판단
if (response.status >= 200 && response.status < 300) {
  const data = response.data.data; // wrapper의 data 필드
}

// 또는 간단히
const { data } = await fetch('/api/...').then(r => r.json());
```

---

## 2. 에러 응답 변경 (RFC 7807)

### Before (이전)

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "사용자를 찾을 수 없습니다"
  },
  "timestamp": "2025-10-23T10:30:00",
  "requestId": "req-123456"
}
```

### After (이후) - RFC 7807 ProblemDetail

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "사용자를 찾을 수 없습니다",
  "instance": "/api/v1/users/123",
  "timestamp": "2025-12-22T10:30:00",
  "code": "USER_NOT_FOUND",
  "traceId": "abc123",
  "spanId": "def456"
}
```

### Validation 에러 (errors 배열 포함)

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed for request",
  "instance": "/api/v1/users",
  "timestamp": "2025-12-22T10:30:00",
  "code": "VALIDATION_FAILED",
  "errors": {
    "email": "올바른 이메일 형식이 아닙니다",
    "name": "이름은 필수입니다"
  }
}
```

### 클라이언트 수정 사항

```javascript
// Before
if (!response.success) {
  console.error(response.error.code, response.error.message);
}

// After
if (response.status >= 400) {
  const errorBody = await response.json();
  console.error(errorBody.code, errorBody.detail);

  // Validation 에러 처리
  if (errorBody.errors) {
    Object.entries(errorBody.errors).forEach(([field, message]) => {
      console.error(`${field}: ${message}`);
    });
  }
}
```

---

## 3. RFC 7807 필드 설명

| 필드 | 설명 | 필수 |
|------|------|------|
| `type` | 에러 유형 URI (현재 `about:blank`) | ✅ |
| `title` | HTTP 상태 이름 (예: "Bad Request") | ✅ |
| `status` | HTTP 상태 코드 (예: 400) | ✅ |
| `detail` | 상세 에러 메시지 | ✅ |
| `instance` | 요청 URI (쿼리스트링 포함) | ✅ |
| `timestamp` | ISO 8601 형식 타임스탬프 | ✅ (확장) |
| `code` | 도메인 에러 코드 (예: "VALIDATION_FAILED") | ⚠️ (선택) |
| `traceId` | 분산 추적 ID (MDC 기반) | ⚠️ (존재시) |
| `spanId` | 스팬 ID (MDC 기반) | ⚠️ (존재시) |
| `errors` | 필드별 유효성 검증 에러 | ⚠️ (검증시) |
| `args` | 도메인 예외 추가 인자 | ⚠️ (존재시) |

---

## 4. 타입스크립트 타입 정의

### 성공 응답 타입

```typescript
interface ApiResponse<T> {
  data: T;
  timestamp: string;  // ISO 8601
  requestId: string;  // UUID
}
```

### 에러 응답 타입 (RFC 7807)

```typescript
interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  timestamp: string;
  code?: string;
  traceId?: string;
  spanId?: string;
  errors?: Record<string, string>;
  args?: Record<string, unknown>;
}
```

### 공통 응답 처리 유틸

```typescript
async function apiCall<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, options);

  if (!response.ok) {
    const error: ProblemDetail = await response.json();
    throw new ApiError(error);
  }

  const result: ApiResponse<T> = await response.json();
  return result.data;
}

class ApiError extends Error {
  constructor(public readonly problem: ProblemDetail) {
    super(problem.detail);
    this.name = 'ApiError';
  }
}
```

---

## 5. HTTP 상태 코드 기반 분기

### 이전 방식 (success 플래그)

```javascript
const response = await api.get('/users/1');
if (response.success) {
  // 성공
} else {
  // 실패
}
```

### 새로운 방식 (HTTP Status Code)

```javascript
try {
  const response = await fetch('/api/v1/users/1');

  if (response.ok) {  // 2xx
    const { data } = await response.json();
    // 성공 처리
  } else {
    const problem = await response.json();
    // 에러 처리 (RFC 7807)
  }
} catch (networkError) {
  // 네트워크 에러
}
```

### Axios 예시

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1'
});

// 응답 인터셉터
api.interceptors.response.use(
  (response) => response.data.data,  // 성공: data 필드 추출
  (error) => {
    if (error.response) {
      // RFC 7807 에러 처리
      const problem = error.response.data;
      return Promise.reject(new ApiError(problem));
    }
    return Promise.reject(error);
  }
);
```

---

## 6. 주요 에러 코드 매핑

| HTTP Status | code | 설명 |
|-------------|------|------|
| 400 | `VALIDATION_FAILED` | 요청 본문 유효성 검증 실패 |
| 400 | `INVALID_ARGUMENT` | 잘못된 인자 |
| 400 | `INVALID_REQUEST_BODY` | JSON 파싱 실패 |
| 400 | `TYPE_MISMATCH` | 파라미터 타입 불일치 |
| 400 | `MISSING_PARAMETER` | 필수 파라미터 누락 |
| 401 | `UNAUTHORIZED` | 인증 필요 |
| 403 | `FORBIDDEN` | 접근 권한 없음 |
| 404 | `RESOURCE_NOT_FOUND` | 리소스 없음 |
| 405 | `METHOD_NOT_ALLOWED` | 지원하지 않는 메서드 |
| 409 | `STATE_CONFLICT` | 상태 충돌 |
| 500 | `INTERNAL_SERVER_ERROR` | 서버 내부 오류 |

---

## 7. 마이그레이션 체크리스트

- [ ] `success` 필드 의존 코드 제거
- [ ] `error.code`, `error.message` → `code`, `detail` 변경
- [ ] HTTP Status Code 기반 분기로 변경
- [ ] `timestamp` 파싱 로직 확인 (ISO 8601 문자열)
- [ ] `requestId` UUID 형식 확인
- [ ] Validation 에러 `errors` 필드 처리 추가
- [ ] TypeScript 타입 정의 업데이트

---

## 8. 하위 호환성

> ⚠️ **Breaking Change**: 이 변경은 하위 호환되지 않습니다.

클라이언트 업데이트가 필수이며, 버전 관리가 필요한 경우 API 버전을 분리하는 것을 권장합니다.

---

## 참고 자료

- [RFC 7807 - Problem Details for HTTP APIs](https://datatracker.ietf.org/doc/html/rfc7807)
- [Spring ProblemDetail](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html)
