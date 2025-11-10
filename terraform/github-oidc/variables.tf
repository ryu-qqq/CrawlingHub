# ============================================================================
# Variables
# ============================================================================

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "environment" {
  description = "Environment (dev/staging/prod)"
  type        = string
  default     = "prod"
}

variable "service_name" {
  description = "Service name"
  type        = string
  default     = "crawlinghub"
}

variable "github_org" {
  description = "GitHub organization or user name"
  type        = string
  default     = "ryu-qqq"
}

variable "github_repo" {
  description = "GitHub repository name"
  type        = string
  default     = "CrawlingHub"
}
