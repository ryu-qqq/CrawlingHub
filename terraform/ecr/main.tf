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

  # Encryption (existing repo uses AES256)
  encryption_type = "AES256"

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

  # Encryption (existing repo uses AES256)
  encryption_type = "AES256"

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

  # KMS encryption: Use existing KMS key from the imported repository
  # This repository was created with KMS encryption, so we must match it
  # IMPORTANT: encryption_type must be "KMS" when using kms_key_arn
  encryption_type = "KMS"
  kms_key_arn     = "arn:aws:kms:ap-northeast-2:646886795421:key/71a789da-813c-4a95-a36f-a7a7259c5015"

  environment  = var.environment
  service_name = "${var.project_name}-crawl-worker"
  team         = "platform-team"
  owner        = "platform@ryuqqq.com"
  cost_center  = "engineering"
}
