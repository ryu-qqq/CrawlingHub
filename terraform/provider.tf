# ========================================
# Terraform Provider Configuration
# ========================================
# AWS Provider for crawlinghub infrastructure
# Region: ap-northeast-2 (Seoul)
# ========================================

terraform {
  required_version = ">= 1.0.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Backend configuration - S3 state storage (shared)
  backend "s3" {
    bucket         = "prod-connectly"
    key            = "crawlinghub/terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "prod-connectly-tf-lock"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "crawlinghub"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}
