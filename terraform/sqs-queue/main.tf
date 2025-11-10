# ============================================================================
# CRAWLINGHUB - SQS Queue
# ============================================================================
# Purpose: EventBridge Schedule Trigger Queue
# - FIFO Queue for ordered message processing
# - EventBridge → SQS → SQS Listener Bootstrap
# ============================================================================

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "environment" {
  description = "Environment (dev/staging/prod)"
  type        = string
  default     = "prod"
}

variable "service_name" {
  description = "Service name"
  type        = string
  default     = "crawlinghub"
}

# ============================================================================
# Locals
# ============================================================================

locals {
  required_tags = {
    Environment = var.environment
    Service     = var.service_name
    Owner       = "windsurf@ryuqqq.com"
    CostCenter  = "engineering"
    Lifecycle   = "permanent"
    DataClass   = "internal"
    ManagedBy   = "terraform"
  }

  queue_base_name = "${var.service_name}-schedule-trigger-${var.environment}"
  queue_name      = "${local.queue_base_name}.fifo"
  dlq_name        = "${local.queue_base_name}-dlq.fifo"
}

# ============================================================================
# SQS FIFO Queue
# ============================================================================

resource "aws_sqs_queue" "schedule_trigger" {
  name                        = local.queue_name
  fifo_queue                  = true
  content_based_deduplication = true

  # Message Retention
  message_retention_seconds = 1209600  # 14 days

  # Visibility Timeout (크롤링 작업 시간 고려)
  visibility_timeout_seconds = 300  # 5 minutes

  # Receive Wait Time (Long Polling)
  receive_wait_time_seconds = 20  # 20 seconds

  # Dead Letter Queue 설정
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.schedule_trigger_dlq.arn
    maxReceiveCount     = 3
  })

  tags = merge(
    local.required_tags,
    {
      Name      = local.queue_name
      Component = "sqs-fifo"
      Purpose   = "schedule-trigger"
    }
  )
}

# ============================================================================
# Dead Letter Queue
# ============================================================================

resource "aws_sqs_queue" "schedule_trigger_dlq" {
  name                      = local.dlq_name
  fifo_queue                = true
  message_retention_seconds = 1209600  # 14 days

  tags = merge(
    local.required_tags,
    {
      Name      = local.dlq_name
      Component = "sqs-fifo-dlq"
      Purpose   = "schedule-trigger-dlq"
    }
  )
}

# ============================================================================
# Queue Policy (EventBridge 접근 허용)
# ============================================================================

data "aws_caller_identity" "current" {}

resource "aws_sqs_queue_policy" "schedule_trigger" {
  queue_url = aws_sqs_queue.schedule_trigger.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowEventBridgeScheduler"
        Effect = "Allow"
        Principal = {
          Service = "scheduler.amazonaws.com"
        }
        Action   = "sqs:SendMessage"
        Resource = aws_sqs_queue.schedule_trigger.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = "arn:aws:scheduler:${var.aws_region}:${data.aws_caller_identity.current.account_id}:schedule/*"
          }
        }
      },
      {
        Sid    = "AllowECSTaskRole"
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/crawlinghub-prod-ecs-task-role"
        }
        Action = [
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes",
          "sqs:ChangeMessageVisibility"
        ]
        Resource = aws_sqs_queue.schedule_trigger.arn
      }
    ]
  })
}

# ============================================================================
# CloudWatch Alarms
# ============================================================================

resource "aws_cloudwatch_metric_alarm" "queue_depth" {
  alarm_name          = "${local.queue_name}-depth"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period              = 300
  statistic           = "Average"
  threshold           = 100
  alarm_description   = "SQS queue depth is too high"
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = aws_sqs_queue.schedule_trigger.name
  }

  tags = merge(
    local.required_tags,
    {
      Name      = "${local.queue_name}-depth-alarm"
      Component = "cloudwatch-alarm"
    }
  )
}

resource "aws_cloudwatch_metric_alarm" "dlq_messages" {
  alarm_name          = "${local.queue_name}-dlq-messages"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period              = 60
  statistic           = "Sum"
  threshold           = 0
  alarm_description   = "Messages in DLQ detected"
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = aws_sqs_queue.schedule_trigger_dlq.name
  }

  tags = merge(
    local.required_tags,
    {
      Name      = "${local.queue_name}-dlq-alarm"
      Component = "cloudwatch-alarm"
    }
  )
}

# ============================================================================
# SSM Parameters
# ============================================================================

resource "aws_ssm_parameter" "queue_url" {
  name        = "/crawlinghub/prod/sqs/schedule-trigger-queue-url"
  description = "SQS schedule trigger queue URL for crawlinghub"
  type        = "String"
  value       = aws_sqs_queue.schedule_trigger.url

  tags = merge(
    local.required_tags,
    {
      Name      = "ssm-${local.queue_name}-url"
      Component = "parameter-store"
    }
  )
}

resource "aws_ssm_parameter" "queue_arn" {
  name        = "/crawlinghub/prod/sqs/schedule-trigger-queue-arn"
  description = "SQS schedule trigger queue ARN for crawlinghub"
  type        = "String"
  value       = aws_sqs_queue.schedule_trigger.arn

  tags = merge(
    local.required_tags,
    {
      Name      = "ssm-${local.queue_name}-arn"
      Component = "parameter-store"
    }
  )
}
