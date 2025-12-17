# ========================================
# ECS Service: web-api
# ========================================
# REST API server with Cloud Map Service Discovery
# Using Infrastructure modules
# Internal access via: crawlinghub-web-api-prod.connectly.local:8080
# ========================================

# ========================================
# Common Tags (for governance)
# ========================================
locals {
  common_tags = {
    environment  = var.environment
    service_name = "${var.project_name}-web-api"
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
data "aws_ecr_repository" "web_api" {
  name = "${var.project_name}-web-api-${var.environment}"
}

# ========================================
# ECS Cluster Reference (from ecs-cluster)
# ========================================
data "aws_ecs_cluster" "main" {
  cluster_name = "${var.project_name}-cluster-${var.environment}"
}

# ========================================
# EventBridge Configuration (from SSM)
# ========================================
data "aws_ssm_parameter" "eventbridge_trigger_queue_arn" {
  name = "/${var.project_name}/sqs/eventbridge-trigger-queue-arn"
}

data "aws_ssm_parameter" "eventbridge_role_arn" {
  name = "/${var.project_name}/eventbridge/role-arn"
}

# ========================================
# Service Discovery Namespace (from shared infrastructure)
# ========================================
data "aws_ssm_parameter" "service_discovery_namespace_id" {
  name = "/shared/service-discovery/namespace-id"
}

# ========================================
# Service Token Secret (for internal service communication)
# ========================================
data "aws_ssm_parameter" "service_token_secret" {
  name = "/shared/security/service-token-secret"
}

# VPC data source for internal communication
data "aws_vpc" "main" {
  id = local.vpc_id
}

# ========================================
# Security Groups
# ========================================
module "ecs_security_group" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/security-group?ref=main"

  name        = "${var.project_name}-web-api-sg-${var.environment}"
  description = "Security group for web-api ECS tasks"
  vpc_id      = local.vpc_id

  type = "custom"

  custom_ingress_rules = [
    {
      from_port   = 8080
      to_port     = 8080
      protocol    = "tcp"
      cidr_block  = data.aws_vpc.main.cidr_block
      description = "Service Discovery - internal VPC communication"
    }
  ]

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
module "ecs_task_execution_role" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/iam-role-policy?ref=main"

  role_name = "${var.project_name}-web-api-execution-role-${var.environment}"

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
module "ecs_task_role" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/iam-role-policy?ref=main"

  role_name = "${var.project_name}-web-api-task-role-${var.environment}"

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
    eventbridge-access = {
      policy = jsonencode({
        Version = "2012-10-17"
        Statement = [
          {
            Effect = "Allow"
            Action = [
              "events:PutEvents",
              "events:PutRule",
              "events:PutTargets",
              "events:DeleteRule",
              "events:RemoveTargets"
            ]
            Resource = "*"
          },
          {
            Effect = "Allow"
            Action = [
              "scheduler:CreateSchedule",
              "scheduler:UpdateSchedule",
              "scheduler:DeleteSchedule",
              "scheduler:GetSchedule",
              "scheduler:ListSchedules"
            ]
            Resource = "*"
          },
          {
            Effect = "Allow"
            Action = [
              "iam:PassRole"
            ]
            Resource = data.aws_ssm_parameter.eventbridge_role_arn.value
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

module "web_api_logs" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/cloudwatch-log-group?ref=main"

  name              = "/aws/ecs/${var.project_name}-web-api-${var.environment}/application"
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
  otel_config_s3_url = "s3://prod-connectly.s3.ap-northeast-2.amazonaws.com/otel-config/crawlinghub-web-api/otel-config.yaml"

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
        value = "${var.project_name}-web-api"
      },
      {
        name  = "APP_PORT"
        value = "8080"
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
        "awslogs-group"         = module.web_api_logs.log_group_name
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
  name            = "${var.project_name}-web-api-${var.environment}"
  cluster_id      = data.aws_ecs_cluster.main.arn
  container_name  = "web-api"
  container_image = "${data.aws_ecr_repository.web_api.repository_url}:${var.image_tag}"
  container_port  = 8080
  cpu             = var.web_api_cpu
  memory          = var.web_api_memory
  desired_count   = var.web_api_desired_count

  # IAM Roles
  execution_role_arn = module.ecs_task_execution_role.role_arn
  task_role_arn      = module.ecs_task_role.role_arn

  # Network Configuration
  subnet_ids         = local.private_subnets
  security_group_ids = [module.ecs_security_group.security_group_id]
  assign_public_ip   = false

  # Container Environment Variables
  container_environment = [
    { name = "SPRING_PROFILES_ACTIVE", value = var.environment },
    { name = "DB_HOST", value = local.rds_host },
    { name = "DB_PORT", value = local.rds_port },
    { name = "DB_NAME", value = local.rds_dbname },
    { name = "DB_USER", value = local.rds_username },
    { name = "REDIS_HOST", value = local.redis_host },
    { name = "REDIS_PORT", value = local.redis_port },
    { name = "EVENTBRIDGE_TARGET_ARN", value = data.aws_ssm_parameter.eventbridge_trigger_queue_arn.value },
    { name = "EVENTBRIDGE_ROLE_ARN", value = data.aws_ssm_parameter.eventbridge_role_arn.value },
    # Service Token 인증 활성화 (서버 간 내부 통신용)
    { name = "SECURITY_SERVICE_TOKEN_ENABLED", value = "true" },
    # Fileflow Client 설정 (내부 VPC Service Discovery 통신)
    { name = "FILEFLOW_BASE_URL", value = "http://fileflow-web-api-prod.connectly.local:8080" },
    { name = "FILEFLOW_CALLBACK_URL", value = "http://crawlinghub-web-api-prod.connectly.local:8080/api/v1/webhook/image-upload" }
  ]

  # Container Secrets
  container_secrets = [
    { name = "DB_PASSWORD", valueFrom = "${data.aws_secretsmanager_secret.rds.arn}:password::" },
    # Service Token Secret (서버 간 내부 통신 인증용)
    { name = "SECURITY_SERVICE_TOKEN_SECRET", valueFrom = data.aws_ssm_parameter.service_token_secret.arn }
  ]

  # Health Check
  health_check_command      = ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1"]
  health_check_start_period = 300 # Web API needs time to initialize JPA, Redis, and HTTP client connections (increased for stability)

  # Logging (use existing log group)
  log_configuration = {
    log_driver = "awslogs"
    options = {
      "awslogs-group"         = module.web_api_logs.log_group_name
      "awslogs-region"        = var.aws_region
      "awslogs-stream-prefix" = "web-api"
    }
  }

  # ADOT Sidecar (using custom S3 config URL to bypass CDN cache)
  sidecars = [local.adot_container_definition]

  # Auto Scaling
  enable_autoscaling       = true
  autoscaling_min_capacity = 2
  autoscaling_max_capacity = 6
  autoscaling_target_cpu   = 70

  # Deployment Configuration
  deployment_circuit_breaker_enable   = true
  deployment_circuit_breaker_rollback = true

  # Service Discovery Configuration
  enable_service_discovery         = true
  service_discovery_namespace_id   = data.aws_ssm_parameter.service_discovery_namespace_id.value
  service_discovery_namespace_name = "connectly.local"

  # Tagging
  environment  = var.environment
  service_name = "${var.project_name}-web-api"
  team         = "platform-team"
  owner        = "platform@ryuqqq.com"
  cost_center  = "engineering"
  project      = var.project_name
  data_class   = "confidential"
}
