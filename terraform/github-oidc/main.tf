# ============================================================================
# CRAWLINGHUB - GitHub Actions OIDC
# ============================================================================
# Purpose: GitHub Actions가 AWS 리소스에 접근할 수 있도록 OIDC 인증 설정
# - OIDC Identity Provider 생성
# - IAM Role 생성 (GitHub Actions용)
# - Trust Policy 설정 (ryu-qqq/CrawlingHub repository 허용)
# ============================================================================

data "aws_caller_identity" "current" {}

# ============================================================================
# Locals
# ============================================================================

locals {
  required_tags = {
    Environment = var.environment
    Service     = var.service_name
    Owner       = "windsurf@ryuqqq.com"
    CostCenter  = "engineering"
    Lifecycle   = "permanent"
    ManagedBy   = "terraform"
  }

  github_oidc_url     = "token.actions.githubusercontent.com"
  github_oidc_arn     = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:oidc-provider/${local.github_oidc_url}"
  github_repo_subject = "repo:${var.github_org}/${var.github_repo}:*"
}

# ============================================================================
# GitHub OIDC Identity Provider
# ============================================================================

resource "aws_iam_openid_connect_provider" "github_actions" {
  url = "https://${local.github_oidc_url}"

  client_id_list = [
    "sts.amazonaws.com"
  ]

  thumbprint_list = [
    "6938fd4d98bab03faadb97b34396831e3780aea1",  # GitHub Actions OIDC thumbprint (2023)
    "1c58a3a8518e8759bf075b76b750d4f2df264fcd"   # GitHub Actions OIDC thumbprint (backup)
  ]

  tags = merge(
    local.required_tags,
    {
      Name      = "${var.service_name}-github-oidc-provider"
      Component = "iam-oidc"
    }
  )
}

# ============================================================================
# IAM Role for GitHub Actions (Already created - referenced as data source)
# ============================================================================
# Note: This IAM Role was already created via GitHub Actions workflow.
# Atlantis only needs read access, so we reference it as a data source
# to avoid IAM permission issues with the Atlantis ECS task role.
# ============================================================================

data "aws_iam_role" "github_actions" {
  name = "${var.service_name}-${var.environment}-github-actions-role"
}

# ============================================================================
# IAM Policies for GitHub Actions (Already attached - no changes needed)
# ============================================================================
# Note: The following policies are already attached to the IAM Role:
# - ECRAccess: Docker push/pull permissions
# - ECSAccess: Task definition and service update permissions
# - TerraformAccess: S3 backend, DynamoDB lock, and describe permissions
#
# These were created during the initial terraform apply via GitHub Actions.
# Atlantis does not need to manage these resources.
# ============================================================================
