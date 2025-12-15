# ========================================
# ECS web-api Outputs
# ========================================

output "service_name" {
  description = "ECS service name"
  value       = module.ecs_service.service_name
}

output "task_definition_arn" {
  description = "ECS task definition ARN"
  value       = module.ecs_service.task_definition_arn
}

output "service_discovery_dns" {
  description = "Service Discovery DNS name for internal access"
  value       = "${module.ecs_service.service_name}.connectly.local"
}

output "internal_endpoint" {
  description = "Internal endpoint URL"
  value       = "http://${module.ecs_service.service_name}.connectly.local:8080"
}

output "log_group_name" {
  description = "CloudWatch log group name"
  value       = module.web_api_logs.log_group_name
}
