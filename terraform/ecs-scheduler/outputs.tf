# ========================================
# ECS scheduler Outputs
# ========================================

output "service_name" {
  description = "ECS scheduler service name"
  value       = module.ecs_service.service_name
}

output "task_definition_arn" {
  description = "Task definition ARN"
  value       = module.ecs_service.task_definition_arn
}

output "log_group_name" {
  description = "CloudWatch log group name"
  value       = module.scheduler_logs.log_group_name
}
