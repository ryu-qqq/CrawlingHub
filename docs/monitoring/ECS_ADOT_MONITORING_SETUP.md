# ECS + ADOT + AMP + Grafana 모니터링 구축 가이드

> Spring Boot 애플리케이션을 ECS에 배포하고, ADOT 사이드카를 통해 Prometheus 메트릭을 수집하여 Amazon Managed Grafana에서 시각화하는 전체 과정을 정리한 문서입니다.

---

## 1. 아키텍처 개요

```
┌─────────────────────────────────────────────────────────────┐
│  ECS Task                                                   │
│  ┌──────────────────┐    ┌──────────────────────────────┐   │
│  │  Spring Boot App  │    │  ADOT Sidecar (Collector)    │   │
│  │                   │    │                              │   │
│  │  /actuator/       │◄───│  prometheus receiver         │   │
│  │   prometheus      │    │  (scrape every 30s)          │   │
│  │   :8080           │    │                              │   │
│  └──────────────────┘    │  resourcedetection (ECS)     │   │
│                           │  resource (env, cluster)     │   │
│                           │  prometheusremotewrite ──────┼──►  AMP
│                           │  awsxray exporter ───────────┼──►  X-Ray
│                           └──────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                                                          │
                                                          ▼
                                                ┌─────────────────┐
                                                │  Amazon Managed  │
                                                │    Grafana       │
                                                │  (Dashboards)    │
                                                └─────────────────┘
```

### 데이터 흐름

1. **Spring Boot** → `/actuator/prometheus` 엔드포인트로 메트릭 노출
2. **ADOT Sidecar** → 30초 간격으로 스크래핑, 리소스 라벨 부착
3. **AMP (Amazon Managed Prometheus)** → Remote Write로 메트릭 저장
4. **Grafana** → AMP를 데이터소스로 대시보드 시각화

---

## 2. 사전 준비 (Prerequisites)

### 2.1 Spring Boot 의존성

> **핵심**: `micrometer-registry-prometheus`가 클래스패스에 없으면 `/actuator/prometheus` 엔드포인트가 아예 활성화되지 않습니다. ADOT가 스크래핑해도 404 → `up=0` → 메트릭 수집 불가.

**build.gradle (각 bootstrap 모듈)**:
```gradle
dependencies {
    // Prometheus metrics exporter (ADOT sidecar scraping 용)
    implementation 'io.micrometer:micrometer-registry-prometheus'

    // Actuator (메트릭 엔드포인트)
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

**application.yml**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    tags:
      application: ${spring.application.name}
```

**Security 설정** (Spring Security 사용 시):
```java
// /actuator/** 경로는 인증 없이 접근 가능해야 함
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/actuator/**").permitAll()
    // ...
);
```

### 2.2 검증 방법

로컬에서 먼저 확인:
```bash
# 앱 실행 후
curl http://localhost:8080/actuator/prometheus

# 아래와 같은 메트릭이 출력되어야 함
# jvm_memory_used_bytes{area="heap",...} 1.234567E8
# http_server_requests_seconds_count{...} 42
```

`/actuator/prometheus`가 404라면 `micrometer-registry-prometheus` 의존성 누락.

---

## 3. OTEL Config 작성

### 3.1 구조

ADOT Collector는 YAML 설정 파일로 동작합니다. S3에 업로드하면 ECS 태스크 시작 시 자동으로 가져갑니다.

**S3 경로 규칙**: `s3://{버킷}/otel-config/{프로젝트}-{서비스}/otel-config.yaml`

예시:
```
s3://prod-connectly/otel-config/crawlinghub-web-api/otel-config.yaml
s3://prod-connectly/otel-config/crawlinghub-scheduler/otel-config.yaml
s3://prod-connectly/otel-config/crawlinghub-crawl-worker/otel-config.yaml
```

### 3.2 전체 설정 파일 (템플릿)

아래는 서비스별로 복사해서 사용하는 템플릿입니다. `{변경필요}` 부분만 수정하세요.

