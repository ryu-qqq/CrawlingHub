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

  # TODO: Add KMS key ARN when KMS module is available
  # kms_key_arn = data.terraform_remote_state.kms.outputs.ecr_key_arn

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

  # TODO: Add KMS key ARN when KMS module is available
  # kms_key_arn = data.terraform_remote_state.kms.outputs.ecr_key_arn

  environment  = var.environment
  service_name = "${var.project_name}-scheduler"
  team         = "platform-team"
  owner        = "platform@ryuqqq.com"
  cost_center  = "engineering"
}
