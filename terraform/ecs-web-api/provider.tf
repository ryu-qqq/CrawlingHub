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
    key            = "crawlinghub/ecs-web-api/terraform.tfstate"
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

variable "web_api_cpu" {
  description = "CPU units for web-api task"
  type        = number
  default     = 512
}

variable "web_api_memory" {
  description = "Memory for web-api task"
  type        = number
  default     = 1024
}

variable "web_api_desired_count" {
  description = "Desired count for web-api service"
  type        = number
  default     = 2
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
# RDS Configuration (MySQL)
# ========================================

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
data "aws_ssm_parameter" "amp_workspace_id" {
  name = "/shared/monitoring/amp-workspace-id"
}

data "aws_ssm_parameter" "amp_remote_write_url" {
  name = "/shared/monitoring/amp-remote-write-url"
}

data "aws_ssm_parameter" "amp_workspace_arn" {
  name = "/shared/monitoring/amp-workspace-arn"
}

# ========================================
# Locals
# ========================================
locals {
  vpc_id          = data.aws_ssm_parameter.vpc_id.value
  private_subnets = split(",", data.aws_ssm_parameter.private_subnets.value)

  # RDS Configuration (MySQL)
  rds_credentials = jsondecode(data.aws_secretsmanager_secret_version.rds.secret_string)
  rds_host        = "prod-shared-mysql.cfacertspqbw.ap-northeast-2.rds.amazonaws.com"
  rds_port        = "3306"
  rds_dbname      = "crawler"
  rds_username    = local.rds_credentials.username

  # Redis Configuration
  redis_host = data.aws_elasticache_cluster.redis.cache_nodes[0].address
  redis_port = tostring(data.aws_elasticache_cluster.redis.port)

  # AMP Configuration (from SSM)
  amp_workspace_id     = data.aws_ssm_parameter.amp_workspace_id.value
  amp_remote_write_url = data.aws_ssm_parameter.amp_remote_write_url.value
  amp_workspace_arn    = data.aws_ssm_parameter.amp_workspace_arn.value
}
