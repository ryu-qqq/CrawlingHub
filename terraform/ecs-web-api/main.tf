# ========================================
# ECS Service: web-api
# ========================================
# REST API server with ALB and Auto Scaling
# Domain: crawler.set-of.com
# ========================================

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
# Security Groups
# ========================================

resource "aws_security_group" "alb" {
  name        = "${var.project_name}-alb-sg-${var.environment}"
  description = "Security group for ALB"
  vpc_id      = local.vpc_id

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS from anywhere"
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP for redirect"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-alb-sg-${var.environment}"
  }
}

resource "aws_security_group" "ecs_web_api" {
  name        = "${var.project_name}-web-api-sg-${var.environment}"
  description = "Security group for web-api ECS tasks"
  vpc_id      = local.vpc_id

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
    description     = "From ALB only"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-web-api-sg-${var.environment}"
  }
}

# ========================================
# Application Load Balancer
# ========================================

module "alb" {
  source = "git::https://github.com/ryu-qqq/infrastructure.git//terraform/modules/alb?ref=main"

  name               = "${var.project_name}-alb-${var.environment}"
  vpc_id             = local.vpc_id
  subnets            = local.public_subnets
  security_group_ids = [aws_security_group.alb.id]

  certificate_arn = local.certificate_arn

  # Target Group for web-api
  target_group_name     = "${var.project_name}-web-api-tg-${var.environment}"
  target_group_port     = 8080
  target_group_protocol = "HTTP"
  target_type           = "ip"

  health_check = {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    matcher             = "200"
  }

  common_tags = {
    Environment = var.environment
    Service     = "${var.project_name}-web-api-${var.environment}"
  }
}

# ========================================
# Route53 DNS Record
# ========================================

resource "aws_route53_record" "web_api" {
  zone_id = local.route53_zone_id
  name    = local.fqdn
  type    = "A"

  alias {
    name                   = module.alb.dns_name
    zone_id                = module.alb.zone_id
    evaluate_target_health = true
  }
}

# ========================================
# IAM Role for ECS Task Execution
# ========================================

resource "aws_iam_role" "ecs_task_execution" {
  name = "${var.project_name}-web-api-execution-role-${var.environment}"

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
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "secrets_access" {
  name = "${var.project_name}-secrets-access-${var.environment}"
  role = aws_iam_role.ecs_task_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = [
          data.aws_secretsmanager_secret.rds.arn
        ]
      },
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

# ========================================
# IAM Role for ECS Task
# ========================================

resource "aws_iam_role" "ecs_task" {
  name = "${var.project_name}-web-api-task-role-${var.environment}"

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
}

# Add EventBridge permissions for web-api
resource "aws_iam_role_policy" "eventbridge_access" {
  name = "${var.project_name}-eventbridge-access-${var.environment}"
  role = aws_iam_role.ecs_task.id

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

# ========================================
# CloudWatch Log Group
# ========================================

resource "aws_cloudwatch_log_group" "web_api" {
  name              = "/ecs/${var.project_name}-web-api-${var.environment}"
  retention_in_days = 30

  tags = {
    Environment = var.environment
    Service     = "${var.project_name}-web-api-${var.environment}"
  }
}

# ========================================
# ECS Task Definition
# ========================================

resource "aws_ecs_task_definition" "web_api" {
  family                   = "${var.project_name}-web-api-${var.environment}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.web_api_cpu
  memory                   = var.web_api_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name  = "web-api"
      image = "${data.aws_ecr_repository.web_api.repository_url}:latest"

      portMappings = [
        {
          containerPort = 8080
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
          "awslogs-group"         = aws_cloudwatch_log_group.web_api.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "web-api"
        }
      }

      healthCheck = {
        command     = ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
    }
  ])

  tags = {
    Environment = var.environment
    Service     = "${var.project_name}-web-api-${var.environment}"
  }
}

# ========================================
# ECS Service
# ========================================

module "ecs_service_web_api" {
  source = "git::https://github.com/ryu-qqq/infrastructure.git//terraform/modules/ecs-service?ref=main"

  cluster_id         = data.aws_ecs_cluster.main.arn
  service_name       = "${var.project_name}-web-api-${var.environment}"
  task_definition    = aws_ecs_task_definition.web_api.arn
  desired_count      = var.web_api_desired_count
  subnets            = local.private_subnets
  security_group_ids = [aws_security_group.ecs_web_api.id]

  load_balancer = {
    target_group_arn = module.alb.target_group_arn
    container_name   = "web-api"
    container_port   = 8080
  }

  # Auto Scaling
  enable_autoscaling  = true
  min_capacity        = 2
  max_capacity        = 10
  cpu_target_value    = 70
  memory_target_value = 80

  common_tags = {
    Environment = var.environment
    Service     = "${var.project_name}-web-api-${var.environment}"
  }
}
