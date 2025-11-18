#!/bin/bash

# =====================================================
# Git Hooks 자동 설치 스크립트
# =====================================================
# 용도: pre-commit + post-commit hooks를 자동으로 설치
# 실행: ./scripts/setup-hooks.sh
# =====================================================

set -e  # Exit on error

REPO_ROOT="$(git rev-parse --show-toplevel)"
HOOKS_DIR="$(git rev-parse --git-path hooks)"

if [[ -d "${REPO_ROOT}/config/hooks" ]]; then
    HOOK_SOURCE_DIR="${REPO_ROOT}/config/hooks"
elif [[ -d "${REPO_ROOT}/.claude/hooks" ]]; then
    HOOK_SOURCE_DIR="${REPO_ROOT}/.claude/hooks"
else
    echo "Hook directory not found (config/hooks or .claude/hooks)"
    exit 1
fi

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# =====================================================
# Helper Functions
# =====================================================

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# =====================================================
# Main Installation
# =====================================================

echo ""
echo "=========================================="
echo "🔧 Git Hooks 설치"
echo "=========================================="
echo ""

# 0. hooks 디렉토리 생성 (없을 경우)
if [[ ! -d "${HOOKS_DIR}" ]]; then
    log_warning "hooks directory not found, creating..."
    mkdir -p "${HOOKS_DIR}"
    log_success "hooks directory created"
fi

# 1. pre-commit hook 설치
log_info "Installing pre-commit hook..."

if [[ -f "${HOOKS_DIR}/pre-commit" ]] && [[ ! -L "${HOOKS_DIR}/pre-commit" ]]; then
    log_warning "Existing pre-commit hook found (not a symlink)"
    read -p "   Overwrite? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_error "Installation cancelled"
        exit 1
    fi
    rm "${HOOKS_DIR}/pre-commit"
fi

ln -sf "${HOOK_SOURCE_DIR}/pre-commit" "${HOOKS_DIR}/pre-commit"
chmod +x "${HOOKS_DIR}/pre-commit"
chmod +x "${HOOK_SOURCE_DIR}/pre-commit"

log_success "pre-commit hook installed"

# 2. post-commit hook 설치
log_info "Installing post-commit hook..."

if [[ -f "${HOOKS_DIR}/post-commit" ]] && [[ ! -L "${HOOKS_DIR}/post-commit" ]]; then
    log_warning "Existing post-commit hook found (not a symlink)"
    read -p "   Overwrite? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_error "Installation cancelled"
        exit 1
    fi
    rm "${HOOKS_DIR}/post-commit"
fi

ln -sf "${HOOK_SOURCE_DIR}/post-commit" "${HOOKS_DIR}/post-commit"
chmod +x "${HOOKS_DIR}/post-commit"
chmod +x "${HOOK_SOURCE_DIR}/post-commit"

log_success "post-commit hook installed"

# 3. 설치 확인
echo ""
log_info "Verifying installation..."

if [[ -L "${HOOKS_DIR}/pre-commit" ]] && [[ -L "${HOOKS_DIR}/post-commit" ]]; then
    log_success "Both hooks are properly linked"
else
    log_error "Hook installation verification failed"
    exit 1
fi

# 4. LangFuse 의존성 확인
echo ""
log_info "Checking LangFuse dependencies..."

# Python langfuse 패키지 확인
if python3 -c "import langfuse" 2>/dev/null; then
    log_success "langfuse package is installed"
else
    log_warning "langfuse package NOT installed"
    echo ""
    echo "   LangFuse 메트릭 수집을 사용하려면 다음 명령을 실행하세요:"
    echo ""
    echo "   ${GREEN}pip3 install langfuse${NC}"
    echo ""
    echo "   (선택사항: LangFuse 없이도 JSONL 로그는 작동합니다)"
    echo ""
fi

# ~/.zshrc 환경 변수 확인
if [[ -n "$LANGFUSE_PUBLIC_KEY" ]] && [[ -n "$LANGFUSE_SECRET_KEY" ]]; then
    log_success "LangFuse environment variables configured in ~/.zshrc"
