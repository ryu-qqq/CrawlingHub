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
# IAM Role for GitHub Actions
# ============================================================================

resource "aws_iam_role" "github_actions" {
  name        = "${var.service_name}-${var.environment}-github-actions-role"
  description = "IAM Role for GitHub Actions to access AWS resources"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = aws_iam_openid_connect_provider.github_actions.arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "${local.github_oidc_url}:aud" = "sts.amazonaws.com"
          }
          StringLike = {
            "${local.github_oidc_url}:sub" = local.github_repo_subject
          }
        }
      }
    ]
  })

  max_session_duration = 3600  # 1 hour

  tags = merge(
    local.required_tags,
    {
      Name      = "${var.service_name}-${var.environment}-github-actions-role"
      Component = "iam-role"
      Purpose   = "github-actions"
    }
  )
}

# ============================================================================
# IAM Policies for GitHub Actions
# ============================================================================

# ECR 권한 (Docker push/pull)
resource "aws_iam_role_policy" "ecr_access" {
  name = "ECRAccess"
  role = aws_iam_role.github_actions.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload",
          "ecr:DescribeRepositories",
          "ecr:ListImages"
        ]
        Resource = "*"
      }
    ]
  })
}

# ECS 권한 (Task definition 업데이트, Service 배포)
resource "aws_iam_role_policy" "ecs_access" {
  name = "ECSAccess"
  role = aws_iam_role.github_actions.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecs:DescribeServices",
          "ecs:DescribeTaskDefinition",
          "ecs:DescribeTasks",
          "ecs:ListTasks",
          "ecs:RegisterTaskDefinition",
          "ecs:UpdateService",
          "ecs:TagResource"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "iam:PassRole"
        ]
        Resource = "*"
        Condition = {
          StringEquals = {
            "iam:PassedToService" = "ecs-tasks.amazonaws.com"
          }
        }
      }
    ]
  })
}

# Terraform 권한 (S3 backend, DynamoDB lock, State 관리)
resource "aws_iam_role_policy" "terraform_access" {
  name = "TerraformAccess"
  role = aws_iam_role.github_actions.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket"
        ]
        Resource = [
          "arn:aws:s3:::prod-connectly",
          "arn:aws:s3:::prod-connectly/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:DeleteItem",
          "dynamodb:DescribeTable"
        ]
        Resource = "arn:aws:dynamodb:${var.aws_region}:${data.aws_caller_identity.current.account_id}:table/prod-connectly-tf-lock"
      },
      {
        Effect = "Allow"
        Action = [
          "ec2:Describe*",
          "elasticache:Describe*",
          "sqs:GetQueueAttributes",
          "sqs:ListQueues"
        ]
        Resource = "*"
      }
    ]
  })
}

# ============================================================================
# SSM Parameter (Role ARN 저장)
# ============================================================================

resource "aws_ssm_parameter" "github_actions_role_arn" {
  name        = "/crawlinghub/prod/github-actions-role-arn"
  description = "GitHub Actions IAM Role ARN for CrawlingHub"
  type        = "String"
  value       = aws_iam_role.github_actions.arn

  tags = merge(
    local.required_tags,
    {
      Name      = "ssm-github-actions-role-arn"
      Component = "parameter-store"
    }
  )
}
