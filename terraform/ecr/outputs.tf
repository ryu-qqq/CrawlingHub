# ============================================================================
# Outputs
# ============================================================================

output "repository_url" {
  description = "ECR 리포지토리 URL"
  value       = aws_ecr_repository.crawlinghub.repository_url
}

output "repository_arn" {
  description = "ECR 리포지토리 ARN"
  value       = aws_ecr_repository.crawlinghub.arn
}

output "repository_name" {
  description = "ECR 리포지토리 이름"
  value       = aws_ecr_repository.crawlinghub.name
}

output "registry_id" {
  description = "ECR 레지스트리 ID"
  value       = aws_ecr_repository.crawlinghub.registry_id
}
