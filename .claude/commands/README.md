# Slash Commands Guide

이 디렉토리는 프로젝트에서 사용할 수 있는 커스텀 슬래시 커맨드들을 포함합니다.

## 📚 목차

- [개요](#개요)
- [사용 가능한 커맨드](#사용-가능한-커맨드)
  - [Gemini Review 분석](#gemini-review)
  - [Jira Task 분석](#jira-task)
- [커맨드 작성 가이드](#커맨드-작성-가이드)
- [MCP 서버 통합](#mcp-서버-통합)

## 개요

슬래시 커맨드는 반복적인 작업 워크플로우를 자동화하고 일관성 있는 프로세스를 제공합니다. 각 커맨드는 특정 작업에 최적화된 도구와 절차를 정의합니다.

### 커맨드 실행 방법

```bash
/command-name [arguments] [flags]
```

## 사용 가능한 커맨드

> **참고**: 일부 커맨드는 `/sc:` 접두사를 사용합니다. 이는 SuperClaude 프레임워크의 표준 커맨드를 나타냅니다.

### sc:gemini-review

**설명**: Gemini 코드 리뷰 코멘트를 분석하고 체계적인 리팩토링 전략을 생성합니다.

**주요 기능**:
- PR의 Gemini 리뷰 코멘트 자동 수집
- 리뷰 항목 분류 (critical, improvement, suggestion, style)
- 우선순위 기반 리팩토링 계획 생성
- 영향도 분석 및 구현 노력 추정
- 단계별 실행 계획 제공

**사용법**:
```bash
# 현재 브랜치의 PR 분석
/sc:gemini-review

# 특정 PR 번호 지정
/sc:gemini-review 42

# 분석만 수행 (리팩토링 계획 제외)
/sc:gemini-review --analyze-only

# critical 항목만 자동 리팩토링
/sc:gemini-review --auto-refactor --min-severity critical
```

**출력 구조**:
1. **리뷰 요약**: 전체 코멘트 수, 심각도 분포, 공통 패턴
2. **분류별 분석**: 각 코멘트의 카테고리, 위치, 영향도, 리팩토링 결정
3. **리팩토링 전략**: 즉시 조치, 단기 개선, 장기 개선 항목
4. **구현 계획**: 단계별 작업 순서 및 예상 시간
5. **의사결정 기록**: 수용/보류/거부된 변경사항 및 이유

**필요 조건**:
- GitHub CLI (`gh`) 설치 및 인증
- Gemini가 PR 리뷰어로 설정된 저장소
- PR이 생성된 브랜치 또는 PR 번호

**MCP 통합**:
- `sequential-thinking`: 복잡한 리팩토링 의사결정 분석
- `serena`: 코드 심볼 분석 및 영향도 평가

---

### jira-task

**설명**: Jira 이슈를 분석하고 구조화된 TodoList를 자동 생성합니다.

**주요 기능**:
- Jira API를 통한 이슈 상세 정보 조회
- Epic 및 하위 태스크 정보 추출
- 브랜치 자동 체크아웃 또는 생성 제안
- Acceptance criteria 기반 작업 항목 분해
- TodoList 자동 생성 및 관리

**사용법**:
```bash
# 이슈 키로 조회
/jira-task KAN-6

# Jira URL로 조회
/jira-task https://ryuqqq.atlassian.net/browse/KAN-6

# 보드 URL에서 선택된 이슈
/jira-task https://ryuqqq.atlassian.net/jira/software/projects/KAN/boards/1?selectedIssue=KAN-6
```

**워크플로우**:
1. **Cloud ID 확인**: Atlassian 리소스 접근 권한 확인
2. **이슈 조회**: Jira API로 이슈 상세 정보 가져오기
3. **Epic 분석**: 소속 Epic의 목표 및 컨텍스트 파악
4. **브랜치 관리**: 기존 브랜치 체크아웃 또는 새 브랜치 제안
5. **TodoList 생성**: 체계적인 작업 항목 자동 생성

**출력 예시**:
```markdown
## Jira 태스크 분석: KAN-6

**제목**: 사용자 인증 기능 구현
**Epic**: 사용자 관리 시스템 구축
**현재 상태**: In Progress
**브랜치**: feature/KAN-6-user-authentication

### 작업 설명
JWT 기반 사용자 인증 시스템을 구현합니다.
- 로그인/로그아웃 API 엔드포인트
- 토큰 검증 미들웨어
- 보안 정책 적용

### TodoList 생성 완료
6개 작업 항목이 생성되었습니다.
```

**TodoList 구조**:
1. ✅ 브랜치 체크아웃 완료
2. 🔄 설계/분석 작업
3. ⏳ 핵심 기능 구현 (acceptance criteria 기반)
4. ⏳ 테스트 작성 및 실행
5. ⏳ 코드 리뷰 준비
6. ⏳ PR 생성

**필요 조건**:
- Atlassian MCP 서버 연결
- Jira 계정 접근 권한
- 프로젝트가 Git 저장소로 관리될 것

**MCP 통합**:
- `atlassian`: Jira API 조회 및 이슈 관리
- `serena`: 프로젝트 컨텍스트 및 메모리 관리

---

## 커맨드 작성 가이드

### 파일 구조

커맨드 파일은 Markdown 형식으로 작성되며, 다음 구조를 따릅니다:

```markdown
---
description: 커맨드 설명 (한 줄)
tags: [project, gitignored]
---

# 커맨드 제목

## 입력 형식
사용자 입력 예시 및 형식

## 실행 단계
1. 첫 번째 단계
2. 두 번째 단계
...

## 출력 형식
결과 출력 예시

## MCP 도구 사용 순서
사용할 MCP 도구 목록

## 에러 처리
예상되는 에러와 처리 방법

## 사용 예시
실제 사용 예시
```

### Front Matter

```yaml
---
description: 커맨드의 간단한 설명 (필수)
tags: [project, gitignored]  # 프로젝트 전용, gitignore 대상
name: command-name  # 선택사항, 파일명과 다를 경우
category: utility | analysis | automation  # 선택사항
complexity: simple | intermediate | advanced  # 선택사항
mcp-servers: ["server1", "server2"]  # 사용하는 MCP 서버
personas: ["analyzer", "architect"]  # 활성화할 페르소나
---
```

### 작성 원칙

1. **명확한 목적**: 커맨드가 해결하는 문제를 명확히 정의
2. **구조화된 단계**: 실행 단계를 명확한 순서로 나열
3. **도구 활용**: 적절한 MCP 도구와 네이티브 도구 조합
4. **에러 처리**: 예상 가능한 에러와 대응 방법 문서화
5. **예시 제공**: 실제 사용 예시로 이해도 향상

### 베스트 프랙티스

**DO**:
- MCP 도구를 우선적으로 활용
- 단계별 실행 순서 명확히 정의
- 한국어로 프로세스 설명, 기술 용어는 영어 유지
- 출력 형식을 구조화하여 일관성 유지
- 에러 상황에 대한 대체 방안 제공

**DON'T**:
- 너무 복잡한 워크플로우 (3-7 단계 권장)
- 사용자 환경 가정 (환경 설정 명시)
- 에러 처리 생략
- 불명확한 출력 형식

## MCP 서버 통합

커맨드에서 활용 가능한 주요 MCP 서버:

### atlassian
- **용도**: Jira, Confluence 연동
- **주요 기능**: 이슈 조회, 페이지 생성, 코멘트 추가
- **사용 예**: `/jira-task`

### sequential-thinking
- **용도**: 복잡한 다단계 추론 및 분석
- **주요 기능**: 구조화된 사고, 가설 검증, 체계적 문제 해결
- **사용 예**: `/sc:gemini-review`

### serena
- **용도**: 프로젝트 메모리 및 세션 관리
- **주요 기능**: 심볼 검색, 메모리 저장, 컨텍스트 유지
- **사용 예**: 프로젝트 컨텍스트 로드/저장

### context7
- **용도**: 공식 라이브러리 문서 조회
- **주요 기능**: 프레임워크 패턴, API 문서, 베스트 프랙티스
- **사용 예**: 라이브러리 사용법 참조

### magic
- **용도**: UI 컴포넌트 생성
- **주요 기능**: 21st.dev 패턴 기반 컴포넌트 생성
- **사용 예**: React/Vue 컴포넌트 자동 생성

### playwright
- **용도**: 브라우저 자동화 및 E2E 테스트
- **주요 기능**: 실제 브라우저 인터랙션, 스크린샷, 접근성 테스트
- **사용 예**: UI 테스트 자동화

## 추가 리소스

- [Claude Code 문서](https://docs.claude.com/en/docs/claude-code)
- [MCP 서버 가이드](../.claude/) - MCP 서버별 상세 문서 참조
- [SuperClaude 프레임워크](../.claude/PRINCIPLES.md)

## 기여 가이드

새로운 커맨드를 추가하려면:

1. `.claude/commands/` 디렉토리에 `command-name.md` 파일 생성
2. 위의 가이드라인에 따라 커맨드 작성
3. 이 README에 커맨드 문서 추가
4. 기능 브랜치 생성 후 PR 제출

**파일 네이밍**:
- 소문자와 하이픈 사용: `jira-task.md`
- 명확하고 설명적인 이름
- 프로젝트 전용 커맨드는 `tags: [project]` 설정
