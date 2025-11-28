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
              "arn:aws:sqs:${var.aws_region}:*:${var.project_name}-*"
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
# ADOT Sidecar (using Infrastructure module)
# ========================================

module "adot_sidecar" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/adot-sidecar?ref=main"

  project_name      = var.project_name
  service_name      = "crawl-worker"
  aws_region        = var.aws_region
  amp_workspace_arn = "arn:aws:aps:${var.aws_region}:*:workspace/*"
  log_group_name    = module.crawl_worker_logs.log_group_name
}

# ========================================
# ECS Task Definition
# ========================================

resource "aws_ecs_task_definition" "crawl_worker" {
  family                   = "${var.project_name}-crawl-worker-${var.environment}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.crawl_worker_cpu
  memory                   = var.crawl_worker_memory
  execution_role_arn       = module.crawl_worker_task_execution_role.role_arn
  task_role_arn            = module.crawl_worker_task_role.role_arn

  container_definitions = jsonencode([
    {
      name  = "crawl-worker"
      image = "${data.aws_ecr_repository.crawl_worker.repository_url}:latest"

      # Port for actuator health check
      portMappings = [
        {
          containerPort = 8082
          protocol      = "tcp"
        }
      ]

      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = var.environment
        },
        {
          name  = "DB_HOST"
          value = local.rds_host
        },
        {
          name  = "DB_PORT"
          value = local.rds_port
        },
        {
          name  = "DB_NAME"
          value = local.rds_dbname
        },
        {
          name  = "DB_USER"
          value = local.rds_username
        },
        {
          name  = "REDIS_HOST"
          value = local.redis_host
        },
        {
          name  = "REDIS_PORT"
          value = local.redis_port
        }
      ]

      secrets = [
        {
          name      = "DB_PASSWORD"
          valueFrom = "${data.aws_secretsmanager_secret.rds.arn}:password::"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = module.crawl_worker_logs.log_group_name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "crawl-worker"
        }
      }

      healthCheck = {
        command     = ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
    },
    # ADOT Sidecar from module
    module.adot_sidecar.container_definition
  ])

  tags = {
    Environment = var.environment
    Service     = "${var.project_name}-crawl-worker-${var.environment}"
  }
}

# ========================================
# ECS Service (No ALB)
# ========================================

resource "aws_ecs_service" "crawl_worker" {
  name            = "${var.project_name}-crawl-worker-${var.environment}"
  cluster         = data.aws_ecs_cluster.main.arn
  task_definition = aws_ecs_task_definition.crawl_worker.arn
  desired_count   = var.crawl_worker_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = local.private_subnets
    security_groups  = [module.ecs_security_group.security_group_id]
    assign_public_ip = false
  }

  # Deployment configuration
  deployment_maximum_percent         = 200
  deployment_minimum_healthy_percent = 100

  # Enable execute command for debugging
  enable_execute_command = true

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  tags = {
    Environment = var.environment
    Service     = "${var.project_name}-crawl-worker-${var.environment}"
  }

  lifecycle {
    ignore_changes = [task_definition]
  }
}

# ========================================
# Outputs
# ========================================
output "crawl_worker_service_name" {
  description = "ECS Service name"
  value       = aws_ecs_service.crawl_worker.name
}

output "crawl_worker_task_definition_arn" {
  description = "Task definition ARN"
  value       = aws_ecs_task_definition.crawl_worker.arn
}
