# ============================================================================
# Shared Infrastructure References
# ============================================================================
# This file references shared infrastructure managed in terraform/network/,
# terraform/kms/, etc. DO NOT modify shared infrastructure from here.
# Updated for CrawlingHub (2025-11-10)
# ============================================================================

# Network Resources
data "aws_ssm_parameter" "vpc_id" {
  name = "/shared/network/vpc-id"
}

data "aws_ssm_parameter" "private_subnet_ids" {
  name = "/shared/network/private-subnet-ids"
}

data "aws_ssm_parameter" "public_subnet_ids" {
  name = "/shared/network/public-subnet-ids"
}

# KMS Keys
data "aws_kms_key" "ecs_secrets" {
  key_id = "alias/ecs-secrets"
}

data "aws_kms_key" "cloudwatch_logs" {
  key_id = "alias/cloudwatch-logs"
}

# Shared RDS Database
data "aws_ssm_parameter" "db_instance_address" {
  name = "/shared/rds/db-instance-address"
}

data "aws_ssm_parameter" "db_instance_port" {
  name = "/shared/rds/db-instance-port"
}

# CrawlingHub 사용자 비밀번호 (애플리케이션 전용 계정)
# DB_ACCESS_GUIDE.md: crawler 데이터베이스 사용
data "aws_ssm_parameter" "crawlinghub_user_password_secret_name" {
  name = "/crawlinghub/prod/db-user-password-secret-name"
}

data "aws_secretsmanager_secret_version" "crawlinghub_user_password" {
  secret_id = data.aws_ssm_parameter.crawlinghub_user_password_secret_name.value
}

# ECR Repository
data "aws_ecr_repository" "crawlinghub" {
  name = "crawlinghub-prod"
}

# Redis (ElastiCache)
data "aws_ssm_parameter" "redis_endpoint" {
  name = "/crawlinghub/prod/redis/endpoint"
}

data "aws_ssm_parameter" "redis_port" {
  name = "/crawlinghub/prod/redis/port"
}

# SQS Queue
data "aws_ssm_parameter" "sqs_queue_url" {
  name = "/crawlinghub/prod/sqs/schedule-trigger-queue-url"
}

data "aws_ssm_parameter" "sqs_queue_arn" {
  name = "/crawlinghub/prod/sqs/schedule-trigger-queue-arn"
}


# ============================================================================
# Locals
# ============================================================================

locals {
  # Network
  vpc_id             = data.aws_ssm_parameter.vpc_id.value
  private_subnet_ids = split(",", data.aws_ssm_parameter.private_subnet_ids.value)
  public_subnet_ids  = split(",", data.aws_ssm_parameter.public_subnet_ids.value)

  # CrawlingHub Database (crawler 데이터베이스 사용)
  # DB_ACCESS_GUIDE.md: crawler 데이터베이스, crawler_user
  db_address  = data.aws_ssm_parameter.db_instance_address.value
  db_port     = data.aws_ssm_parameter.db_instance_port.value
  db_user     = jsondecode(data.aws_secretsmanager_secret_version.crawlinghub_user_password.secret_string)["username"]
  db_password = jsondecode(data.aws_secretsmanager_secret_version.crawlinghub_user_password.secret_string)["password"]

  # Redis Cache
  redis_endpoint = data.aws_ssm_parameter.redis_endpoint.value
  redis_port     = data.aws_ssm_parameter.redis_port.value

  # SQS Queue
  sqs_queue_url = data.aws_ssm_parameter.sqs_queue_url.value
  sqs_queue_arn = data.aws_ssm_parameter.sqs_queue_arn.value

  # ECR Image (Bootstrap별 태그)
  ecr_repository_url = data.aws_ecr_repository.crawlinghub.repository_url
  ecr_image_web_api  = "${local.ecr_repository_url}:web-api-latest"
  ecr_image_scheduler = "${local.ecr_repository_url}:scheduler-latest"
  ecr_image_sqs_listener = "${local.ecr_repository_url}:sqs-listener-latest"
}
