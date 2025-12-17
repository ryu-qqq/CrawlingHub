# ========================================
# ECS Service: crawl-worker
# ========================================
# SQS message consumer for crawling task execution
# No ALB (background worker)
# Consumes from SQS queues, executes HTTP crawling
# Using Infrastructure repository modules
# ========================================

# ========================================
# Common Tags (for governance)
# ========================================
locals {
  common_tags = {
    environment  = var.environment
    service_name = "${var.project_name}-crawl-worker"
    team         = "platform-team"
    owner        = "platform@ryuqqq.com"
    cost_center  = "engineering"
    project      = var.project_name
    data_class   = "confidential"
  }
}

# ========================================
# ECR Repository Reference
# ========================================
data "aws_ecr_repository" "crawl_worker" {
  name = "${var.project_name}-crawl-worker-${var.environment}"
}

# ========================================
# ECS Cluster Reference (from ecs-cluster)
# ========================================
data "aws_ecs_cluster" "main" {
  cluster_name = "${var.project_name}-cluster-${var.environment}"
}

# ========================================
# Service Token Secret (for internal service communication)
# ========================================
data "aws_ssm_parameter" "service_token_secret" {
  name = "/shared/security/service-token-secret"
}

# ========================================
# Security Group (using Infrastructure module)
# ========================================

module "ecs_security_group" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/security-group?ref=main"

  name        = "${var.project_name}-crawl-worker-sg-${var.environment}"
  description = "Security group for crawl-worker ECS tasks"
  vpc_id      = local.vpc_id

  # Custom type for crawl-worker (egress only for HTTP requests)
  type = "custom"

  # No ingress rules - crawl-worker doesn't expose any ports

  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ========================================
# IAM Roles (using Infrastructure module)
# ========================================

# ECS Task Execution Role
module "crawl_worker_task_execution_role" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/iam-role-policy?ref=main"

  role_name = "${var.project_name}-crawl-worker-execution-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  attach_aws_managed_policies = [
    "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
  ]

  enable_secrets_manager_policy = true
  secrets_manager_secret_arns   = [data.aws_secretsmanager_secret.rds.arn]

  custom_inline_policies = {
    ssm-access = {
      policy = jsonencode({
        Version = "2012-10-17"
        Statement = [
          {
            Effect = "Allow"
            Action = [
              "ssm:GetParameters",
              "ssm:GetParameter"
            ]
            Resource = [
              "arn:aws:ssm:${var.aws_region}:*:parameter/shared/*"
            ]
          }
        ]
      })
    }
  }

  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ECS Task Role
module "crawl_worker_task_role" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/iam-role-policy?ref=main"

  role_name = "${var.project_name}-crawl-worker-task-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  custom_inline_policies = {
    sqs-access = {
      policy = jsonencode({
        Version = "2012-10-17"
        Statement = [
          {
            Effect = "Allow"
            Action = [
              "sqs:ReceiveMessage",
              "sqs:DeleteMessage",
              "sqs:GetQueueAttributes",
              "sqs:SendMessage",
              "sqs:ChangeMessageVisibility"
            ]
            Resource = [
              "arn:aws:sqs:${var.aws_region}:*:${var.project_name}-*",
              "arn:aws:sqs:${var.aws_region}:*:prod-monitoring-sqs-${var.project_name}-*"
            ]
          }
        ]
      })
    }
    adot-amp-access = {
      policy = jsonencode({
        Version = "2012-10-17"
        Statement = [
          {
            Sid    = "AMPRemoteWrite"
            Effect = "Allow"
            Action = [
              "aps:RemoteWrite"
            ]
            Resource = "arn:aws:aps:${var.aws_region}:*:workspace/*"
          },
          {
            Sid    = "XRayTracing"
            Effect = "Allow"
            Action = [
              "xray:PutTraceSegments",
              "xray:PutTelemetryRecords",
              "xray:GetSamplingRules",
              "xray:GetSamplingTargets",
              "xray:GetSamplingStatisticSummaries"
            ]
            Resource = "*"
          },
          {
            Sid    = "S3OtelConfigAccess"
            Effect = "Allow"
            Action = [
              "s3:GetObject"
            ]
            Resource = "arn:aws:s3:::prod-connectly/otel-config/*"
          }
        ]
      })
    }
  }

  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ========================================
# CloudWatch Log Group (using Infrastructure module)
# ========================================

module "crawl_worker_logs" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/cloudwatch-log-group?ref=main"

  name              = "/aws/ecs/${var.project_name}-crawl-worker-${var.environment}/application"
  retention_in_days = 30

  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ========================================
# ADOT Sidecar (Custom definition with S3 URL - bypasses CDN cache)
# ========================================

locals {
  # Use S3 directly to bypass CDN cache issues
  # ADOT requires format: s3://bucket.s3.region.amazonaws.com/path
  otel_config_s3_url = "s3://prod-connectly.s3.ap-northeast-2.amazonaws.com/otel-config/crawlinghub-crawl-worker/otel-config.yaml"

  adot_container_definition = {
    name      = "adot-collector"
    image     = "public.ecr.aws/aws-observability/aws-otel-collector:latest"
    cpu       = 256
    memory    = 512
    essential = false

    command = [
      "--config=${local.otel_config_s3_url}"
    ]

    portMappings = [
      {
        containerPort = 4317
        protocol      = "tcp"
      },
      {
        containerPort = 4318
        protocol      = "tcp"
      }
    ]

    environment = [
      {
        name  = "AWS_REGION"
        value = var.aws_region
      },
      {
        name  = "AMP_ENDPOINT"
        value = local.amp_remote_write_url
      },
      {
        name  = "SERVICE_NAME"
        value = "${var.project_name}-crawl-worker"
      },
      {
        name  = "APP_PORT"
        value = "8082"
      },
      {
        name  = "CLUSTER_NAME"
        value = data.aws_ecs_cluster.main.cluster_name
      },
      {
        name  = "ENVIRONMENT"
        value = var.environment
      }
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = module.crawl_worker_logs.log_group_name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "adot"
      }
    }
  }
}

