# Notice API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 2개 |
| Command (명령) | 0개 |
| **합계** | **2개** |

**Base Path**: `/api/v1/market/notice-categories`

> Command Controller 없음. 고시정보 카테고리는 조회 전용 도메인입니다.

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | /api/v1/market/notice-categories | NoticeCategoryQueryController | searchNoticeCategoriesByOffset | SearchNoticeCategoryByOffsetUseCase |
| 2 | GET | /api/v1/market/notice-categories/category-group/{categoryGroup} | NoticeCategoryQueryController | getNoticeCategoryByCategoryGroup | GetNoticeCategoryUseCase |

### Q1. searchNoticeCategoriesByOffset - 고시정보 카테고리 목록 조회

- **Path**: `GET /api/v1/market/notice-categories`
- **Controller**: `NoticeCategoryQueryController`
- **Method**: `searchNoticeCategoriesByOffset(@ParameterObject @Valid SearchNoticeCategoriesApiRequest request)`
- **Permission**: `notice-category:read`
- **Request**: `SearchNoticeCategoriesApiRequest` (@ModelAttribute - Query String)
  - `active`: Boolean (옵션) - 활성 상태 필터 (true/false, 미지정시 전체)
  - `searchField`: String (옵션) - 검색 필드 (CODE, NAME_KO, NAME_EN)
  - `searchWord`: String (옵션) - 검색어
  - `sortKey`: String (옵션) - 정렬 키 (createdAt, code)
  - `sortDirection`: String (옵션) - 정렬 방향 (ASC, DESC)
  - `page`: Integer (옵션, 기본값 0) - 페이지 번호 (0부터)
  - `size`: Integer (옵션, 기본값 20) - 페이지 크기
- **Response**: `ApiResponse<PageApiResponse<NoticeCategoryApiResponse>>`
  - `NoticeCategoryApiResponse`:
    - `id`: Long - 카테고리 ID
    - `code`: String - 카테고리 코드
    - `nameKo`: String - 한글명
    - `nameEn`: String - 영문명
    - `targetCategoryGroup`: String - 대상 카테고리 그룹
    - `active`: boolean - 활성 상태
    - `fields`: List&lt;NoticeFieldApiResponse&gt; - 고시정보 필드 목록
    - `createdAt`: String (ISO 8601) - 생성일시
  - `NoticeFieldApiResponse` (fields 배열 내):
    - `id`: Long - 필드 ID
    - `fieldCode`: String - 필드 코드
    - `fieldName`: String - 필드명
    - `required`: boolean - 필수 여부
    - `sortOrder`: int - 정렬 순서
- **UseCase**: `SearchNoticeCategoryByOffsetUseCase`
- **설명**: 고시정보 카테고리 목록을 Offset 기반 페이지네이션으로 조회합니다. 활성 상태 필터, 코드/이름 검색, 정렬을 지원합니다.

### Q2. getNoticeCategoryByCategoryGroup - 카테고리 그룹별 고시정보 조회

- **Path**: `GET /api/v1/market/notice-categories/category-group/{categoryGroup}`
- **Controller**: `NoticeCategoryQueryController`
- **Method**: `getNoticeCategoryByCategoryGroup(@PathVariable CategoryGroup categoryGroup)`
- **Permission**: `notice-category:read`
- **Request**:
  - Path Variable: `categoryGroup` (CategoryGroup enum) - 카테고리 그룹 코드
- **Response**: `ApiResponse<NoticeCategoryApiResponse>`
  - `NoticeCategoryApiResponse`:
    - `id`: Long - 카테고리 ID
    - `code`: String - 카테고리 코드
    - `nameKo`: String - 한글명
    - `nameEn`: String - 영문명
    - `targetCategoryGroup`: String - 대상 카테고리 그룹
    - `active`: boolean - 활성 상태
    - `fields`: List&lt;NoticeFieldApiResponse&gt; - 고시정보 필드 목록
    - `createdAt`: String (ISO 8601) - 생성일시
- **UseCase**: `GetNoticeCategoryUseCase`
- **설명**: 특정 카테고리 그룹(CategoryGroup)에 해당하는 고시정보 카테고리를 단건 조회합니다. 해당 카테고리에 포함된 고시정보 필드 목록도 함께 반환합니다.

---

## Command Endpoints

해당 도메인에 Command 엔드포인트가 없습니다.

고시정보 카테고리(Notice Category)는 관리자가 시스템 레벨에서 관리하는 마스터 데이터로, REST API를 통한 생성/수정/삭제가 제공되지 않습니다.

---

## 아키텍처 패턴

### CQRS 분리

- **Query Controller**: `NoticeCategoryQueryController` - 조회 전용 (GET 메서드만)
- **Command Controller**: 미구현 (조회 전용 도메인)

### Hexagonal Architecture

- **Adapter-In (REST)**: Controller → Mapper → UseCase
- **Application Layer**: UseCase (Port-In) 인터페이스 정의
- **Adapter-Out**: Persistence 구현 (Port-Out)

### 권한 체계

- `notice-category:read`: 고시정보 카테고리 조회 권한 (`@RequirePermission`)

### Endpoints 상수 클래스

```java
// NoticeAdminEndpoints.java
private static final String BASE = "/api/v1/market";
public static final String NOTICE_CATEGORIES = BASE + "/notice-categories";
public static final String CATEGORY_GROUP = "/category-group/{categoryGroup}";
public static final String PATH_CATEGORY_GROUP = "categoryGroup";
```

---

## 사용 예시

### 고시정보 카테고리 목록 조회

```http
GET /api/v1/market/notice-categories?active=true&page=0&size=20
```

```http
GET /api/v1/market/notice-categories?searchField=NAME_KO&searchWord=의류&page=0&size=10
```

### 카테고리 그룹별 고시정보 조회

```http
GET /api/v1/market/notice-categories/category-group/FASHION
```

---

## 관련 문서

- Domain Layer: `domain/src/main/java/com/ryuqq/marketplace/domain/category/vo/CategoryGroup.java`
- Application Layer: `application/src/main/java/com/ryuqq/marketplace/application/notice/`
- UseCase 인터페이스:
  - `application/.../notice/port/in/query/GetNoticeCategoryUseCase.java`
  - `application/.../notice/port/in/query/SearchNoticeCategoryByOffsetUseCase.java`
