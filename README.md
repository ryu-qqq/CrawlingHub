# CrawlingHub

분산 크롤링 시스템을 위한 엔터프라이즈급 백엔드 플랫폼

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.x](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-blue.svg)](https://gradle.org/)

## Overview

CrawlingHub는 다양한 마켓플레이스에서 상품 및 판매자 데이터를 수집하고 처리하는 분산 크롤링 시스템입니다.
헥사고날 아키텍처(Ports & Adapters)와 도메인 주도 설계(DDD)를 기반으로 구축되었습니다.

### 주요 기능

- **크롤링 태스크 관리**: 스케줄 기반 크롤링 태스크 생성 및 상태 관리
- **분산 워커 시스템**: SQS 기반 비동기 크롤링 작업 처리
- **재시도 메커니즘**: 실패한 태스크에 대한 자동 재시도 및 복구
- **UserAgent 관리**: 동적 UserAgent 풀 관리로 차단 방지
- **상품 데이터 수집**: 마켓플레이스별 상품 정보 수집 및 정규화

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Bootstrap Layer                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │
│  │  Web API        │  │  Scheduler      │  │  Crawl Worker   │     │
│  │  (REST)         │  │  (Batch)        │  │  (SQS Consumer) │     │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘     │
└───────────┼─────────────────────┼─────────────────────┼─────────────┘
            │                     │                     │
┌───────────┼─────────────────────┼─────────────────────┼─────────────┐
│           │         Adapter-In (Driving)              │             │
│  ┌────────┴────────┐                      ┌───────────┴───────┐    │
│  │  REST API       │                      │  SQS Listener     │    │
│  └────────┬────────┘                      └───────────┬───────┘    │
└───────────┼───────────────────────────────────────────┼─────────────┘
            │                                           │
┌───────────┴───────────────────────────────────────────┴─────────────┐
│                        Application Layer                             │
│           (Use Cases, Commands, Queries, Services)                   │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
┌───────────────────────────────┴─────────────────────────────────────┐
│                          Domain Layer                                │
│    ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐          │
│    │  Task    │  │ Schedule │  │ Product  │  │  Seller  │  ...     │
│    │Aggregate │  │Aggregate │  │Aggregate │  │Aggregate │          │
│    └──────────┘  └──────────┘  └──────────┘  └──────────┘          │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
┌───────────────────────────────┴─────────────────────────────────────┐
│                      Adapter-Out (Driven)                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │
│  │ MySQL       │  │ Redis       │  │ SQS         │  │ HTTP       │ │
│  │ Persistence │  │ Persistence │  │ Publisher   │  │ Client     │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Build | Gradle 8.x (Kotlin DSL) |
| Database | MySQL 8.0, Redis |
| Message Queue | AWS SQS |
| Event Bus | AWS EventBridge |
| Infrastructure | Terraform, AWS ECS |
| CI/CD | GitHub Actions, Atlantis |
| Monitoring | OpenTelemetry, Grafana |

## Project Structure

```
crawlinghub/
├── domain/                     # Domain Layer - Aggregates, Entities, Value Objects
├── application/                # Application Layer - Use Cases, Services
├── adapter-in/                 # Inbound Adapters
│   ├── rest-api/              # REST API Controller
│   └── sqs-listener/          # SQS Message Consumer
├── adapter-out/                # Outbound Adapters
│   ├── persistence-mysql/     # MySQL Repository Implementation
│   ├── persistence-redis/     # Redis Cache Implementation
│   ├── aws-sqs/               # SQS Publisher
│   ├── aws-eventbridge/       # EventBridge Publisher
│   ├── http-client/           # HTTP Client for External APIs
│   ├── fileflow-client/       # FileFlow Service Client
│   └── marketplace-client/    # Marketplace API Client
├── bootstrap/                  # Runnable Applications
│   ├── bootstrap-web-api/     # REST API Application
│   ├── bootstrap-scheduler/   # Scheduler Application
│   └── bootstrap-crawl-worker/# Crawl Worker Application
├── integration-test/           # Integration Tests
├── terraform/                  # Infrastructure as Code
├── local-dev/                  # Local Development Environment
├── docs/                       # Documentation
│   ├── coding_convention/     # Coding Standards (88 rules)
│   ├── design/                # Architecture Design Docs
│   ├── prd/                   # Product Requirements
│   └── guide/                 # Development Guides
└── config/                     # Build Configuration
    ├── checkstyle/            # Checkstyle Rules
    ├── spotbugs/              # SpotBugs Configuration
    └── pmd/                   # PMD Rules
```

## Getting Started

### Prerequisites

- Java 21
- Docker & Docker Compose
- Gradle 8.x

### Local Development

```bash
# 1. Clone repository
git clone https://github.com/ryu-qqq/crawlinghub.git
cd crawlinghub

# 2. Start local infrastructure
cd local-dev
docker-compose up -d

# 3. Build project
./gradlew build

# 4. Run applications
./gradlew :bootstrap:bootstrap-web-api:bootRun
./gradlew :bootstrap:bootstrap-scheduler:bootRun
./gradlew :bootstrap:bootstrap-crawl-worker:bootRun
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run with coverage report
./gradlew test jacocoTestReport

# Run integration tests
./gradlew :integration-test:test
```

## Code Quality

이 프로젝트는 엄격한 코드 품질 기준을 적용합니다:

| Tool | Purpose | Configuration |
|------|---------|---------------|
| **Checkstyle** | Code Style | Google Java Style |
| **SpotBugs** | Bug Detection | Max Effort |
| **PMD** | Static Analysis | Custom Rules |
| **Spotless** | Code Formatting | Google Java Format (AOSP) |
| **JaCoCo** | Coverage | Domain: 90%, Application: 80% |
| **ArchUnit** | Architecture Tests | Hexagonal Rules |

### Lombok 금지 정책

이 프로젝트는 **Lombok 사용을 금지**합니다. 모든 모듈은 순수 Java로 작성되어야 합니다.

```bash
# Lombok 검증
./gradlew checkNoLombok
```

## Domain Model

### Core Aggregates

| Aggregate | Description |
|-----------|-------------|
| **CrawlTask** | 크롤링 작업 단위 (상태 머신 기반) |
| **CrawlSchedule** | 크롤링 스케줄 정의 |
| **CrawlExecution** | 실행 이력 및 결과 |
| **Product** | 수집된 상품 데이터 |
| **Seller** | 판매자 정보 |
| **UserAgent** | UserAgent 풀 관리 |

### Task State Machine

```
WAITING → PUBLISHED → RUNNING → SUCCESS
                        ↓
                      FAILED → RETRY → PUBLISHED
                        ↓
                     TIMEOUT → RETRY → PUBLISHED
```

## Documentation

- [Coding Convention](./docs/coding_convention/) - 88개 코딩 규칙
- [Architecture Design](./docs/design/) - 아키텍처 설계 문서
- [Development Guide](./docs/guide/) - 개발 가이드

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Commit Convention

```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 변경
style: 코드 포맷팅
refactor: 코드 리팩토링
test: 테스트 추가/수정
chore: 빌드/설정 변경
```

## License

This project is proprietary software. All rights reserved.

## Contact

- **Maintainer**: Sangwon Ryu
- **Repository**: [github.com/ryu-qqq/crawlinghub](https://github.com/ryu-qqq/crawlinghub)
