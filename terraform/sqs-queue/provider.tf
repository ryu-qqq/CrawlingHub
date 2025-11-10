# ============================================================================
# Terraform Configuration
# ============================================================================

terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.100.0"
    }
  }

  backend "s3" {
    bucket         = "ryuqqq-terraform-state-prod"
    key            = "crawlinghub/sqs-queue/terraform.tfstate"
    region         = "ap-northeast-2"
    encrypt        = true
    dynamodb_table = "terraform-state-lock"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      ManagedBy   = "Terraform"
      Environment = var.environment
      Service     = var.service_name
    }
  }
}
