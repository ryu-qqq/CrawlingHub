# ========================================
# EventBridge for Crawlinghub Scheduler
# ========================================
# EventBridge rules for scheduled crawling tasks
# Uses infrastructure module
# ========================================

# ========================================
# Common Tags (for governance)
# ========================================
locals {
  common_tags = {
    environment  = var.environment
    service_name = "${var.project_name}-eventbridge"
    team         = "platform-team"
    owner        = "platform@ryuqqq.com"
    cost_center  = "engineering"
    project      = var.project_name
    data_class   = "confidential"
  }
  ecs_cluster_arn = data.aws_ecs_cluster.this.arn
}

# ========================================
# EventBridge Module (from infrastructure repo)
# ========================================
module "crawler_scheduler" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/eventbridge?ref=main"

  name        = "${var.project_name}-scheduler-${var.environment}"
  target_type = "ecs"
  description = "Crawlinghub scheduled crawling tasks"

  # Schedule Configuration
  schedule_expression = var.schedule_expression
  enabled             = var.enabled

  # ECS Target Configuration
  ecs_cluster_arn         = local.ecs_cluster_arn
  ecs_task_definition_arn = data.aws_ecs_task_definition.scheduler.arn
  ecs_task_count          = 1
  ecs_launch_type         = "FARGATE"

  ecs_network_configuration = {
    subnets          = local.private_subnets
    security_groups  = [aws_security_group.eventbridge_task.id]
    assign_public_ip = false
  }

  # Required Tags
  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ========================================
# Data Sources
# ========================================
data "aws_ecs_cluster" "this" {
  cluster_name = "${var.project_name}-cluster-${var.environment}"
}

data "aws_ecs_task_definition" "scheduler" {
  task_definition = "${var.project_name}-scheduler-${var.environment}"
}

# ========================================
# Security Group for EventBridge triggered tasks
# ========================================
resource "aws_security_group" "eventbridge_task" {
  name        = "${var.project_name}-eventbridge-task-sg-${var.environment}"
  description = "Security group for EventBridge triggered ECS tasks"
  vpc_id      = local.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-eventbridge-task-sg-${var.environment}"
  }
}

# ========================================
# Variables
# ========================================
variable "schedule_expression" {
  description = "Schedule expression (cron or rate)"
  type        = string
  default     = "rate(1 hour)"
}

variable "enabled" {
  description = "Whether the EventBridge rule is enabled"
  type        = bool
  default     = true
}

# ========================================
# Outputs
# ========================================
output "eventbridge_rule_arn" {
  description = "EventBridge rule ARN"
  value       = module.crawler_scheduler.rule_arn
}

output "eventbridge_rule_name" {
  description = "EventBridge rule name"
  value       = module.crawler_scheduler.rule_name
}
