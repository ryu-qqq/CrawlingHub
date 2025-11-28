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
  value       = module.ecs_service.service_name
}

output "task_definition_arn" {
  description = "ECS task definition ARN"
  value       = module.ecs_service.task_definition_arn
}

output "fqdn" {
  description = "Fully qualified domain name"
  value       = local.fqdn
}

output "url" {
  description = "Application URL"
  value       = "https://${local.fqdn}"
}
