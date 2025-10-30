# 설치 스크립트

## install-claude-hooks.sh

Claude Hooks + Cache 시스템을 다른 프로젝트에 설치하는 대화형 스크립트입니다.

### 사용법

```bash
# 1. 이 프로젝트를 임시로 클론
git clone https://github.com/your-org/claude-spring-standards.git /tmp/claude-spring-standards

# 2. 본인 프로젝트로 이동
cd your-project

# 3. 설치 스크립트 실행
bash /tmp/claude-spring-standards/scripts/install-claude-hooks.sh

# 4. 완료 후 임시 디렉토리 삭제
rm -rf /tmp/claude-spring-standards
```

### 설치되는 항목

#### 필수 항목 (자동 설치)
- `.claude/hooks/user-prompt-submit.sh` - 사용자 입력 Hook
- `.claude/hooks/after-tool-use.sh` - 도구 사용 후 Hook
- `.claude/hooks/scripts/log-helper.py` - JSON 로그 헬퍼
- `.claude/hooks/scripts/view-logs.sh` - 로그 뷰어
- `.claude/hooks/scripts/validation-helper.py` - 검증 헬퍼
- `.claude/hooks/scripts/build-rule-cache.py` - Cache 빌드 스크립트
- `.claude/commands/lib/inject-rules.py` - 규칙 주입 스크립트
- `.claude/commands/code-gen-domain.md` - Domain 코드 생성 명령
- `.claude/commands/code-gen-usecase.md` - UseCase 코드 생성 명령
- `.claude/commands/code-gen-controller.md` - Controller 코드 생성 명령
- `.claude/commands/validate-domain.md` - Domain 검증 명령
- `.claude/commands/validate-architecture.md` - 아키텍처 검증 명령
- `.claude/commands/README.md` - Commands 설명
- `.claude/hooks/logs/README.md` - 로그 시스템 설명

#### 선택 항목 (대화형 선택)
- `.claude/CLAUDE.md` - Claude Code 프로젝트 설정
- `docs/coding_convention/` - 코딩 규칙 문서 (90개 규칙)
- `hooks/pre-commit` - Git pre-commit hooks (커밋 시점 검증)
- `hooks/validators/` - Git hook 검증 스크립트

### 설치 후 작업

#### 1. 프로젝트별 설정 수정

```bash
# CLAUDE.md 편집 (프로젝트 정보 업데이트)
vim .claude/CLAUDE.md
```

#### 2. 코딩 규칙 추가/수정

```bash
# 기존 규칙 수정 또는 새 규칙 추가
vim docs/coding_convention/02-domain-layer/...
```

#### 3. Cache 빌드

```bash
# 규칙 문서를 JSON Cache로 변환
python3 .claude/hooks/scripts/build-rule-cache.py
```

#### 4. Git Pre-commit Hooks 설정 (선택사항)

Git pre-commit hooks를 설치한 경우, 프로젝트에 맞게 검증 규칙을 수정하세요.

```bash
# 검증 스크립트 수정
vim hooks/validators/validate-transaction-boundaries.sh
vim hooks/validators/validate-proxy-constraints.sh
vim hooks/validators/validate-lombok-usage.sh
vim hooks/validators/validate-law-of-demeter.sh

# 테스트
git add <file>
git commit -m "test"  # 검증 자동 실행
```

**검증 항목:**
- ✅ Transaction 경계 검증 (`@Transactional` 내 외부 API 호출)
- ✅ Spring 프록시 제약사항 (Private/Final 메서드 `@Transactional`)
- ✅ Lombok 사용 금지
- ✅ Law of Demeter (Getter 체이닝 금지)

**심볼릭 링크:**
- `.git/hooks/pre-commit` → `../../hooks/pre-commit`
- Git 저장소가 아니면 설치 불가

### 의존성

스크립트가 자동으로 확인하는 항목:

- **Python 3**: 필수 (스크립트 실행용)
- **tiktoken**: 선택 설치 (Token 측정용)
- **jq**: 권장 (JSON 로그 분석용)

### 백업

스크립트는 다음과 같이 자동 백업합니다:

- 기존 `.claude/hooks/` → `.claude/hooks.backup.YYYYMMDD_HHMMSS`
- 기존 `docs/coding_convention/` → `docs/coding_convention.backup.YYYYMMDD_HHMMSS`

### 실행 예시

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🚀 Claude Hooks + Cache 시스템 설치
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

설치 대상 디렉토리: /Users/user/my-project

⚠️  이미 Claude Hooks가 설치되어 있습니다.

덮어쓰시겠습니까? (y/N): y

기존 설정을 백업합니다...
✅ 백업 완료: /Users/user/my-project/.claude/hooks.backup.20251017_143000

📁 디렉토리 구조 생성 중...
📋 설정 파일 복사 중...
🔧 실행 권한 설정 중...
✅ 파일 복사 완료

💡 CLAUDE.md 파일이 없습니다.
템플릿 CLAUDE.md를 복사하시겠습니까? (y/N): y
✅ CLAUDE.md 복사 완료
⚠️  프로젝트에 맞게 CLAUDE.md를 수정하세요!

