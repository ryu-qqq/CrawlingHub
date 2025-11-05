#!/bin/bash

# =====================================================
# Cursor IDE Worktree Setup Script
# Purpose: 각 워크트리 생성 시 자동으로 실행되는 설정 스크립트
# Usage: Cursor IDE가 자동으로 실행 (수동 실행 불필요)
# =====================================================

set -e

WORKTREE_ROOT="$(pwd)"
PROJECT_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || echo "${WORKTREE_ROOT}")"

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}ℹ️  ${NC}$1"
}

log_success() {
    echo -e "${GREEN}✅ ${NC}$1"
}

log_warning() {
    echo -e "${YELLOW}⚠️  ${NC}$1"
}

# 워크트리인지 확인
if [[ "$WORKTREE_ROOT" == "$PROJECT_ROOT" ]]; then
    # 메인 프로젝트이면 스크립트 실행 안함
    exit 0
fi

log_info "워크트리 설정 시작: ${WORKTREE_ROOT}"

# 1. .cursorrules 확인 및 복사 (없는 경우)
if [[ ! -f "${WORKTREE_ROOT}/.cursorrules" ]] && [[ -f "${PROJECT_ROOT}/.cursorrules" ]]; then
    log_info ".cursorrules 복사 중..."
    cp "${PROJECT_ROOT}/.cursorrules" "${WORKTREE_ROOT}/"
    log_success ".cursorrules 복사 완료"
fi

# 2. 작업지시서 확인 (있는 경우)
if ls "${WORKTREE_ROOT}"/*.md 1> /dev/null 2>&1; then
    log_info "작업지시서 발견:"
    ls -1 "${WORKTREE_ROOT}"/*.md | while read -r file; do
        echo "  - $(basename "$file")"
    done
fi

# 3. 환경 변수 설정 (필요한 경우)
if [[ -f "${PROJECT_ROOT}/.env" ]]; then
    log_info "환경 변수 파일 확인: .env"
    if [[ ! -f "${WORKTREE_ROOT}/.env" ]]; then
        log_warning ".env 파일이 없습니다. 필요한 경우 복사하세요."
    fi
fi

# 4. Gradle 설정 확인
if [[ -f "${WORKTREE_ROOT}/gradlew" ]]; then
    log_info "Gradle 프로젝트 확인됨"
    # Gradle wrapper 권한 확인
    if [[ ! -x "${WORKTREE_ROOT}/gradlew" ]]; then
        log_info "Gradle wrapper 실행 권한 부여 중..."
        chmod +x "${WORKTREE_ROOT}/gradlew"
    fi
fi

# 5. Cursor 워크스페이스 파일 확인
if ls "${WORKTREE_ROOT}"/*.code-workspace 1> /dev/null 2>&1; then
    log_success "Cursor 워크스페이스 파일 발견:"
    ls -1 "${WORKTREE_ROOT}"/*.code-workspace | while read -r file; do
        echo "  - $(basename "$file")"
    done
fi

log_success "워크트리 설정 완료!"
echo ""
echo "📝 다음 단계:"
echo "  1. Cursor IDE에서 워크스페이스 파일을 열어주세요"
echo "  2. 작업지시서를 참조하여 코드를 작성하세요"
echo "  3. git commit으로 변경사항을 커밋하세요"


