# ============================================================================
# Outputs
# ============================================================================

output "oidc_provider_arn" {
  description = "GitHub OIDC Identity Provider ARN"
  value       = aws_iam_openid_connect_provider.github_actions.arn
}

output "github_actions_role_arn" {
  description = "GitHub Actions IAM Role ARN (use this in workflow secrets.AWS_ROLE_ARN)"
  value       = data.aws_iam_role.github_actions.arn
}

output "github_actions_role_name" {
  description = "GitHub Actions IAM Role Name"
  value       = data.aws_iam_role.github_actions.name
}

output "github_actions_iam_read_policy_arn" {
  description = "GitHub Actions IAM Read Policy ARN"
  value       = aws_iam_policy.github_actions_iam_read.arn
}
