# ========================================
# Common Variables
# ========================================
# Shared variables for all Terraform modules
# ========================================

variable "aws_region" {
  description = "AWS Region"
  type        = string
  default     = "ap-northeast-2"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "prod"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "crawlinghub"
}

variable "domain_name" {
  description = "Main domain name"
  type        = string
  default     = "set-of.com"
}

variable "subdomain" {
  description = "Subdomain for this service"
  type        = string
  default     = "crawler"
}

# ========================================
# ECS Configuration
# ========================================

variable "web_api_desired_count" {
  description = "Desired count for web-api ECS service"
  type        = number
  default     = 2
}

variable "web_api_cpu" {
  description = "CPU units for web-api task (1 vCPU = 1024)"
  type        = number
  default     = 512
}

variable "web_api_memory" {
  description = "Memory for web-api task (MiB)"
  type        = number
  default     = 1024
}

variable "scheduler_cpu" {
  description = "CPU units for scheduler task"
  type        = number
  default     = 256
}

variable "scheduler_memory" {
  description = "Memory for scheduler task (MiB)"
  type        = number
  default     = 512
}