```yaml
# AWS Distro for OpenTelemetry (ADOT) Collector Configuration
# Service: {프로젝트}-{서비스명} (port {앱포트})

receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318

  prometheus:
    config:
      global:
        scrape_interval: 30s
        scrape_timeout: 10s

      scrape_configs:
        - job_name: '{프로젝트}-{서비스}-metrics'    # ← 변경
          scrape_interval: 30s
          static_configs:
            - targets: ['localhost:{앱포트}']         # ← 변경 (8080, 8081, 8082 등)
          metrics_path: /actuator/prometheus
          metric_relabel_configs:
            - source_labels: [__name__]
              regex: '(http_.*|jvm_.*|process_.*|system_.*|application_.*|business_.*|spring_.*|hikaricp_.*|{프로젝트}_.*)'  # ← 커스텀 메트릭 prefix 추가
              action: keep

  awsecscontainermetrics:
    collection_interval: 30s

processors:
  batch:
    timeout: 60s
    send_batch_size: 1000

  memory_limiter:
    check_interval: 5s
    limit_mib: 410
    spike_limit_mib: 128

  # ECS 태스크 메타데이터 자동 감지 - 각 태스크별 고유 식별자 부여
  resourcedetection:
    detectors: [env, ecs]
    timeout: 5s
    override: true
    ecs:
      resource_attributes:
        aws.ecs.task.arn:
          enabled: true
        aws.ecs.cluster.arn:
          enabled: true
        aws.ecs.task.id:
          enabled: true
        cloud.availability_zone:
          enabled: true

  resource:
    attributes:
      - key: environment
        value: ${ENVIRONMENT}               # ← terraform이 주입하는 환경변수
        action: upsert
      - key: cluster_name
        value: ${CLUSTER_NAME}              # ← terraform이 주입하는 환경변수
        action: upsert
      - key: service_name
        value: {서비스명}                    # ← 변경 (web-api, scheduler, crawl-worker 등)
        action: upsert

  metricstransform:
    transforms:
      - include: .*
        match_type: regexp
        action: update
        operations:
          - action: add_label
            new_label: platform
            new_value: ecs

exporters:
  awsxray:
    region: ${AWS_REGION}
    indexed_attributes:
      - http.method
      - http.status_code
      - http.route
      - rpc.service
      - rpc.method
      - db.system
      - db.name

  prometheusremotewrite:
    endpoint: ${AMP_ENDPOINT}
    auth:
      authenticator: sigv4auth
    resource_to_telemetry_conversion:
      enabled: true                         # ← resource attributes를 메트릭 라벨로 변환
    retry_on_failure:
      enabled: true
      initial_interval: 5s
      max_interval: 30s
      max_elapsed_time: 300s

extensions:
  sigv4auth:
    region: ${AWS_REGION}
    service: aps

  health_check:
    endpoint: :13133

service:
  extensions:
    - sigv4auth
    - health_check

  telemetry:
    logs:
      level: warn
      encoding: json
    metrics:
      level: detailed

  pipelines:
    metrics:
      receivers:
        - otlp
        - prometheus
        - awsecscontainermetrics
      processors:
        - memory_limiter
        - resourcedetection
        - resource
        - metricstransform
        - batch
      exporters:
        - prometheusremotewrite

    traces:
      receivers:
        - otlp
      processors:
        - memory_limiter
        - resourcedetection
        - resource
        - batch
      exporters:
        - awsxray
```

### 3.3 서비스별 변경 포인트

| 항목 | web-api | scheduler | crawl-worker |
|------|---------|-----------|--------------|
| job_name | `{프로젝트}-web-api-metrics` | `{프로젝트}-scheduler-metrics` | `{프로젝트}-worker-metrics` |
| targets port | `localhost:8080` | `localhost:8081` | `localhost:8082` |
| service_name | `web-api` | `scheduler` | `crawl-worker` |

### 3.4 metric_relabel_configs (중요!)

`action: keep` regex는 **화이트리스트** 방식입니다. 여기 포함되지 않은 메트릭은 전부 드롭됩니다.

**기본 포함**:
- `http_.*` — HTTP 요청 메트릭
- `jvm_.*` — JVM 메모리, GC, 스레드
- `process_.*` — 프로세스 CPU, 메모리
- `system_.*` — 시스템 메트릭
- `spring_.*` — Spring 프레임워크 메트릭
- `hikaricp_.*` — HikariCP 커넥션 풀