# ========================================
# ECS Service (using Infrastructure module)
# ========================================

module "ecs_service" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecs-service?ref=main"

  # Service Configuration
  name            = "${var.project_name}-crawl-worker-${var.environment}"
  cluster_id      = data.aws_ecs_cluster.main.arn
  container_name  = "crawl-worker"
  container_image = "${data.aws_ecr_repository.crawl_worker.repository_url}:${var.image_tag}"
  container_port  = 8082
  cpu             = var.crawl_worker_cpu
  memory          = var.crawl_worker_memory
  desired_count   = var.crawl_worker_desired_count

  # IAM Roles
  execution_role_arn = module.crawl_worker_task_execution_role.role_arn
  task_role_arn      = module.crawl_worker_task_role.role_arn

  # Network Configuration
  subnet_ids         = local.private_subnets
  security_group_ids = [module.ecs_security_group.security_group_id]
  assign_public_ip   = false

  # No Load Balancer for crawl-worker (background worker)
  load_balancer_config = null

  # Container Environment Variables
  container_environment = [
    { name = "SPRING_PROFILES_ACTIVE", value = var.environment },
    { name = "DB_HOST", value = local.rds_host },
    { name = "DB_PORT", value = local.rds_port },
    { name = "DB_NAME", value = local.rds_dbname },
    { name = "DB_USER", value = local.rds_username },
    { name = "REDIS_HOST", value = local.redis_host },
    { name = "REDIS_PORT", value = local.redis_port },
    { name = "SQS_CRAWL_TASK_QUEUE_URL", value = "https://sqs.ap-northeast-2.amazonaws.com/646886795421/prod-monitoring-sqs-crawlinghub-crawling-task" },
    { name = "SQS_EVENTBRIDGE_TRIGGER_QUEUE_URL", value = "https://sqs.ap-northeast-2.amazonaws.com/646886795421/prod-monitoring-sqs-crawlinghub-eventbridge-trigger" },
    { name = "SQS_CRAWL_TASK_DLQ_URL", value = "https://sqs.ap-northeast-2.amazonaws.com/646886795421/prod-monitoring-sqs-crawlinghub-crawling-task-dlq" },
    # Fileflow Client 설정 (이미지 업로드 서비스)
    { name = "FILEFLOW_BASE_URL", value = "http://fileflow-web-api-prod.connectly.local:8080" },
    { name = "FILEFLOW_CALLBACK_URL", value = "http://crawlinghub-web-api-prod.connectly.local:8080/api/v1/webhook/image-upload" },
    # Service Token 인증 활성화 (서버 간 내부 통신용)
    { name = "SECURITY_SERVICE_TOKEN_ENABLED", value = "true" }
  ]

  # Container Secrets
  container_secrets = [
    { name = "DB_PASSWORD", valueFrom = "${data.aws_secretsmanager_secret.rds.arn}:password::" },
    # Service Token Secret (서버 간 내부 통신 인증용)
    { name = "SECURITY_SERVICE_TOKEN_SECRET", valueFrom = data.aws_ssm_parameter.service_token_secret.arn }
  ]

  # Health Check
  health_check_command      = ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1"]
  health_check_start_period = 300 # Worker needs more time to initialize Redisson, Redis, SQS connections (increased for stability)

  # Logging (use existing log group)
  log_configuration = {
    log_driver = "awslogs"
    options = {
      "awslogs-group"         = module.crawl_worker_logs.log_group_name
      "awslogs-region"        = var.aws_region
      "awslogs-stream-prefix" = "crawl-worker"
    }
  }

  # ADOT Sidecar (using custom S3 config URL to bypass CDN cache)
  sidecars = [local.adot_container_definition]

  # Auto Scaling (SQS-based scaling)
  enable_autoscaling       = true
  autoscaling_min_capacity = 2
  autoscaling_max_capacity = 10
  autoscaling_target_cpu   = 70

  # Enable ECS Exec for debugging
  enable_execute_command = true

  # Deployment Configuration
  deployment_circuit_breaker_enable   = true
  deployment_circuit_breaker_rollback = true

  # Tagging
  environment  = var.environment
  service_name = "${var.project_name}-crawl-worker"
  team         = "platform-team"
  owner        = "platform@ryuqqq.com"
  cost_center  = "engineering"
  project      = var.project_name
  data_class   = "confidential"
}

# ========================================
# Outputs
# ========================================
output "service_name" {
  description = "ECS Service name"
  value       = module.ecs_service.service_name
}

output "task_definition_arn" {
  description = "Task definition ARN"
  value       = module.ecs_service.task_definition_arn
}

output "log_group_name" {
  description = "CloudWatch log group name"
  value       = module.crawl_worker_logs.log_group_name
}
