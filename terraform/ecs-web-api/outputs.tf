# ========================================
# ECS web-api Outputs
# ========================================

output "alb_dns_name" {
  description = "ALB DNS name"
  value       = module.alb.dns_name
}

output "alb_arn" {
  description = "ALB ARN"
  value       = module.alb.arn
}

output "service_name" {
  description = "ECS service name"
  value       = module.ecs_service_web_api.service_name
}

output "fqdn" {
  description = "Fully qualified domain name"
  value       = local.fqdn
}

output "url" {
  description = "Application URL"
  value       = "https://${local.fqdn}"
}
