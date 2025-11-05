# PR 생성 가이드

## 1. 변경사항 확인
```bash
cd /Users/sangwon-ryu/wt-seller
git status
```

## 2. 모든 변경사항 스테이징
```bash
# 새로 추가된 파일들 포함하여 모두 추가
git add .

# 또는 특정 파일만 추가
git add adapter-in/rest-api/src/main/java/com/ryuqq/crawlinghub/adapter/in/rest/
git add adapter-out/persistence-mysql/src/main/java/com/ryuqq/crawlinghub/adapter/out/persistence/mustit/seller/
git add application/src/main/java/com/ryuqq/crawlinghub/application/mustit/seller/
git add domain/src/main/java/com/ryuqq/crawlinghub/domain/mustit/seller/history/
git add adapter-out/persistence-mysql/src/main/resources/db/migration/V2__Create_product_count_history.sql
```

## 3. 커밋 생성
```bash
# 커밋 메시지는 COMMIT_MESSAGE.md 참조
git commit -m "feat(seller): 셀러 상세 조회 API 확장 및 CQRS 패턴 적용

셀러 Bounded Context 리팩토링 - Phase 1-4 완료

### 주요 변경사항

#### Phase 1: Domain Layer
- ✅ ProductCountHistory 도메인 객체 생성
- ✅ ProductCountHistoryId Value Object 생성

#### Phase 2: Application Layer
- ✅ SaveProductCountHistoryPort / LoadProductCountHistoryPort 생성
- ✅ SellerManager 구현 (Manager 패턴)
- ✅ SellerCommandFacade 구현 (Facade 패턴)
- ✅ GetSellerDetailService 확장 (getDetail 메서드 추가)

#### Phase 3: Persistence Layer (CQRS)
- ✅ Flyway 마이그레이션 스크립트 작성
- ✅ ProductCountHistoryEntity JPA Entity 생성
- ✅ CQRS 패턴 적용 (Command/Query 분리)

#### Phase 4: REST API Layer
- ✅ PageApiResponse 공통 DTO 생성
- ✅ SellerDetailApiResponse 생성
- ✅ SellerApiMapper 확장
- ✅ SellerController 수정

### 향후 작업
- 스케줄 관련 Port 구현 후 ScheduleInfo, ScheduleHistory 기능 활성화 예정"
```

## 4. 원격 브랜치에 푸시
```bash
# 현재 브랜치 확인
git branch

# 원격에 푸시 (처음이면 -u 옵션)
git push -u origin feature/seller

# 이미 푸시된 브랜치면
git push
```

## 5. PR 생성
GitHub/GitLab에서 PR 생성:
- **제목**: `feat(seller): 셀러 상세 조회 API 확장 및 CQRS 패턴 적용 (Phase 1-4)`
- **설명**: `COMMIT_MESSAGE.md`의 PR 설명 제안 참조

## 6. (선택) 커밋 메시지 파일 삭제
```bash
git rm COMMIT_MESSAGE.md PR_GUIDE.md
git commit -m "docs: PR 가이드 파일 제거"
```

## 빠른 명령어 모음
```bash
# 모든 변경사항 확인 및 커밋
cd /Users/sangwon-ryu/wt-seller
git add .
git commit -F COMMIT_MESSAGE.md  # 또는 직접 커밋 메시지 입력
git push -u origin feature/seller
```
