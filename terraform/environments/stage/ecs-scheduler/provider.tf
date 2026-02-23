# ========================================
# Terraform Provider Configuration (Stage)
# ========================================

terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "prod-connectly"
    key            = "crawlinghub/ecs-scheduler-stage/terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "prod-connectly-tf-lock"
    encrypt        = true
    kms_key_id     = "arn:aws:kms:ap-northeast-2:646886795421:key/086b1677-614f-46ba-863e-23c215fb5010"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}

# ========================================
# Common Variables
# ========================================
variable "project_name" {
  description = "Project name"
  type        = string
  default     = "crawlinghub"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "stage"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "scheduler_cpu" {
  description = "CPU units for scheduler task"
  type        = number
  default     = 256
}

variable "scheduler_memory" {
  description = "Memory for scheduler task"
  type        = number
  default     = 1024
}

variable "image_tag" {
  description = "Docker image tag to deploy. Format: scheduler-stage-{build-number}-{git-sha}"
  type        = string
  default     = "scheduler-stage-1-initial"

  validation {
    condition     = can(regex("^scheduler-stage-[0-9]+-[a-zA-Z0-9]+$", var.image_tag))
    error_message = "Image tag must follow format: scheduler-stage-{build-number}-{git-sha} (e.g., scheduler-stage-1-abc1234)"
  }
}

# ========================================
# Shared Resource References (SSM)
# ========================================
data "aws_ssm_parameter" "vpc_id" {
  name = "/shared/network/vpc-id"
}

data "aws_ssm_parameter" "private_subnets" {
  name = "/shared/network/private-subnets"
}

# ========================================
# RDS Configuration (MySQL - Shared Staging via RDS Proxy)
# ========================================

# RDS Proxy endpoint from SSM Parameter Store
data "aws_ssm_parameter" "rds_proxy_endpoint" {
  name = "/shared/rds/staging-proxy-endpoint"
}

data "aws_secretsmanager_secret" "rds" {
  name = "setof-commerce/rds/staging-credentials"
}

data "aws_secretsmanager_secret_version" "rds" {
  secret_id = data.aws_secretsmanager_secret.rds.id
}

# ========================================
# Monitoring Configuration (AMP)
# ========================================
data "aws_ssm_parameter" "amp_workspace_arn" {
  name = "/shared/monitoring/amp-workspace-arn"
}

data "aws_ssm_parameter" "amp_remote_write_url" {
  name = "/shared/monitoring/amp-remote-write-url"
}

# ========================================
# SQS Queue Configuration (from sqs stack)
# ========================================
# Stage uses dedicated SQS queues (separate from prod)
# Deploy order: terraform/environments/stage/sqs → terraform/environments/stage/ecs-scheduler
data "aws_ssm_parameter" "sqs" {
  for_each = {
    crawl_task_queue_url    = "/${var.project_name}/sqs-stage/crawling-task-queue-url"
    product_image_queue_url = "/${var.project_name}/sqs-stage/product-image-queue-url"
    product_sync_queue_url  = "/${var.project_name}/sqs-stage/product-sync-queue-url"
    access_policy_arn       = "/${var.project_name}/sqs-stage/access-policy-arn"
  }
  name = each.value
}

# ========================================
# Locals
# ========================================
locals {
  vpc_id          = data.aws_ssm_parameter.vpc_id.value
  private_subnets = split(",", data.aws_ssm_parameter.private_subnets.value)

  # RDS Configuration (MySQL - Shared Staging)
  # Using RDS Proxy for connection pooling and failover resilience
  rds_credentials = jsondecode(data.aws_secretsmanager_secret_version.rds.secret_string)
  rds_host        = data.aws_ssm_parameter.rds_proxy_endpoint.value
  rds_port        = "3306"
  rds_dbname      = "crawler"
  rds_username    = local.rds_credentials.username

  # Redis Configuration (Shared Stage Redis)
  redis_host = "stage-shared-redis.j9czrc.0001.apn2.cache.amazonaws.com"
  redis_port = 6379

  # AMP Configuration
  amp_workspace_arn    = data.aws_ssm_parameter.amp_workspace_arn.value
  amp_remote_write_url = data.aws_ssm_parameter.amp_remote_write_url.value

  # SQS Configuration (from SSM via for_each)
  sqs_crawl_task_queue_url    = data.aws_ssm_parameter.sqs["crawl_task_queue_url"].value
  sqs_product_image_queue_url = data.aws_ssm_parameter.sqs["product_image_queue_url"].value
  sqs_product_sync_queue_url  = data.aws_ssm_parameter.sqs["product_sync_queue_url"].value
  sqs_access_policy_arn       = data.aws_ssm_parameter.sqs["access_policy_arn"].value
}