**프로젝트별 추가 필수**:
- `{프로젝트}_.*` — 커스텀 메트릭 (예: `crawlinghub_.*`, `fileflow_.*`)
- `scheduler_job_.*` — 스케줄러 메트릭 (observability-spring-boot-starter 사용 시)
- `sqs_consumer_.*` — SQS 컨슈머 메트릭 (observability-spring-boot-starter 사용 시)

> **자주 하는 실수**: 커스텀 메트릭 prefix를 regex에 안 넣으면 AMP에 도달하지 않아서 Grafana에서 "No data"가 됩니다.

---

## 4. S3 업로드

### 4.1 버킷 암호화 확인

```bash
aws s3api get-bucket-encryption --bucket {버킷명}
```

KMS 암호화가 설정되어 있으면 업로드 시 `--sse aws:kms` 필수:

```bash
# KMS 키 확인
KMS_KEY="arn:aws:kms:ap-northeast-2:{계정ID}:key/{키ID}"

# 업로드
aws s3 cp otel-config-web-api.yaml \
  s3://{버킷}/otel-config/{프로젝트}-web-api/otel-config.yaml \
  --sse aws:kms --sse-kms-key-id "$KMS_KEY"

aws s3 cp otel-config-scheduler.yaml \
  s3://{버킷}/otel-config/{프로젝트}-scheduler/otel-config.yaml \
  --sse aws:kms --sse-kms-key-id "$KMS_KEY"

aws s3 cp otel-config-worker.yaml \
  s3://{버킷}/otel-config/{프로젝트}-crawl-worker/otel-config.yaml \
  --sse aws:kms --sse-kms-key-id "$KMS_KEY"
```

### 4.2 적용

S3 업로드 후 **ECS 태스크 재시작** 필요 (ADOT 사이드카가 시작 시 S3에서 config를 가져옴):

```bash
# ECS 서비스 강제 재배포
aws ecs update-service \
  --cluster {프로젝트}-cluster-{환경} \
  --service {서비스명} \
  --force-new-deployment
```

---

## 5. Grafana 대시보드

### 5.1 대시보드 구성

| 대시보드 | 파일 | 용도 |
|----------|------|------|
| Overview | `dashboard.json` | 전체 서비스 통합 현황 (UP, RPS, Latency, Error Rate + 서비스별 collapsed row) |
| API | `dashboard-api.json` | API 서비스 상세 (Traffic, Latency, Endpoints by URI, Errors) |
| Scheduler | `dashboard-scheduler.json` | 스케줄러 상세 (Job Duration, Batch Items, Success Rate) |
| Worker | `dashboard-worker.json` | 워커 상세 (Task Processing, Duration, Error Rate) |

### 5.2 대시보드 공통 구조

모든 대시보드에 포함되는 섹션:

```
📊 Overview          — UP 인스턴스, 주요 KPI (stat panels)
🗄️ Database (HikariCP) — 커넥션 풀 사용량, Utilization, Acquire Time
☕ JVM Metrics        — Heap, Non-Heap, GC, Threads (태스크별 분리)
🖥️ ECS Resources     — CPU/Memory Utilization (CloudWatch)
```

서비스별 추가 섹션:

- **API**: Traffic & Latency, Errors & Status Codes, Endpoints by URI
- **Scheduler**: Job Duration, Batch Items, Success Rate
- **Worker**: Crawl Task Processing, Outbound Client, Error Rate

### 5.3 템플릿 변수

| 변수 | 타입 | 용도 |
|------|------|------|
| `$environment` | query (`label_values(up, environment)`) | prod/stage 환경 전환 |
| `$job` | constant 또는 query | 서비스별 Prometheus job 이름 |
| `$task_id` | query (`label_values(up{job="$job"}, aws_ecs_task_id)`) | ECS 태스크별 필터링 |
| `$cw_datasource` | datasource (CloudWatch) | CloudWatch 패널용 |

### 5.4 PromQL 패턴