else
    log_warning "LangFuse environment variables NOT configured"
    echo ""
    echo "   LangFuse 클라우드 업로드를 사용하려면 ~/.zshrc에 환경 변수를 추가하세요:"
    echo ""
    echo "   ${GREEN}echo 'export LANGFUSE_PUBLIC_KEY=\"pk-lf-...\"' >> ~/.zshrc${NC}"
    echo "   ${GREEN}echo 'export LANGFUSE_SECRET_KEY=\"sk-lf-...\"' >> ~/.zshrc${NC}"
    echo "   ${GREEN}echo 'export LANGFUSE_HOST=\"https://us.cloud.langfuse.com\"' >> ~/.zshrc${NC}"
    echo "   ${GREEN}source ~/.zshrc${NC}"
    echo ""
    echo "   (선택사항: 환경 변수 없이도 JSONL 로그는 작동합니다)"
    echo ""
fi

# =====================================================
# Summary
# =====================================================

echo ""
echo "=========================================="
echo "✨ 설치 완료!"
echo "=========================================="
echo ""
echo "설치된 Hooks:"
echo "  ✅ pre-commit  → 코드 품질 검증 (ArchUnit + Gradle)"
echo "  ✅ post-commit → TDD 메트릭 수집 (LangFuse)"
echo ""
echo "동작 방식:"
echo "  1. git commit 전 → pre-commit이 코드 검증"
echo "  2. git commit 후 → post-commit이 메트릭 수집"
echo ""
echo "메트릭 로그 위치:"
echo "  📁 ~/.claude/logs/tdd-cycle.jsonl (항상 작동)"
echo "  ☁️  LangFuse Cloud (환경 변수 설정 시)"
echo ""
echo "다음 단계:"
echo "  1. LangFuse 사용 원하면: pip3 install langfuse"
echo "  2. ~/.zshrc에 환경 변수 추가 (선택사항)"
echo "  3. git commit 테스트!"
echo ""
#!/bin/bash

# =====================================================
# Git Hooks 자동 설치 스크립트
# =====================================================
# 용도: pre-commit + post-commit hooks를 자동으로 설치
# 실행: ./scripts/setup-hooks.sh
# =====================================================

set -e  # Exit on error

REPO_ROOT="$(git rev-parse --show-toplevel)"
HOOKS_DIR="$(git rev-parse --git-path hooks)"

mkdir -p "${HOOKS_DIR}"

if [[ -d "${REPO_ROOT}/config/hooks" ]]; then
    HOOK_SOURCE_DIR="${REPO_ROOT}/config/hooks"
elif [[ -d "${REPO_ROOT}/.claude/hooks" ]]; then
    HOOK_SOURCE_DIR="${REPO_ROOT}/.claude/hooks"
else
    echo "Hook directory not found (config/hooks or .claude/hooks)"
    exit 1
fi

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# =====================================================
# Helper Functions
# =====================================================

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# =====================================================
# Main Installation
# =====================================================

echo ""
echo "=========================================="
echo "🔧 Git Hooks 설치"
echo "=========================================="
echo ""

<<<<<<< HEAD
# Git metadata
PROJECT_ROOT="$(pwd)"
GIT_DIR="$(git rev-parse --git-dir)"
HOOKS_DIR="${GIT_DIR}/hooks"

mkdir -p "${HOOKS_DIR}"
=======
# 0. hooks 디렉토리 생성 (없을 경우)
if [[ ! -d "${HOOKS_DIR}" ]]; then
    log_warning "hooks directory not found, creating..."
    mkdir -p "${HOOKS_DIR}"
    log_success "hooks directory created"
fi
>>>>>>> origin/feature/MUSTIT-002-application

# 1. pre-commit hook 설치
log_info "Installing pre-commit hook..."

if [[ -f "${HOOKS_DIR}/pre-commit" ]] && [[ ! -L "${HOOKS_DIR}/pre-commit" ]]; then
    log_warning "Existing pre-commit hook found (not a symlink)"
    read -p "   Overwrite? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_error "Installation cancelled"
        exit 1
    fi
    rm "${HOOKS_DIR}/pre-commit"
fi

<<<<<<< HEAD
ln -sf "${PROJECT_ROOT}/config/hooks/pre-commit" "${HOOKS_DIR}/pre-commit"
chmod +x "${HOOKS_DIR}/pre-commit"
chmod +x "${PROJECT_ROOT}/config/hooks/pre-commit"
=======
ln -sf "${HOOK_SOURCE_DIR}/pre-commit" "${HOOKS_DIR}/pre-commit"
chmod +x "${HOOKS_DIR}/pre-commit"
chmod +x "${HOOK_SOURCE_DIR}/pre-commit"
>>>>>>> origin/feature/MUSTIT-002-application

