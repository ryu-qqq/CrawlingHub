# ========================================
# ECS Service: scheduler
# ========================================
# Background scheduler service
# No ALB, no auto scaling
# Desired count: 1 (fixed)
# Using Infrastructure repository modules
# ========================================

# ========================================
# Common Tags (for governance)
# ========================================
locals {
  common_tags = {
    environment  = var.environment
    service_name = "${var.project_name}-scheduler"
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
data "aws_ecr_repository" "scheduler" {
  name = "${var.project_name}-scheduler-${var.environment}"
}

# ========================================
# ECS Cluster Reference (from ecs-cluster)
# ========================================
data "aws_ecs_cluster" "main" {
  cluster_name = "${var.project_name}-cluster-${var.environment}"
}

# ========================================
# Security Group (using Infrastructure module)
# ========================================

module "ecs_security_group" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/security-group?ref=main"

  name        = "${var.project_name}-scheduler-sg-${var.environment}"
  description = "Security group for scheduler ECS tasks"
  vpc_id      = local.vpc_id

  # Custom type for scheduler (no ingress, egress only)
  type = "custom"

  # No ingress rules - scheduler doesn't expose any ports

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
module "scheduler_task_execution_role" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/iam-role-policy?ref=main"

  role_name = "${var.project_name}-scheduler-execution-role-${var.environment}"

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
module "scheduler_task_role" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/iam-role-policy?ref=main"

  role_name = "${var.project_name}-scheduler-task-role-${var.environment}"

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
            Sid    = "S3ConfigAccess"
            Effect = "Allow"
            Action = [
              "s3:GetObject"
            ]
            Resource = "arn:aws:s3:::connectly-prod/*"
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

module "scheduler_logs" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/cloudwatch-log-group?ref=main"

  name              = "/aws/ecs/${var.project_name}-scheduler-${var.environment}/application"
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
  otel_config_s3_url = "s3://connectly-prod.s3.ap-northeast-2.amazonaws.com/otel-config/crawlinghub-scheduler/otel-config.yaml"

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
        value = "${var.project_name}-scheduler"
      },
      {
        name  = "APP_PORT"
        value = "8081"
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
        "awslogs-group"         = module.scheduler_logs.log_group_name
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
  name            = "${var.project_name}-scheduler-${var.environment}"
  cluster_id      = data.aws_ecs_cluster.main.arn
  container_name  = "scheduler"
  container_image = "${data.aws_ecr_repository.scheduler.repository_url}:latest"
  container_port  = 8081
  cpu             = var.scheduler_cpu
  memory          = var.scheduler_memory
  desired_count   = 1 # Fixed at 1 (scheduler)

  # IAM Roles
  execution_role_arn = module.scheduler_task_execution_role.role_arn
  task_role_arn      = module.scheduler_task_role.role_arn

  # Network Configuration
  subnet_ids         = local.private_subnets
  security_group_ids = [module.ecs_security_group.security_group_id]
  assign_public_ip   = false

  # No Load Balancer for scheduler
  load_balancer_config = null

  # Container Environment Variables
  container_environment = [
    { name = "SPRING_PROFILES_ACTIVE", value = var.environment },
    { name = "DB_HOST", value = local.rds_host },
    { name = "DB_PORT", value = local.rds_port },
    { name = "DB_NAME", value = local.rds_dbname },
    { name = "DB_USER", value = local.rds_username },
    { name = "REDIS_HOST", value = local.redis_host },
    { name = "REDIS_PORT", value = local.redis_port }
  ]

  # Container Secrets
  container_secrets = [
    { name = "DB_PASSWORD", valueFrom = "${data.aws_secretsmanager_secret.rds.arn}:password::" }
  ]

  # Health Check
  health_check_command      = ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1"]
  health_check_start_period = 180 # Scheduler needs more time to initialize Redisson, Redis connections, and beans

  # Logging (use existing log group)
  log_configuration = {
    log_driver = "awslogs"
    options = {
      "awslogs-group"         = module.scheduler_logs.log_group_name
      "awslogs-region"        = var.aws_region
      "awslogs-stream-prefix" = "scheduler"
    }
  }

  # ADOT Sidecar (using custom S3 config URL to bypass CDN cache)
  sidecars = [local.adot_container_definition]

  # No Auto Scaling for scheduler (fixed at 1)
  enable_autoscaling = false

  # Enable ECS Exec for debugging
  enable_execute_command = true

  # Deployment Configuration
  deployment_circuit_breaker_enable   = true
  deployment_circuit_breaker_rollback = true

  # Tagging
  environment  = var.environment
  service_name = "${var.project_name}-scheduler"
  team         = "platform-team"
  owner        = "platform@ryuqqq.com"
  cost_center  = "engineering"
  project      = var.project_name
  data_class   = "confidential"
}
