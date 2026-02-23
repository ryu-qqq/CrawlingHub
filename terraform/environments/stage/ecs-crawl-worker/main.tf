# ========================================
# ECS Service: crawl-worker (Stage)
# ========================================
# SQS message consumer for crawling task execution
# No ALB (background worker), No Auto Scaling
# Using Infrastructure modules
# ========================================

# ========================================
# Common Tags (for governance)
# ========================================
locals {
  common_tags = {
    environment  = var.environment
    service_name = "${var.project_name}-crawl-worker-${var.environment}"
    team         = "platform-team"
    owner        = "platform@ryuqqq.com"
    cost_center  = "engineering"
    project      = var.project_name
    data_class   = "internal"
  }
}

# ========================================
# ECR Repository Reference
# ========================================
data "aws_ecr_repository" "crawl_worker" {
  name = "${var.project_name}-crawl-worker-stage"
}

# ========================================
# ECS Cluster Reference (from ecs-cluster)
# ========================================
data "aws_ecs_cluster" "main" {
  cluster_name = "${var.project_name}-cluster-${var.environment}"
}

data "aws_caller_identity" "current" {}

# ========================================
# Service Token Secret (for internal service communication)
# ========================================
data "aws_ssm_parameter" "service_token_secret" {
  name = "/shared/security/service-token-secret"
}

# ========================================
# Sentry DSN (for error tracking)
# ========================================
data "aws_ssm_parameter" "sentry_dsn" {
  name = "/crawlinghub/sentry/dsn"
}

# ========================================
# KMS Key for CloudWatch Logs Encryption
# ========================================
resource "aws_kms_key" "logs" {
  description             = "KMS key for CrawlingHub crawl-worker-stage CloudWatch logs encryption"
  deletion_window_in_days = 30
  enable_key_rotation     = true

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "Enable IAM User Permissions"
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
        }
        Action   = "kms:*"
        Resource = "*"
      },
      {
        Sid    = "Allow CloudWatch Logs"
        Effect = "Allow"
        Principal = {
          Service = "logs.${var.aws_region}.amazonaws.com"
        }
        Action = [
          "kms:Encrypt*",
          "kms:Decrypt*",
          "kms:ReEncrypt*",
          "kms:GenerateDataKey*",
          "kms:Describe*"
        ]
        Resource = "*"
        Condition = {
          ArnLike = {
            "kms:EncryptionContext:aws:logs:arn" = "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/ecs/${var.project_name}-crawl-worker-${var.environment}/*"
          }
        }
      }
    ]
  })

  tags = merge(local.common_tags, {
    Name      = "${var.project_name}-crawl-worker-logs-kms-${var.environment}"
    Lifecycle = "staging"
    ManagedBy = "terraform"
  })
}

resource "aws_kms_alias" "logs" {
  name          = "alias/${var.project_name}-crawl-worker-logs-${var.environment}"
  target_key_id = aws_kms_key.logs.key_id
}

# ========================================
# Security Group (using Infrastructure module)
# ========================================

