# ============================================================================
# CRAWLINGHUB - ECS Service Configuration
# ============================================================================
# Purpose: Defines ECS cluster and 3 independent services
# - Web API service (crawlinghub-prod)
# - Scheduler service (crawlinghub-scheduler-prod)
# - SQS Listener service (crawlinghub-sqs-listener-prod)
# ============================================================================

# ============================================================================
# ECS Cluster
# ============================================================================

resource "aws_ecs_cluster" "crawlinghub" {
  name = "${local.name_prefix}-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = merge(
    local.required_tags,
    {
      Name      = "${local.name_prefix}-cluster"
      Component = "ecs"
    }
  )
}

# ECS Cluster Capacity Providers
resource "aws_ecs_cluster_capacity_providers" "crawlinghub" {
  cluster_name = aws_ecs_cluster.crawlinghub.name

  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 1
    base              = 1
  }
}

# ============================================================================
# CloudWatch Log Groups
# ============================================================================

# Web API Log Group
module "crawlinghub_logs" {
  source = "../modules/cloudwatch-log-group"

  name              = local.log_group_name
  retention_in_days = local.log_retention_days
  kms_key_id        = data.aws_kms_key.cloudwatch_logs.arn

  environment = local.environment
  service     = local.service_name
  team        = "engineering"
  owner       = "windsurf@ryuqqq.com"
  cost_center = "engineering"
  project     = "crawlinghub"
}

# Scheduler Log Group
module "scheduler_logs" {
  source = "../modules/cloudwatch-log-group"

  name              = "/aws/ecs/${local.name_prefix}-scheduler"
  retention_in_days = local.log_retention_days
  kms_key_id        = data.aws_kms_key.cloudwatch_logs.arn

  environment = local.environment
  service     = "${local.service_name}-scheduler"
  team        = "engineering"
  owner       = "windsurf@ryuqqq.com"
  cost_center = "engineering"
  project     = "crawlinghub"
}

# SQS Listener Log Group
module "sqs_listener_logs" {
  source = "../modules/cloudwatch-log-group"

  name              = "/aws/ecs/${local.name_prefix}-sqs-listener"
  retention_in_days = local.log_retention_days
  kms_key_id        = data.aws_kms_key.cloudwatch_logs.arn

  environment = local.environment
  service     = "${local.service_name}-sqs-listener"
  team        = "engineering"
  owner       = "windsurf@ryuqqq.com"
  cost_center = "engineering"
  project     = "crawlinghub"
}

# ============================================================================
# Security Groups
# ============================================================================

# Web API Security Group
resource "aws_security_group" "crawlinghub" {
  name_prefix = "${local.name_prefix}-"
  description = "Security group for crawlinghub web API tasks"
  vpc_id      = local.vpc_id

  ingress {
    from_port       = local.container_port
    to_port         = local.container_port
    protocol        = "tcp"
    security_groups = [aws_security_group.crawlinghub_alb.id]
    description     = "Allow traffic from ALB"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = merge(
    local.required_tags,
    {
      Name      = "sg-${local.name_prefix}"
      Component = "security"
    }
  )

  lifecycle {
    create_before_destroy = true
  }
}

# Scheduler Security Group
resource "aws_security_group" "scheduler" {
  name_prefix = "${local.name_prefix}-scheduler-"
  description = "Security group for crawlinghub scheduler tasks"
  vpc_id      = local.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = merge(
    local.required_tags,
    {
      Name      = "sg-${local.name_prefix}-scheduler"
      Component = "security"
    }
  )

  lifecycle {
    create_before_destroy = true
  }
}

# SQS Listener Security Group
resource "aws_security_group" "sqs_listener" {
  name_prefix = "${local.name_prefix}-sqs-listener-"
  description = "Security group for crawlinghub SQS listener tasks"
  vpc_id      = local.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = merge(
    local.required_tags,
    {
      Name      = "sg-${local.name_prefix}-sqs-listener"
      Component = "security"
    }
  )

  lifecycle {
    create_before_destroy = true
  }
}

# ============================================================================
# IAM Roles and Policies
# ============================================================================

# IAM Role for ECS Task Execution
resource "aws_iam_role" "crawlinghub_execution_role" {
  name = "${local.name_prefix}-ecs-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })

  tags = local.required_tags
}

# Attach AWS managed policy for ECS task execution
resource "aws_iam_role_policy_attachment" "crawlinghub_execution_role_policy" {
  role       = aws_iam_role.crawlinghub_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Additional policy for CloudWatch Logs
resource "aws_iam_role_policy" "crawlinghub_logs_policy" {
  name = "${local.name_prefix}-logs-policy"
  role = aws_iam_role.crawlinghub_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "arn:aws:logs:*:*:*"
      }
    ]
  })
}

