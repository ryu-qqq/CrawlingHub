# ============================================================================
# CRAWLINGHUB - Outputs
# ============================================================================

# ECS Cluster Outputs
output "ecs_cluster_id" {
  description = "ECS cluster ID"
  value       = aws_ecs_cluster.crawlinghub.id
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = aws_ecs_cluster.crawlinghub.name
}

output "ecs_cluster_arn" {
  description = "ECS cluster ARN"
  value       = aws_ecs_cluster.crawlinghub.arn
}

# ============================================================================
# Web API Service Outputs
# ============================================================================

output "web_api_service_id" {
  description = "Web API ECS service ID"
  value       = module.crawlinghub_service.service_id
}

output "web_api_service_name" {
  description = "Web API ECS service name"
  value       = module.crawlinghub_service.service_name
}

output "web_api_security_group_id" {
  description = "Web API security group ID"
  value       = aws_security_group.crawlinghub.id
}

output "web_api_log_group_name" {
  description = "Web API CloudWatch log group name"
  value       = module.crawlinghub_logs.log_group_name
}

output "web_api_log_group_arn" {
  description = "Web API CloudWatch log group ARN"
  value       = module.crawlinghub_logs.log_group_arn
}

# ============================================================================
# Scheduler Service Outputs
# ============================================================================

output "scheduler_service_id" {
  description = "Scheduler ECS service ID"
  value       = module.scheduler_service.service_id
}

output "scheduler_service_name" {
  description = "Scheduler ECS service name"
  value       = module.scheduler_service.service_name
}

output "scheduler_security_group_id" {
  description = "Scheduler security group ID"
  value       = aws_security_group.scheduler.id
}

output "scheduler_log_group_name" {
  description = "Scheduler CloudWatch log group name"
  value       = module.scheduler_logs.log_group_name
}

# ============================================================================
# SQS Listener Service Outputs
# ============================================================================

output "sqs_listener_service_id" {
  description = "SQS Listener ECS service ID"
  value       = module.sqs_listener_service.service_id
}

output "sqs_listener_service_name" {
  description = "SQS Listener ECS service name"
  value       = module.sqs_listener_service.service_name
}

output "sqs_listener_security_group_id" {
  description = "SQS Listener security group ID"
  value       = aws_security_group.sqs_listener.id
}

output "sqs_listener_log_group_name" {
  description = "SQS Listener CloudWatch log group name"
  value       = module.sqs_listener_logs.log_group_name
}

# ============================================================================
# ALB Outputs
# ============================================================================

output "alb_dns_name" {
  description = "ALB DNS name"
  value       = module.crawlinghub_alb.alb_dns_name
}

output "alb_arn" {
  description = "ALB ARN"
  value       = module.crawlinghub_alb.alb_arn
}

output "target_group_arn" {
  description = "Target group ARN"
  value       = module.crawlinghub_alb.target_group_arns["crawlinghub"]
}

output "alb_security_group_id" {
  description = "ALB security group ID"
  value       = aws_security_group.crawlinghub_alb.id
}
