# OpenTelemetry 환경 변수 검증 가이드

## 개요
OTEL Collector가 올바르게 동작하기 위해 필요한 환경 변수가 ECS Task Definition에 설정되어 있는지 검증합니다.

## 필수 환경 변수

### 1. AWS_REGION
- **용도**: AWS X-Ray 및 SigV4 인증에 사용
- **설정 위치**: 
  - `terraform/ecs-web-api/main.tf:394-396`
  - `terraform/ecs-scheduler/main.tf:259-261`
  - `terraform/ecs-crawl-worker/main.tf:262-264`
- **값**: `var.aws_region` (Terraform 변수)

### 2. AMP_ENDPOINT
- **용도**: Amazon Managed Prometheus Remote Write 엔드포인트
- **설정 위치**:
  - `terraform/ecs-web-api/main.tf:398-400`
  - `terraform/ecs-scheduler/main.tf:263-265`
  - `terraform/ecs-crawl-worker/main.tf:266-268`
- **값**: `local.amp_remote_write_url` (Terraform local 변수)

## 검증 방법

### 배포 전 검증 (Terraform)

```bash
# 1. Terraform 변수 확인
cd terraform/ecs-web-api
terraform console
> var.aws_region
> local.amp_remote_write_url

# 2. Terraform Plan으로 환경 변수 확인
terraform plan | grep -A 5 "AWS_REGION\|AMP_ENDPOINT"
```

### 배포 후 검증 (ECS)

```bash
# 1. Web API Task Definition 확인
aws ecs describe-task-definition \
  --task-definition crawlinghub-web-api-prod \
  --query 'taskDefinition.containerDefinitions[?name==`adot-collector`].environment' \
  --output table

# 2. Scheduler Task Definition 확인
aws ecs describe-task-definition \
  --task-definition crawlinghub-scheduler-prod \
  --query 'taskDefinition.containerDefinitions[?name==`adot-collector`].environment' \
  --output table

# 3. Worker Task Definition 확인
aws ecs describe-task-definition \
  --task-definition crawlinghub-crawl-worker-prod \
  --query 'taskDefinition.containerDefinitions[?name==`adot-collector`].environment' \
  --output table
```

### 런타임 검증 (컨테이너 내부)

```bash
# 1. ECS 태스크 ID 확인
aws ecs list-tasks \
  --cluster crawlinghub-cluster-prod \
  --service-name crawlinghub-web-api-prod

# 2. ECS Exec로 컨테이너 접속
aws ecs execute-command \
  --cluster crawlinghub-cluster-prod \
  --task <TASK_ID> \
  --container adot-collector \
  --interactive \
  --command "/bin/sh"

# 3. 환경 변수 확인
echo $AWS_REGION
echo $AMP_ENDPOINT
```

## 예상 결과

### 정상 케이스
```
AWS_REGION=ap-northeast-2
AMP_ENDPOINT=https://aps-workspaces.ap-northeast-2.amazonaws.com/workspaces/ws-xxxxx/api/v1/remote_write
```

### 오류 케이스

#### 1. 환경 변수 누락
```
Error: Failed to start OTEL Collector
Cause: environment variable AWS_REGION not set
```

**해결 방법**: Terraform에서 `var.aws_region` 설정 확인

#### 2. AMP 엔드포인트 오류
```
Error: Failed to export metrics to Prometheus
Cause: invalid AMP endpoint URL
```

**해결 방법**: Terraform에서 `local.amp_remote_write_url` 설정 확인

## CloudWatch Logs 모니터링

```bash
# OTEL Collector 시작 로그 확인
aws logs tail /aws/ecs/crawlinghub-web-api-prod/adot \
  --follow \
  --filter-pattern "AWS_REGION\|AMP_ENDPOINT\|error\|failed"

# 환경 변수 관련 에러 검색
aws logs filter-log-events \
  --log-group-name /aws/ecs/crawlinghub-web-api-prod/adot \
  --filter-pattern "environment variable" \
  --start-time $(date -u -d '10 minutes ago' +%s)000
```

## 검증 체크리스트

배포 전:
- [ ] Terraform 변수 `var.aws_region` 설정 확인
- [ ] Terraform local 변수 `local.amp_remote_write_url` 설정 확인
- [ ] Terraform Plan에서 환경 변수 주입 확인

배포 후:
- [ ] ECS Task Definition에서 환경 변수 확인 (3개 서비스)
- [ ] OTEL Collector 컨테이너 정상 시작 확인
- [ ] CloudWatch Logs에서 환경 변수 관련 에러 없음 확인
- [ ] Prometheus 메트릭 수집 정상 작동 확인

## 참고 자료

- [AWS Distro for OpenTelemetry - Environment Variables](https://aws-otel.github.io/docs/getting-started/environment-variables)
- [Amazon Managed Prometheus - Remote Write](https://docs.aws.amazon.com/prometheus/latest/userguide/AMP-onboard-ingest-metrics-remote-write.html)
- [AWS X-Ray - Configuration](https://docs.aws.amazon.com/xray/latest/devguide/xray-sdk-java-configuration.html)
