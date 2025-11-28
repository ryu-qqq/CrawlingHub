# ========================================
# ECS Service: web-api
# ========================================
# REST API server with ALB and Auto Scaling
# Domain: crawler.set-of.com
# Using Infrastructure repository modules
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
# Security Groups (using Infrastructure module)
# ========================================

# ALB Security Group
module "alb_security_group" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/security-group?ref=main"

  name        = "${var.project_name}-alb-sg-${var.environment}"
  description = "Security group for ALB"
  vpc_id      = local.vpc_id

  type = "alb"

  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ECS Security Group
module "ecs_security_group" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/security-group?ref=main"

  name        = "${var.project_name}-web-api-sg-${var.environment}"
  description = "Security group for web-api ECS tasks"
  vpc_id      = local.vpc_id

  type                       = "ecs"
  enable_ecs_alb_ingress     = true
  ecs_ingress_from_alb_sg_id = module.alb_security_group.security_group_id
  ecs_container_port         = 8080

  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ========================================
# Application Load Balancer (using Infrastructure module)
# ========================================

module "alb" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/alb?ref=main"

  name               = "${var.project_name}-alb-${var.environment}"
  vpc_id             = local.vpc_id
  subnet_ids         = local.public_subnets
  security_group_ids = [module.alb_security_group.security_group_id]

  enable_deletion_protection = false

  # Target Groups
  target_groups = {
    web-api = {
      port        = 8080
      protocol    = "HTTP"
      target_type = "ip"
      health_check = {
        enabled             = true
        healthy_threshold   = 2
        unhealthy_threshold = 3
        timeout             = 5
        interval            = 30
        path                = "/actuator/health"
        matcher             = "200"
      }
    }
  }

  # HTTPS Listener
  https_listeners = {
    https = {
      port            = 443
      protocol        = "HTTPS"
      certificate_arn = local.certificate_arn
      ssl_policy      = "ELBSecurityPolicy-TLS13-1-2-2021-06"
      default_action = {
        type             = "forward"
        target_group_key = "web-api"
      }
    }
  }

  # HTTP to HTTPS Redirect
  http_listeners = {
    http-redirect = {
      port     = 80
      protocol = "HTTP"
      default_action = {
        type = "redirect"
        redirect = {
          port        = "443"
          protocol    = "HTTPS"
          status_code = "HTTP_301"
        }
      }
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
# Route53 DNS Record
# ========================================

resource "aws_route53_record" "web_api" {
  zone_id = local.route53_zone_id
  name    = local.fqdn
  type    = "A"

  alias {
    name                   = module.alb.alb_dns_name
    zone_id                = module.alb.alb_zone_id
    evaluate_target_health = true
  }
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
  otel_config_s3_url = "s3://connectly-prod.s3.ap-northeast-2.amazonaws.com/otel-config/crawlinghub-web-api/otel-config.yaml"

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
  name             = "${var.project_name}-web-api-${var.environment}"
  cluster_id       = data.aws_ecs_cluster.main.arn
  container_name   = "web-api"
  container_image  = "${data.aws_ecr_repository.web_api.repository_url}:latest"
  container_port   = 8080
  cpu              = var.web_api_cpu
  memory           = var.web_api_memory
  desired_count    = var.web_api_desired_count

  # IAM Roles
  execution_role_arn = module.ecs_task_execution_role.role_arn
  task_role_arn      = module.ecs_task_role.role_arn

  # Network Configuration
  subnet_ids         = local.private_subnets
  security_group_ids = [module.ecs_security_group.security_group_id]
  assign_public_ip   = false

  # Load Balancer Configuration
  load_balancer_config = {
    target_group_arn = module.alb.target_group_arns["web-api"]
    container_name   = "web-api"
    container_port   = 8080
  }
  health_check_grace_period_seconds = 60

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
  health_check_command      = ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1"]
  health_check_start_period = 120  # Web API needs time to initialize JPA, Redis, and HTTP client connections

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

  # Tagging
  environment  = var.environment
  service_name = "${var.project_name}-web-api"
  team         = "platform-team"
  owner        = "platform@ryuqqq.com"
  cost_center  = "engineering"
  project      = var.project_name
  data_class   = "confidential"
}