log_success "pre-commit hook installed"

# 2. post-commit hook 설치
log_info "Installing post-commit hook..."

if [[ -f "${HOOKS_DIR}/post-commit" ]] && [[ ! -L "${HOOKS_DIR}/post-commit" ]]; then
    log_warning "Existing post-commit hook found (not a symlink)"
    read -p "   Overwrite? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_error "Installation cancelled"
        exit 1
    fi
    rm "${HOOKS_DIR}/post-commit"
fi

<<<<<<< HEAD
ln -sf "${PROJECT_ROOT}/config/hooks/post-commit" "${HOOKS_DIR}/post-commit"
chmod +x "${HOOKS_DIR}/post-commit"
chmod +x "${PROJECT_ROOT}/config/hooks/post-commit"
=======
ln -sf "${HOOK_SOURCE_DIR}/post-commit" "${HOOKS_DIR}/post-commit"
chmod +x "${HOOKS_DIR}/post-commit"
chmod +x "${HOOK_SOURCE_DIR}/post-commit"
>>>>>>> origin/feature/MUSTIT-002-application

log_success "post-commit hook installed"

# 3. 설치 확인
echo ""
log_info "Verifying installation..."

if [[ -L "${HOOKS_DIR}/pre-commit" ]] && [[ -L "${HOOKS_DIR}/post-commit" ]]; then
    log_success "Both hooks are properly linked"
else
    log_error "Hook installation verification failed"
    exit 1
fi

# 4. LangFuse 의존성 확인
echo ""
log_info "Checking LangFuse dependencies..."

# Python langfuse 패키지 확인
if python3 -c "import langfuse" 2>/dev/null; then
    log_success "langfuse package is installed"
else
    log_warning "langfuse package NOT installed"
    echo ""
    echo "   LangFuse 메트릭 수집을 사용하려면 다음 명령을 실행하세요:"
    echo ""
    echo "   ${GREEN}pip3 install langfuse${NC}"
    echo ""
    echo "   (선택사항: LangFuse 없이도 JSONL 로그는 작동합니다)"
    echo ""
fi

# ~/.zshrc 환경 변수 확인
if [[ -n "$LANGFUSE_PUBLIC_KEY" ]] && [[ -n "$LANGFUSE_SECRET_KEY" ]]; then
    log_success "LangFuse environment variables configured in ~/.zshrc"
else
    log_warning "LangFuse environment variables NOT configured"
    echo ""
    echo "   LangFuse 클라우드 업로드를 사용하려면 ~/.zshrc에 환경 변수를 추가하세요:"
    echo ""
    echo "   ${GREEN}echo 'export LANGFUSE_PUBLIC_KEY=\"pk-lf-...\"' >> ~/.zshrc${NC}"
    echo "   ${GREEN}echo 'export LANGFUSE_SECRET_KEY=\"sk-lf-...\"' >> ~/.zshrc${NC}"
    echo "   ${GREEN}echo 'export LANGFUSE_HOST=\"https://us.cloud.langfuse.com\"' >> ~/.zshrc${NC}"
    echo "   ${GREEN}source ~/.zshrc${NC}"
    echo ""
    echo "   (선택사항: 환경 변수 없이도 JSONL 로그는 작동합니다)"
    echo ""
fi

# =====================================================
# Summary
# =====================================================

echo ""
echo "=========================================="
echo "✨ 설치 완료!"
echo "=========================================="
echo ""
echo "설치된 Hooks:"
echo "  ✅ pre-commit  → 코드 품질 검증 (ArchUnit + Gradle)"
echo "  ✅ post-commit → TDD 메트릭 수집 (LangFuse)"
echo ""
echo "동작 방식:"
echo "  1. git commit 전 → pre-commit이 코드 검증"
echo "  2. git commit 후 → post-commit이 메트릭 수집"
echo ""
echo "메트릭 로그 위치:"
echo "  📁 ~/.claude/logs/tdd-cycle.jsonl (항상 작동)"
echo "  ☁️  LangFuse Cloud (환경 변수 설정 시)"
echo ""
echo "다음 단계:"
echo "  1. LangFuse 사용 원하면: pip3 install langfuse"
echo "  2. ~/.zshrc에 환경 변수 추가 (선택사항)"
echo "  3. git commit 테스트!"
echo ""