module "ecs_security_group" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/security-group?ref=main"

  name        = "${var.project_name}-crawl-worker-sg-${var.environment}"
  description = "Security group for crawl-worker ECS tasks (stage)"
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
              "arn:aws:ssm:${var.aws_region}:*:parameter/shared/*",
              "arn:aws:ssm:${var.aws_region}:*:parameter/crawlinghub/*"
            ]
          },
          {
            Effect = "Allow"
            Action = [
              "kms:Decrypt"
            ]
            Resource = [
              aws_kms_key.logs.arn
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

  # NOTE: SQS access policy is attached via aws_iam_role_policy_attachment below
  # (includes both SQS and KMS permissions from the centralized sqs stack)

  custom_inline_policies = {
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
# SQS Access Policy Attachment (from sqs stack)
# ========================================
resource "aws_iam_role_policy_attachment" "crawl_worker_sqs_access" {
  role       = module.crawl_worker_task_role.role_name
  policy_arn = local.sqs_access_policy_arn
}

# ========================================
# CloudWatch Log Group (using Infrastructure module)
# ========================================

module "crawl_worker_logs" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/cloudwatch-log-group?ref=main"

  name              = "/aws/ecs/${var.project_name}-crawl-worker-${var.environment}/application"
  retention_in_days = 14
  kms_key_id        = aws_kms_key.logs.arn

  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ========================================
# NOTE: Log Streaming to OpenSearch 제거 (Stage 환경)
# Stage 환경에서는 CloudWatch Logs만 사용
# ========================================

# ========================================
# ADOT Sidecar (using Infrastructure module)
# ========================================
module "adot_sidecar" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/adot-sidecar?ref=main"

  project_name              = var.project_name
  service_name              = "crawl-worker"
  aws_region                = var.aws_region
  amp_workspace_arn         = local.amp_workspace_arn
  amp_remote_write_endpoint = local.amp_remote_write_url
  log_group_name            = module.crawl_worker_logs.log_group_name
  app_port                  = 8082
  cluster_name              = data.aws_ecs_cluster.main.cluster_name
  environment               = var.environment
  config_bucket             = "prod-connectly"
  config_version            = "20251215"
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
    { name = "SPRING_PROFILES_ACTIVE", value = "stage" },
    { name = "DB_HOST", value = local.rds_host },
    { name = "DB_PORT", value = local.rds_port },
    { name = "DB_NAME", value = local.rds_dbname },
    { name = "DB_USER", value = local.rds_username },
    { name = "REDIS_HOST", value = local.redis_host },
    { name = "REDIS_PORT", value = tostring(local.redis_port) },
    # SQS Main Queues (from SSM parameters)
    { name = "SQS_CRAWL_TASK_QUEUE_URL", value = local.sqs_crawl_task_queue_url },
    { name = "SQS_PRODUCT_IMAGE_QUEUE_URL", value = local.sqs_product_image_queue_url },
    { name = "SQS_PRODUCT_SYNC_QUEUE_URL", value = local.sqs_product_sync_queue_url },
    # Fileflow Client 설정 (Stage 내부 VPC Service Discovery 통신)
    { name = "FILEFLOW_BASE_URL", value = "http://fileflow-web-api-stage.connectly.local:8080" },
    { name = "FILEFLOW_CALLBACK_URL", value = "http://crawlinghub-web-api-stage.connectly.local:8080/api/v1/webhook/image-upload" },
    # Service Token 인증 활성화 (서버 간 내부 통신용)
    { name = "SECURITY_SERVICE_TOKEN_ENABLED", value = "true" }
  ]

  # Container Secrets
  container_secrets = [
    { name = "DB_PASSWORD", valueFrom = "${data.aws_secretsmanager_secret.rds.arn}:password::" },
    # Service Token Secret (서버 간 내부 통신 인증용)
    { name = "SECURITY_SERVICE_TOKEN_SECRET", valueFrom = data.aws_ssm_parameter.service_token_secret.arn },
    # Sentry DSN (에러 트래킹용)
    { name = "SENTRY_DSN", valueFrom = data.aws_ssm_parameter.sentry_dsn.arn }
  ]

  # Health Check
  health_check_command      = ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1"]
  health_check_start_period = 120

  # Logging
  log_configuration = {
    log_driver = "awslogs"
    options = {
      "awslogs-group"         = module.crawl_worker_logs.log_group_name
      "awslogs-region"        = var.aws_region
      "awslogs-stream-prefix" = "crawl-worker"
    }
  }

  # ADOT Sidecar
  sidecars = [module.adot_sidecar.container_definition]

  # Auto Scaling - Stage 환경에서는 비활성화
  enable_autoscaling = false

  # Enable ECS Exec for debugging
  enable_execute_command = true

  # Deployment Configuration
  deployment_circuit_breaker_enable   = true
  deployment_circuit_breaker_rollback = true

  # Tagging
  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
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

output "kms_key_arn" {
  description = "KMS key ARN for logs encryption"
  value       = aws_kms_key.logs.arn
}
