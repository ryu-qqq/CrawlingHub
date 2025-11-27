# ========================================
# EventBridge for Crawlinghub Scheduler
# ========================================
# EventBridge rules for scheduled crawling tasks
# Target: SQS Queue (eventbridge-trigger-queue)
# Flow: EventBridge Rule -> SQS -> Listener -> CrawlTask Creation
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
}

# ========================================
# Data Sources
# ========================================
data "aws_ssm_parameter" "eventbridge_trigger_queue_arn" {
  name = "/${var.project_name}/sqs/eventbridge-trigger-queue-arn"
}

data "aws_ssm_parameter" "eventbridge_trigger_queue_url" {
  name = "/${var.project_name}/sqs/eventbridge-trigger-queue-url"
}

# ========================================
# EventBridge Rule: Crawler Scheduler Trigger
# ========================================
# Periodic trigger to check and execute scheduled crawling tasks
# The actual scheduler logic is handled by the SQS Listener
# ========================================
resource "aws_cloudwatch_event_rule" "crawler_scheduler" {
  name                = "${var.project_name}-scheduler-${var.environment}"
  description         = "Crawlinghub scheduled crawling task trigger"
  schedule_expression = var.schedule_expression
  state               = var.enabled ? "ENABLED" : "DISABLED"

  tags = {
    Name        = "${var.project_name}-scheduler-${var.environment}"
    Environment = var.environment
    Service     = local.common_tags.service_name
    Team        = local.common_tags.team
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
    Project     = local.common_tags.project
    DataClass   = local.common_tags.data_class
  }
}

# ========================================
# EventBridge Target: SQS Queue
# ========================================
resource "aws_cloudwatch_event_target" "sqs_target" {
  rule      = aws_cloudwatch_event_rule.crawler_scheduler.name
  target_id = "${var.project_name}-sqs-target"
  arn       = data.aws_ssm_parameter.eventbridge_trigger_queue_arn.value

  # Transform the EventBridge event to match EventBridgeTriggerPayload format
  # Note: schedulerId and sellerId will be null for global trigger
  # The listener will query active schedulers and create tasks for each
  input_transformer {
    input_paths = {
      time = "$.time"
    }
    input_template = <<EOF
{
  "schedulerId": null,
  "sellerId": null,
  "schedulerName": "global-scheduler-trigger",
  "triggerTime": <time>
}
EOF
  }
}

# ========================================
# IAM Role for EventBridge to send messages to SQS
# ========================================
resource "aws_iam_role" "eventbridge_sqs" {
  name = "${var.project_name}-eventbridge-sqs-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "events.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-eventbridge-sqs-role-${var.environment}"
    Environment = var.environment
  }
}

resource "aws_iam_role_policy" "eventbridge_sqs" {
  name = "${var.project_name}-eventbridge-sqs-policy-${var.environment}"
  role = aws_iam_role.eventbridge_sqs.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "sqs:SendMessage"
        ]
        Resource = data.aws_ssm_parameter.eventbridge_trigger_queue_arn.value
      }
    ]
  })
}

# ========================================
# SQS Queue Policy: Allow EventBridge to send messages
# ========================================
data "aws_caller_identity" "current" {}

resource "aws_sqs_queue_policy" "eventbridge_trigger" {
  queue_url = data.aws_ssm_parameter.eventbridge_trigger_queue_url.value

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowEventBridgeToSendMessage"
        Effect = "Allow"
        Principal = {
          Service = "events.amazonaws.com"
        }
        Action   = "sqs:SendMessage"
        Resource = data.aws_ssm_parameter.eventbridge_trigger_queue_arn.value
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_cloudwatch_event_rule.crawler_scheduler.arn
          }
        }
      }
    ]
  })
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
  value       = aws_cloudwatch_event_rule.crawler_scheduler.arn
}

output "eventbridge_rule_name" {
  description = "EventBridge rule name"
  value       = aws_cloudwatch_event_rule.crawler_scheduler.name
}

output "eventbridge_sqs_role_arn" {
  description = "IAM role ARN for EventBridge to SQS"
  value       = aws_iam_role.eventbridge_sqs.arn
}
