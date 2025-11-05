# Worktree Create Command

**Git Worktree 생성 및 작업 환경 설정**

---

## 🎯 목적

새로운 기능 개발을 위한 독립적인 Git Worktree 생성:
1. Feature 브랜치 생성
2. Worktree 디렉토리 생성
3. 작업지시서 자동 복사
4. .cursorrules 자동 복사
5. Cursor AI 작업 환경 준비

---

## 📝 사용법

```bash
# 기본 사용 (작업지시서 없음)
/worktree-create order

# 작업지시서 포함
/worktree-create order order-aggregate.md

# 다른 기능 예시
/worktree-create payment payment-aggregate.md
```

---

## 🔄 실행 프로세스

### Step 1: Worktree Manager 스크립트 실행

```bash
bash .claude/scripts/worktree-manager.sh create {feature-name} [work-order]
```

### Step 2: 자동 처리 항목

1. **워크트리 디렉토리 생성**: `.worktrees/` 디렉토리 생성 (없는 경우)
2. **브랜치 생성**: `feature/{feature-name}` 브랜치 생성 (없는 경우)
3. **Worktree 추가**: `.worktrees/wt-{feature-name}` 디렉토리에 Worktree 추가
4. **작업지시서 복사**: `.claude/work-orders/{work-order}` → Worktree 루트로 복사
5. **규칙 복사**: `.cursorrules` → Worktree 루트로 복사
6. **Cursor 워크스페이스 파일 생성**: `{feature-name}.code-workspace` 파일 생성
7. **Cursor IDE 자동 열기**: 가능한 경우 Cursor IDE로 워크스페이스 자동 열기

---

## 📦 출력

**성공:**
```
✅ Worktree 생성 완료!

📂 Worktree 경로: /Users/sangwon-ryu/crawlinghub/.worktrees/wt-order
🌿 브랜치: feature/order
📋 작업지시서: order-aggregate.md (자동 복사)
🎨 Cursor 워크스페이스: /Users/sangwon-ryu/crawlinghub/.worktrees/wt-order/order.code-workspace

📝 다음 단계:
  1. Cursor IDE에서 워크스페이스 열기:
     - 프롬프트 아래에서 'order.code-workspace' 선택
     - 또는 더블클릭: .worktrees/wt-order/order.code-workspace
  2. Cursor AI로 Boilerplate 생성
  3. order-aggregate.md 참조하여 코드 작성
  4. git commit
  5. /validate-cursor-changes (검증)
```

**브랜치 이미 존재:**
```
⚠️  브랜치 이미 존재: feature/order
✅ Worktree 추가 완료
```

**작업지시서 없음:**
```
⚠️  작업지시서 없음: .claude/work-orders/invalid-order.md
✅ Worktree 생성 완료 (작업지시서 제외)
```

---

## 🌲 Worktree 구조

생성된 Worktree 디렉토리 구조:

```
/Users/sangwon-ryu/crawlinghub/
├── .worktrees/                    # 워크트리 관리 디렉토리 (프로젝트 내부)
│   └── wt-order/                  # order 기능 워크트리
│       ├── adapter-in/
│       ├── adapter-out/
│       ├── application/
│       ├── domain/
│       ├── bootstrap/
│       ├── order-aggregate.md     # 작업지시서 (자동 복사)
│       ├── .cursorrules           # Cursor AI 규칙 (자동 복사)
│       ├── order.code-workspace   # Cursor IDE 워크스페이스 파일 (자동 생성)
│       └── ... (프로젝트 전체 파일)
└── ... (메인 프로젝트 파일)
```

**장점:**
- 프로젝트 내부에서 모든 워크트리 관리
- `.gitignore`로 자동 제외 (커밋되지 않음)
- 깔끔한 구조 및 쉬운 관리

**워크스페이스 파일 특징:**
- Cursor IDE에서 자동으로 인식
- 프롬프트 아래에서 워크스페이스 선택 가능
- 더블클릭으로 바로 열 수 있음

---

## 💡 사용 시나리오

### 시나리오 1: 큐 시스템과 함께 사용

```bash
# 1. 작업 큐에 추가
/queue-add order order-aggregate.md

# 2. 작업 시작
/queue-start order

# 3. Worktree 생성 (수동 또는 자동)
/worktree-create order order-aggregate.md
```

### 시나리오 2: 독립적으로 사용

```bash
# Worktree 생성
/worktree-create payment payment-aggregate.md
# → .worktrees/wt-payment/ 생성
# → payment.code-workspace 자동 생성
# → Cursor IDE 자동 열기 시도

# Cursor IDE에서 작업
# → 프롬프트 아래에서 'payment.code-workspace' 선택
# → payment-aggregate.md 참조
# → .cursorrules 자동 로드
# → 코드 생성

# 커밋 (워크트리 내에서)
cd .worktrees/wt-payment
git add .
git commit -m "feat: Payment Aggregate 생성"

# 검증 (메인 프로젝트에서)
cd /Users/sangwon-ryu/crawlinghub
/validate-cursor-changes
```

---

## ⚠️ 주의사항

**중복 Worktree:**
```
❌ Worktree가 이미 존재함: ../wt-order

기존 Worktree를 제거하려면:
/worktree-remove order
```

**경로 제약:**
- Worktree는 프로젝트 내부 `.worktrees/` 디렉토리에 생성됩니다
- 프로젝트 내부에서 모든 워크트리 관리
- `.gitignore`에 자동 추가되어 커밋되지 않음

**브랜치 충돌:**
- 이미 존재하는 브랜치인 경우 기존 브랜치를 사용합니다
- 새 브랜치가 필요한 경우 먼저 브랜치를 삭제하세요

**Cursor IDE 자동 열기:**
- `cursor` CLI 명령어가 설치되어 있으면 자동으로 열림
- macOS에서는 `open -a Cursor` 사용
- 자동 열기 실패 시 워크스페이스 파일을 수동으로 열어주세요

---

## 🔗 관련 커맨드

- `/worktree-remove {feature}` - Worktree 제거
- `/worktree-list` - 활성 Worktree 목록
- `/worktree-status` - Worktree 상태 확인
- `/queue-start {feature}` - 큐 작업 시작 (Worktree 자동 생성 안내)

---

**✅ 이 커맨드는 독립적인 개발 환경을 제공합니다!**