모든 쿼리에 `job="$job",environment=~"$environment"` 필터가 포함되어야 합니다:

```promql
# 인스턴스 수
sum(up{job="$job",environment=~"$environment"})

# HTTP RPS
sum(rate(http_server_requests_seconds_count{job="$job",environment=~"$environment"}[1m]))

# HTTP 평균 레이턴시
sum(rate(http_server_requests_seconds_sum{job="$job",environment=~"$environment"}[1m]))
/ sum(rate(http_server_requests_seconds_count{job="$job",environment=~"$environment"}[1m]))

# JVM Heap (태스크별)
sum by (aws_ecs_task_id) (
  jvm_memory_used_bytes{job="$job",area="heap",environment=~"$environment",aws_ecs_task_id=~"$task_id"}
)

# HikariCP 커넥션 풀
hikaricp_connections_active{job="$job",environment=~"$environment"}

# 커스텀 메트릭 예시 (프로젝트별)
sum by (operation) (
  rate({프로젝트}_crawl_task_total{job="$job",environment=~"$environment"}[1m])
)
```

### 5.5 임포트 방법

1. Grafana 좌측 메뉴 → Dashboards → Import
2. JSON 파일 업로드 또는 내용 붙여넣기
3. Prometheus 데이터소스 UID 확인 (대시보드 내 `"uid": "ef4r3izgbak8wb"` → 본인 환경에 맞게 변경)
4. Import 클릭

> **주의**: 데이터소스 UID가 다른 환경이면 JSON 내 `"uid": "..."` 를 일괄 치환해야 합니다.

---

## 6. 새 프로젝트 적용 체크리스트

### Step 1: Spring Boot 설정
- [ ] `build.gradle`에 `micrometer-registry-prometheus` 추가
- [ ] `spring-boot-starter-actuator` 추가
- [ ] `application.yml`에 prometheus 엔드포인트 노출 설정
- [ ] Security 설정에서 `/actuator/**` permitAll
- [ ] 로컬에서 `curl localhost:{포트}/actuator/prometheus` 확인

### Step 2: OTEL Config 작성
- [ ] 템플릿 복사 후 서비스별 값 변경 (job_name, port, service_name)
- [ ] `metric_relabel_configs`의 keep regex에 **커스텀 메트릭 prefix** 추가
- [ ] `resource.attributes`에 `${ENVIRONMENT}`, `${CLUSTER_NAME}` 환경변수 사용

### Step 3: S3 업로드
- [ ] 버킷 KMS 암호화 확인
- [ ] 서비스별 OTEL config 업로드
- [ ] ECS 태스크 재시작 (ADOT가 새 config 로드)

### Step 4: 메트릭 수집 확인
- [ ] 2~3분 대기 후 AMP에서 `up{job="{프로젝트}-{서비스}-metrics"}` 쿼리
- [ ] 값이 **1**이면 정상, **0**이면 스크래핑 실패 (아래 트러블슈팅 참고)
- [ ] 커스텀 메트릭 쿼리해서 데이터 도달 확인

### Step 5: Grafana 대시보드
- [ ] 기존 대시보드 JSON 복사
- [ ] 프로젝트명/서비스명 일괄 치환 (`sed` 또는 에디터)
- [ ] 커스텀 메트릭 패널 수정 (프로젝트별 메트릭 이름에 맞게)
- [ ] 데이터소스 UID 확인/변경
- [ ] Grafana에 임포트

---

## 7. 트러블슈팅

### 7.1 `up` 메트릭이 0인 경우

**증상**: Grafana에서 모든 패널이 "No data", AMP에서 `up{job="..."}` 값이 0.

**원인과 해결**:

| 원인 | 확인 방법 | 해결 |
|------|----------|------|
| `micrometer-registry-prometheus` 누락 | `curl /actuator/prometheus` → 404 | build.gradle에 의존성 추가 |
| actuator 엔드포인트 미노출 | `curl /actuator` → prometheus 목록에 없음 | application.yml에 `exposure.include: prometheus` |
| Spring Security 차단 | `curl /actuator/prometheus` → 401/403 | SecurityConfig에서 `/actuator/**` permitAll |
| ADOT 포트 불일치 | OTEL config targets port ≠ 앱 port | targets port를 앱 실제 포트로 맞춤 |

