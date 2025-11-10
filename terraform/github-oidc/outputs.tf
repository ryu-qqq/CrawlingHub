# ============================================================================
# Outputs
# ============================================================================

output "oidc_provider_arn" {
  description = "GitHub OIDC Identity Provider ARN"
  value       = aws_iam_openid_connect_provider.github_actions.arn
}

output "github_actions_role_arn" {
  description = "GitHub Actions IAM Role ARN (use this in workflow secrets.AWS_ROLE_ARN)"
  value       = aws_iam_role.github_actions.arn
}

output "github_actions_role_name" {
  description = "GitHub Actions IAM Role Name"
  value       = aws_iam_role.github_actions.name
}

output "ssm_parameter_name" {
  description = "SSM Parameter name storing the Role ARN"
  value       = aws_ssm_parameter.github_actions_role_arn.name
}