📚 코딩 규칙 문서 (docs/coding_convention/)
이 디렉토리는 프로젝트별로 다를 수 있습니다.

코딩 규칙 문서도 복사하시겠습니까? (y/N): y
✅ 코딩 규칙 문서 복사 완료

🐍 Python 의존성 확인 중...
✅ Python 3 확인 완료
⚠️  tiktoken이 설치되지 않았습니다.
tiktoken을 설치하시겠습니까? (y/N): y
✅ tiktoken 설치 완료

🔧 jq 설치 확인 중...
✅ jq 설치 확인 완료

💾 Cache 빌드
지금 Cache를 빌드하시겠습니까? (y/N): y
✅ Cache 빌드 완료

🔗 Git Pre-commit Hooks (선택사항)
Git pre-commit hooks는 커밋 시점에 코드를 검증합니다.
※ 주의: Spring 프로젝트 전용 검증 로직이 포함되어 있습니다.

검증 항목:
  - Transaction 경계 검증 (@Transactional 내 외부 API 호출)
  - Spring 프록시 제약사항 (Private/Final 메서드)
  - Lombok 사용 금지
  - Law of Demeter (Getter 체이닝)

Git pre-commit hooks를 설치하시겠습니까? (y/N): y
📋 Git hooks 파일 복사 중...
✅ Git pre-commit hooks 설치 완료
   위치: hooks/pre-commit
   심볼릭 링크: .git/hooks/pre-commit → ../../hooks/pre-commit

💡 프로젝트에 맞게 hooks/validators/ 스크립트를 수정하세요!

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✅ 설치 완료!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📖 다음 단계:

1. 프로젝트별 설정 수정:
   - .claude/CLAUDE.md 편집 (프로젝트 정보 업데이트)
   - docs/coding_convention/ 규칙 추가/수정
   - hooks/validators/ 스크립트 수정 (프로젝트 검증 규칙)

2. Cache 빌드 (규칙 변경 시마다):
   python3 .claude/hooks/scripts/build-rule-cache.py

3. 로그 확인:
   ./.claude/hooks/scripts/view-logs.sh
   ./.claude/hooks/scripts/view-logs.sh -f  # 실시간
   ./.claude/hooks/scripts/view-logs.sh -s  # 통계

4. Git pre-commit hooks 테스트:
   git add <file>
   git commit -m "test" # 검증 자동 실행

💡 Claude Code에서 다음과 같이 사용하세요:
   - domain, usecase, controller 등 키워드 입력
   - 자동으로 Layer별 규칙이 주입되고 검증됩니다
```

### 문제 해결

#### 권한 오류
```bash
chmod +x /tmp/claude-spring-standards/scripts/install-claude-hooks.sh
```

#### Python 없음
```bash
# macOS
brew install python3

# Ubuntu
sudo apt-get install python3
```

#### jq 없음
```bash
# macOS
brew install jq

# Ubuntu
sudo apt-get install jq
```

### 제거 방법

```bash
# Claude 설정 완전 제거
rm -rf .claude/hooks
rm -rf .claude/cache
rm -rf .claude/commands/lib/inject-rules.py

# 코딩 규칙 문서도 제거 (선택)
rm -rf docs/coding_convention

# Git pre-commit hooks 제거 (선택)
rm -rf hooks
rm .git/hooks/pre-commit
```

### Git Pre-commit Hooks 상세

#### 검증 스크립트 구조

```
hooks/
├── pre-commit                          # Master hook (심볼릭 링크 대상)
└── validators/
    ├── validate-transaction-boundaries.sh  # Transaction 경계 검증
    ├── validate-proxy-constraints.sh       # Spring 프록시 제약사항
    ├── validate-lombok-usage.sh            # Lombok 사용 금지
    └── validate-law-of-demeter.sh          # Law of Demeter
```

#### 검증 규칙 커스터마이징

각 검증 스크립트는 독립적으로 활성화/비활성화 가능합니다.

```bash
# validators/validate-transaction-boundaries.sh 예시
# 외부 API 호출 패턴을 프로젝트에 맞게 수정
EXTERNAL_API_PATTERNS=(
    "RestTemplate"
    "WebClient"
    "FeignClient"
    # 프로젝트 특정 패턴 추가
)
```

#### 검증 비활성화

특정 검증을 비활성화하려면 `hooks/pre-commit`에서 해당 라인 주석 처리:

```bash
# vim hooks/pre-commit
# bash "$VALIDATORS_DIR/validate-lombok-usage.sh" "$STAGED_FILES"  # 비활성화
```

#### 수동 설치 (스크립트 사용하지 않는 경우)

```bash
# 1. hooks 디렉토리 생성
mkdir -p hooks/validators

# 2. 검증 스크립트 복사
cp /path/to/template/hooks/* hooks/

# 3. 실행 권한 부여
chmod +x hooks/pre-commit hooks/validators/*.sh

# 4. Git hooks 심볼릭 링크 생성
ln -sf ../../hooks/pre-commit .git/hooks/pre-commit

# 5. 테스트
git add <file>
git commit -m "test"
```
