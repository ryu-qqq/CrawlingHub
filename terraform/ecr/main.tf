# ========================================
# ECR Repositories for crawlinghub (using Infrastructure module)
# ========================================
# Container registries for:
# - web-api: REST API server
# - scheduler: Background scheduler
# ========================================

# ========================================
# ECR Repository: web-api
# ========================================
module "ecr_web_api" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecr?ref=main"

  name = "${var.project_name}-web-api-${var.environment}"

  # KMS encryption: null = AES256 (default), provide ARN for KMS encryption
  # kms_key_arn = data.terraform_remote_state.kms.outputs.ecr_key_arn
  kms_key_arn = null

  environment  = var.environment
  service_name = "${var.project_name}-web-api"
  team         = "platform-team"
  owner        = "platform@ryuqqq.com"
  cost_center  = "engineering"
}

# ========================================
# ECR Repository: scheduler
# ========================================
module "ecr_scheduler" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecr?ref=main"

  name = "${var.project_name}-scheduler-${var.environment}"

  # KMS encryption: null = AES256 (default), provide ARN for KMS encryption
  # kms_key_arn = data.terraform_remote_state.kms.outputs.ecr_key_arn
  kms_key_arn = null

  environment  = var.environment
  service_name = "${var.project_name}-scheduler"
  team         = "platform-team"
  owner        = "platform@ryuqqq.com"
  cost_center  = "engineering"
}

# ========================================
# ECR Repository: crawl-worker
# ========================================
module "ecr_crawl_worker" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecr?ref=main"

  name = "${var.project_name}-crawl-worker-${var.environment}"

  # KMS encryption: null = AES256 (default), provide ARN for KMS encryption
  # kms_key_arn = data.terraform_remote_state.kms.outputs.ecr_key_arn
  kms_key_arn = null

  environment  = var.environment
  service_name = "${var.project_name}-crawl-worker"
  team         = "platform-team"
  owner        = "platform@ryuqqq.com"
  cost_center  = "engineering"
}