# Additional policy for ECR access
resource "aws_iam_role_policy" "crawlinghub_ecr_policy" {
  name = "${local.name_prefix}-ecr-policy"
  role = aws_iam_role.crawlinghub_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage"
        ]
        Resource = "*"
      }
    ]
  })
}

# Additional policy for Secrets Manager access
resource "aws_iam_role_policy" "crawlinghub_secrets_access" {
  name = "${local.name_prefix}-secrets-access"
  role = aws_iam_role.crawlinghub_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = [
          "arn:aws:secretsmanager:ap-northeast-2:646886795421:secret:prod-shared-mysql-master-password-*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt"
        ]
        Resource = data.aws_kms_key.ecs_secrets.arn
      }
    ]
  })
}

# IAM Role for ECS Task
resource "aws_iam_role" "crawlinghub_task_role" {
  name = "${local.name_prefix}-ecs-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })

  tags = local.required_tags
}

# SQS Access Policy for Task Role (SQS Listener Service)
resource "aws_iam_role_policy" "crawlinghub_sqs_access" {
  name = "${local.name_prefix}-sqs-access"
  role = aws_iam_role.crawlinghub_task_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes",
          "sqs:ChangeMessageVisibility"
        ]
        Resource = local.sqs_queue_arn
      }
    ]
  })
}

# ============================================================================
# Web API Service
# ============================================================================

module "crawlinghub_service" {
  source = "../modules/ecs-service"

  # Required variables
  name               = local.service_name
  cluster_id         = aws_ecs_cluster.crawlinghub.id
  container_name     = local.container_name
  container_port     = local.container_port
  container_image    = local.ecr_image_web_api
  cpu                = 1024
  memory             = 2048
  desired_count      = 1
  execution_role_arn = aws_iam_role.crawlinghub_execution_role.arn
  task_role_arn      = aws_iam_role.crawlinghub_task_role.arn
  subnet_ids         = local.private_subnet_ids
  common_tags        = local.required_tags

  # Security groups
  security_group_ids = [aws_security_group.crawlinghub.id]

  # CloudWatch Logs
  log_configuration = {
    log_driver = "awslogs"
    options = {
      "awslogs-group"         = module.crawlinghub_logs.log_group_name
      "awslogs-region"        = "ap-northeast-2"
      "awslogs-stream-prefix" = "ecs"
    }
  }

  # Load Balancer Configuration
  load_balancer_config = {
    target_group_arn = module.crawlinghub_alb.target_group_arns["crawlinghub"]
    container_name   = local.container_name
    container_port   = local.container_port
  }

  # Environment Variables
  container_environment = [
    {
      name  = "ENVIRONMENT"
      value = local.environment
    },
    {
      name  = "SERVICE_NAME"
      value = local.service_name
    },
    {
      name  = "SPRING_PROFILES_ACTIVE"
      value = "prod"
    },
    {
      name  = "DB_HOST"
      value = local.db_address
    },
    {
      name  = "DB_PORT"
      value = tostring(local.db_port)
    },
    {
      name  = "DB_NAME"
      value = "crawler"
    },
    {
      name  = "DB_USER"
      value = local.db_user
    },
    {
      name  = "FLYWAY_ENABLED"
      value = "true"
    },
    {
      name  = "FLYWAY_BASELINE_ON_MIGRATE"
      value = "true"
    },
    {
      name  = "REDIS_HOST"
      value = local.redis_endpoint
    },
    {
      name  = "REDIS_PORT"
      value = tostring(local.redis_port)
    }
  ]

  # Secrets (injected at runtime)
  container_secrets = [
    {
      name      = "DB_PASSWORD"
      valueFrom = "${data.aws_secretsmanager_secret_version.crawlinghub_user_password.arn}:password::"
    }
  ]

  depends_on = [
    module.crawlinghub_logs,
    aws_iam_role_policy_attachment.crawlinghub_execution_role_policy
  ]
}

# ============================================================================
# Scheduler Service
# ============================================================================

module "scheduler_service" {
  source = "../modules/ecs-service"

