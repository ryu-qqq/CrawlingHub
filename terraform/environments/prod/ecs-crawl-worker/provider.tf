# ========================================
# Terraform Provider Configuration
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
    key            = "crawlinghub/ecs-crawl-worker/terraform.tfstate"
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
  default     = "prod"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "crawl_worker_cpu" {
  description = "CPU units for crawl-worker task"
  type        = number
  default     = 512
}

variable "crawl_worker_memory" {
  description = "Memory for crawl-worker task"
  type        = number
  default     = 1024
}

variable "crawl_worker_desired_count" {
  description = "Desired count for crawl-worker service"
  type        = number
  default     = 2
}

variable "image_tag" {
  description = "Docker image tag to deploy. Auto-set by GitHub Actions build-and-deploy.yml. Format: {component}-{build-number}-{git-sha}"
  type        = string
  default     = "latest"  # Fallback only - GitHub Actions will override this

  validation {
    condition     = can(regex("^(latest|crawl-worker-[0-9]+-[a-f0-9]+)$", var.image_tag))
    error_message = "Image tag must be 'latest' or follow format: crawl-worker-{build-number}-{git-sha} (e.g., crawl-worker-46-cd5bdb2)"
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

data "aws_caller_identity" "current" {}

# ========================================
# RDS Configuration (MySQL)
# ========================================

# RDS Proxy endpoint from SSM Parameter Store
data "aws_ssm_parameter" "rds_proxy_endpoint" {
  name = "/shared/rds/proxy-endpoint"
}

# Crawlinghub-specific Secrets Manager secret
data "aws_secretsmanager_secret" "rds" {
  name = "crawlinghub/rds/credentials"
}

data "aws_secretsmanager_secret_version" "rds" {
  secret_id = data.aws_secretsmanager_secret.rds.id
}

# ========================================
# ElastiCache (Redis) Configuration
# ========================================
data "aws_elasticache_cluster" "redis" {
  cluster_id = "${var.project_name}-redis-${var.environment}"
}

# ========================================
# AMP (Amazon Managed Prometheus) Reference via SSM
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
data "aws_ssm_parameter" "sqs" {
  for_each = {
    crawl_task_queue_url    = "/${var.project_name}/sqs/crawling-task-queue-url"
    product_image_queue_url = "/${var.project_name}/sqs/product-image-queue-url"
    product_sync_queue_url  = "/${var.project_name}/sqs/product-sync-queue-url"
    access_policy_arn       = "/${var.project_name}/sqs/access-policy-arn"
  }
  name = each.value
}

# ========================================
# Locals
# ========================================
locals {
  vpc_id          = data.aws_ssm_parameter.vpc_id.value
  private_subnets = split(",", data.aws_ssm_parameter.private_subnets.value)

  # RDS Configuration (MySQL)
  # Using RDS Proxy for connection pooling and failover resilience
  rds_credentials = jsondecode(data.aws_secretsmanager_secret_version.rds.secret_string)
  rds_host        = data.aws_ssm_parameter.rds_proxy_endpoint.value
  rds_port        = "3306"
  rds_dbname      = "crawler"
  rds_username    = local.rds_credentials.username

  # Redis Configuration
  redis_host = data.aws_elasticache_cluster.redis.cache_nodes[0].address
  redis_port = tostring(data.aws_elasticache_cluster.redis.port)

  # AMP Configuration (from SSM)
  amp_workspace_arn    = data.aws_ssm_parameter.amp_workspace_arn.value
  amp_remote_write_url = data.aws_ssm_parameter.amp_remote_write_url.value

  # SQS Configuration (from SSM via for_each)
  sqs_crawl_task_queue_url    = data.aws_ssm_parameter.sqs["crawl_task_queue_url"].value
  sqs_product_image_queue_url = data.aws_ssm_parameter.sqs["product_image_queue_url"].value
  sqs_product_sync_queue_url  = data.aws_ssm_parameter.sqs["product_sync_queue_url"].value
  sqs_access_policy_arn       = data.aws_ssm_parameter.sqs["access_policy_arn"].value
}