### 7.2 일부 메트릭만 "No data"

**증상**: JVM, HTTP 메트릭은 보이는데 HikariCP, 커스텀 메트릭이 안 보임.

**원인**: OTEL config의 `metric_relabel_configs` → `action: keep` regex에 해당 prefix가 없음.

**해결**: regex에 누락된 prefix 추가:
```yaml
regex: '(http_.*|jvm_.*|...|hikaricp_.*|{프로젝트}_.*)'
#                                       ^^^^^^^^^^^^^^^^ 추가
```

### 7.3 "out of order sample" 에러 (AMP)

**증상**: ADOT 로그에 "out of order sample" 에러 반복.

**원인**: 여러 ECS 태스크가 동일한 `service_instance_id`로 메트릭을 보냄.

**해결**: `resourcedetection` 프로세서에 ECS 감지 추가 (태스크별 고유 ID 부여):
```yaml
processors:
  resourcedetection:
    detectors: [env, ecs]
    ecs:
      resource_attributes:
        aws.ecs.task.id:
          enabled: true
```

### 7.4 OTEL Collector self-scrape 충돌

**증상**: ADOT 자체 메트릭(`:8888`)을 scrape하면 여러 태스크 간 충돌.

**해결**: otel-collector self-scrape job을 **제거**합니다. 필요시 `awsecscontainermetrics` receiver로 대체.

```yaml
# 아래 job을 scrape_configs에서 제거
# - job_name: 'otel-collector'
#   static_configs:
#     - targets: ['localhost:8888']
```

### 7.5 S3 업로드 AccessDenied

**증상**: `aws s3 cp` 시 PutObject AccessDenied.

**원인**: 버킷에 KMS 암호화가 설정되어 있어서 일반 업로드가 거부됨.

**해결**:
```bash
# 1. 버킷 암호화 설정 확인
aws s3api get-bucket-encryption --bucket {버킷}

# 2. KMS 키 ID로 업로드
aws s3 cp config.yaml s3://{버킷}/{경로} \
  --sse aws:kms --sse-kms-key-id "{KMS 키 ARN}"
```

---

## 8. 환경별 설정 차이

prod와 stage는 **같은 OTEL config 파일**을 사용합니다. 환경별 차이는 terraform이 주입하는 환경변수로 처리:

| 환경변수 | prod | stage | 설정 위치 |
|----------|------|-------|----------|
| `ENVIRONMENT` | `prod` | `stage` | terraform → ECS task definition |
| `CLUSTER_NAME` | `{프로젝트}-cluster-prod` | `{프로젝트}-cluster-stage` | terraform → ECS task definition |
| `AWS_REGION` | `ap-northeast-2` | `ap-northeast-2` | terraform → ECS task definition |
| `AMP_ENDPOINT` | AMP workspace URL | AMP workspace URL | terraform → ECS task definition |

Grafana 대시보드에서 `$environment` 드롭다운으로 환경 전환 가능.

---

## 9. 파일 구조 (참고)

```
프로젝트/
├── otel-config-web-api.yaml        # OTEL config (web-api)
├── otel-config-scheduler.yaml      # OTEL config (scheduler)
├── otel-config-worker.yaml         # OTEL config (worker)
├── docs/
│   ├── grafana/
│   │   ├── dashboard.json          # Overview (전체 서비스 통합)
│   │   ├── dashboard-api.json      # API 상세
│   │   ├── dashboard-scheduler.json # Scheduler 상세
│   │   └── dashboard-worker.json   # Worker 상세
│   └── monitoring/
│       └── ECS_ADOT_MONITORING_SETUP.md  # 이 문서
├── bootstrap/
│   ├── bootstrap-web-api/build.gradle      # micrometer-registry-prometheus 추가
│   ├── bootstrap-scheduler/build.gradle
│   └── bootstrap-crawl-worker/build.gradle
└── terraform/
    └── modules/adot-sidecar/       # ADOT 사이드카 terraform 모듈
```
