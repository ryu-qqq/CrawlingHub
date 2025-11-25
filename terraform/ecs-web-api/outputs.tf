# ========================================
# ECS web-api Outputs
# ========================================

output "alb_dns_name" {
  description = "ALB DNS name"
  value       = module.alb.alb_dns_name
}

output "alb_arn" {
  description = "ALB ARN"
  value       = module.alb.alb_arn
}

output "service_name" {
  description = "ECS service name"
  value       = aws_ecs_service.web_api.name
}

output "fqdn" {
  description = "Fully qualified domain name"
  value       = local.fqdn
}

output "url" {
  description = "Application URL"
  value       = "https://${local.fqdn}"
}