  name               = "${local.service_name}-scheduler"
  cluster_id         = aws_ecs_cluster.crawlinghub.id
  container_name     = "crawlinghub-scheduler"
  container_port     = 9091 # Actuator port
  container_image    = local.ecr_image_scheduler
  cpu                = 512
  memory             = 1024
  desired_count      = 1 # 스케줄러는 항상 1개만
  execution_role_arn = aws_iam_role.crawlinghub_execution_role.arn
  task_role_arn      = aws_iam_role.crawlinghub_task_role.arn
  subnet_ids         = local.private_subnet_ids
  common_tags        = local.required_tags

  security_group_ids = [aws_security_group.scheduler.id]

  log_configuration = {
    log_driver = "awslogs"
    options = {
      "awslogs-group"         = module.scheduler_logs.log_group_name
      "awslogs-region"        = "ap-northeast-2"
      "awslogs-stream-prefix" = "scheduler"
    }
  }

  # No Load Balancer for scheduler
  load_balancer_config = null

  container_environment = [
    {
      name  = "SPRING_PROFILES_ACTIVE"
      value = "prod"
    },
    {
      name  = "DB_HOST"
      value = local.db_address
    },
    {
      name  = "DB_PORT"
      value = tostring(local.db_port)
    },
    {
      name  = "DB_NAME"
      value = "crawler"
    },
    {
      name  = "DB_USER"
      value = local.db_user
    },
    {
      name  = "REDIS_HOST"
      value = local.redis_endpoint
    },
    {
      name  = "REDIS_PORT"
      value = tostring(local.redis_port)
    },
    {
      name  = "AWS_REGION"
      value = "ap-northeast-2"
    },
    {
      name  = "AWS_CLOUDWATCH_LOG_GROUP"
      value = module.scheduler_logs.log_group_name
    },
    {
      name  = "ACTUATOR_PORT"
      value = "9091"
    }
  ]

  container_secrets = [
    {
      name      = "DB_PASSWORD"
      valueFrom = "${data.aws_secretsmanager_secret_version.crawlinghub_user_password.arn}:password::"
    }
  ]

  depends_on = [
    module.scheduler_logs,
    aws_iam_role_policy_attachment.crawlinghub_execution_role_policy
  ]
}

# ============================================================================
# SQS Listener Service
# ============================================================================

module "sqs_listener_service" {
  source = "../modules/ecs-service"

  name               = "${local.service_name}-sqs-listener"
  cluster_id         = aws_ecs_cluster.crawlinghub.id
  container_name     = "crawlinghub-sqs-listener"
  container_port     = 9092 # Actuator port
  container_image    = local.ecr_image_sqs_listener
  cpu                = 512
  memory             = 1024
  desired_count      = 1 # SQS Listener는 항상 1개만
  execution_role_arn = aws_iam_role.crawlinghub_execution_role.arn
  task_role_arn      = aws_iam_role.crawlinghub_task_role.arn
  subnet_ids         = local.private_subnet_ids
  common_tags        = local.required_tags

  security_group_ids = [aws_security_group.sqs_listener.id]

  log_configuration = {
    log_driver = "awslogs"
    options = {
      "awslogs-group"         = module.sqs_listener_logs.log_group_name
      "awslogs-region"        = "ap-northeast-2"
      "awslogs-stream-prefix" = "sqs-listener"
    }
  }

  # No Load Balancer for SQS listener
  load_balancer_config = null

  container_environment = [
    {
      name  = "SPRING_PROFILES_ACTIVE"
      value = "prod"
    },
    {
      name  = "DB_HOST"
      value = local.db_address
    },
    {
      name  = "DB_PORT"
      value = tostring(local.db_port)
    },
    {
      name  = "DB_NAME"
      value = "crawler"
    },
    {
      name  = "DB_USER"
      value = local.db_user
    },
    {
      name  = "REDIS_HOST"
      value = local.redis_endpoint
    },
    {
      name  = "REDIS_PORT"
      value = tostring(local.redis_port)
    },
    {
      name  = "AWS_REGION"
      value = "ap-northeast-2"
    },
    {
      name  = "AWS_SQS_QUEUE_URL"
      value = local.sqs_queue_url
    },
    {
      name  = "AWS_CLOUDWATCH_LOG_GROUP"
      value = module.sqs_listener_logs.log_group_name
    },
    {
      name  = "ACTUATOR_PORT"
      value = "9092"
    }
  ]

  container_secrets = [
    {
      name      = "DB_PASSWORD"
      valueFrom = "${data.aws_secretsmanager_secret_version.crawlinghub_user_password.arn}:password::"
    }
  ]

  depends_on = [
    module.sqs_listener_logs,
    aws_iam_role_policy_attachment.crawlinghub_execution_role_policy
  ]
}
