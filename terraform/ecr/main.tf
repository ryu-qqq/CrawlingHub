# ========================================
# ECR Repositories for crawlinghub
# ========================================
# Container registries for:
# - web-api: REST API server
# - scheduler: Background scheduler
# ========================================

module "ecr_web_api" {
  source = "git::https://github.com/ryu-qqq/infrastructure.git//terraform/modules/ecr?ref=main"

  repository_name      = "${var.project_name}-web-api-${var.environment}"
  image_tag_mutability = "MUTABLE"
  scan_on_push         = true

  lifecycle_policy = {
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 30 tagged images"
        selection = {
          tagStatus     = "tagged"
          tagPrefixList = ["v", "prod", "latest"]
          countType     = "imageCountMoreThan"
          countNumber   = 30
        }
      },
      {
        rulePriority = 2
        description  = "Delete untagged images older than 7 days"
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 7
        }
      }
    ]
  }

  common_tags = {
    Environment = var.environment
    Service     = "${var.project_name}-web-api-${var.environment}"
  }
}

module "ecr_scheduler" {
  source = "git::https://github.com/ryu-qqq/infrastructure.git//terraform/modules/ecr?ref=main"

  repository_name      = "${var.project_name}-scheduler-${var.environment}"
  image_tag_mutability = "MUTABLE"
  scan_on_push         = true

  lifecycle_policy = {
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 30 tagged images"
        selection = {
          tagStatus     = "tagged"
          tagPrefixList = ["v", "prod", "latest"]
          countType     = "imageCountMoreThan"
          countNumber   = 30
        }
      },
      {
        rulePriority = 2
        description  = "Delete untagged images older than 7 days"
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 7
        }
      }
    ]
  }

  common_tags = {
    Environment = var.environment
    Service     = "${var.project_name}-scheduler-${var.environment}"
  }
}
