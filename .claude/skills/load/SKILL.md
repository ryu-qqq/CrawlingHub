---
name: load
description: 세션 시작 시 프로젝트 컨텍스트 로딩. Serena 활성화 + Spring Standards 규칙 캐싱 + 프로젝트 현황 스캔.
context: fork
agent: session-loader
allowed-tools: Read, Write, Glob, Grep, Bash
---

# /load

세션 시작 시 프로젝트 컨텍스트를 로딩합니다.

## 사용법

```bash
/load                    # 기본 로딩 (캐시 우선)
/load --refresh          # 규칙 인덱스 캐시 무시하고 새로 조회
/load --full             # 모든 메모리 상세 내용 포함
/load --status-only      # 프로젝트 현황만 (Git 상태, 브랜치)
```

## 수행 작업

1. Serena 프로젝트 활성화 (`activate_project`)
2. 기존 메모리 복원 (plans, epics, jira 등)
3. Spring Standards 규칙 인덱스 캐싱 (`list_rules` → Serena memory)
4. Git 상태 + 진행 중인 계획 스캔
5. 컨텍스트 요약 반환
