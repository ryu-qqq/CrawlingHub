# ADOT Sidecar Module

AWS Distro for OpenTelemetry (ADOT) 사이드카 컨테이너를 ECS Task에 추가하기 위한 Terraform 모듈.

## 목적

Spring Boot 애플리케이션의 메트릭을 Amazon Managed Prometheus (AMP)로 수집하여 Amazon Managed Grafana (AMG)에서 시각화.

## 사용법

```hcl
module "adot_sidecar" {
  source = "../modules/adot-sidecar"

  project_name      = "crawlinghub"
  service_name      = "web-api"
  aws_region        = "ap-northeast-2"
  amp_workspace_arn = "arn:aws:aps:ap-northeast-2:646886795421:workspace/ws-xxxxx"
  log_group_name    = "/ecs/crawlinghub-web-api-prod"
}

# Task Definition에 ADOT 컨테이너 추가
resource "aws_ecs_task_definition" "web_api" {
  # ... 기존 설정 ...

  container_definitions = jsonencode([
    # 메인 컨테이너
    {
      name  = "web-api"
      image = "..."
      # ...
    },
    # ADOT 사이드카 추가
    module.adot_sidecar.container_definition
  ])
}

# Task Role에 IAM 정책 추가
resource "aws_iam_role_policy" "adot_access" {
  name   = "adot-amp-access"
  role   = aws_iam_role.ecs_task.id
  policy = module.adot_sidecar.iam_policy_document
}
```

## Outputs

| Name | Description |
|------|-------------|
| `container_definition` | ECS task에 추가할 ADOT 컨테이너 정의 |
| `iam_policy_document` | AMP 접근을 위한 IAM 정책 |
| `otel_config_url` | OTEL 설정 파일을 업로드해야 할 URL |
