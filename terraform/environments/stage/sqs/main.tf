# ========================================
# SQS Queues (Stage)
# ========================================
# Stage-isolated SQS queues for testing
# Separate from prod queues via SSM path: sqs-stage/
# Using Infrastructure repository modules
# ========================================

# ========================================
# Common Tags (for governance)
# ========================================
locals {
  common_tags = {
    environment  = var.environment
    service_name = "${var.project_name}-sqs-${var.environment}"
    team         = "platform-team"
    owner        = "platform@ryuqqq.com"
    cost_center  = "engineering"
    project      = var.project_name
    data_class   = "internal"
  }
}

# ========================================
# KMS Key for SQS Encryption
# ========================================
resource "aws_kms_key" "sqs" {
  description             = "KMS key for CrawlingHub SQS queue encryption (stage)"
  deletion_window_in_days = 30
  enable_key_rotation     = true

  tags = {
    Name        = "${var.project_name}-sqs-kms-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-sqs"
  }
}

resource "aws_kms_alias" "sqs" {
  name          = "alias/${var.project_name}-sqs-${var.environment}"
  target_key_id = aws_kms_key.sqs.key_id
}

# ========================================
# SQS Queue: Crawling Tasks
# ========================================
module "crawling_task_queue" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/sqs?ref=main"

  name       = "stage-monitoring-sqs-${var.project_name}-crawling-task"
  fifo_queue = false

  # KMS Encryption (required)
  kms_key_id = aws_kms_key.sqs.arn

  # Message Configuration
  visibility_timeout_seconds = 300    # 5 minutes - enough time to process crawling task
  message_retention_seconds  = 345600 # 4 days
  max_message_size           = 262144 # 256 KB
  delay_seconds              = 0
  receive_wait_time_seconds  = 20 # Long polling enabled

  # DLQ Configuration
  enable_dlq                    = true
  max_receive_count             = 3       # Move to DLQ after 3 failed attempts
  dlq_message_retention_seconds = 1209600 # 14 days for DLQ

  # CloudWatch Alarms - Stage에서는 비활성화
  enable_cloudwatch_alarms = false

  # Required Tags
  environment = local.common_tags.environment
  service     = local.common_tags.service_name
  team        = local.common_tags.team
  owner       = local.common_tags.owner
  cost_center = local.common_tags.cost_center
  project     = local.common_tags.project
  data_class  = local.common_tags.data_class
}

# ========================================
# SQS Queue: EventBridge Trigger
# ========================================
module "eventbridge_trigger_queue" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/sqs?ref=main"

  name       = "stage-monitoring-sqs-${var.project_name}-eventbridge-trigger"
  fifo_queue = false

  # KMS Encryption (required)
  kms_key_id = aws_kms_key.sqs.arn

  # Message Configuration
  visibility_timeout_seconds = 60     # 1 minute - trigger processing is fast
  message_retention_seconds  = 86400  # 1 day
  max_message_size           = 262144 # 256 KB
  delay_seconds              = 0
  receive_wait_time_seconds  = 20 # Long polling enabled

  # DLQ Configuration
  enable_dlq                    = true
  max_receive_count             = 3      # Move to DLQ after 3 failed attempts
  dlq_message_retention_seconds = 604800 # 7 days for DLQ

  # CloudWatch Alarms - Stage에서는 비활성화
  enable_cloudwatch_alarms = false

  # Required Tags
  environment = local.common_tags.environment
  service     = local.common_tags.service_name
  team        = local.common_tags.team
  owner       = local.common_tags.owner
  cost_center = local.common_tags.cost_center
  project     = local.common_tags.project
  data_class  = local.common_tags.data_class
}

# ========================================
# SQS Queue: Product Image
# ========================================
module "product_image_queue" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/sqs?ref=main"

  name       = "stage-monitoring-sqs-${var.project_name}-product-image"
  fifo_queue = false

  # KMS Encryption (required)
  kms_key_id = aws_kms_key.sqs.arn

  # Message Configuration
  visibility_timeout_seconds = 120    # 2 minutes - image processing time
  message_retention_seconds  = 345600 # 4 days
  max_message_size           = 262144 # 256 KB
  delay_seconds              = 0
  receive_wait_time_seconds  = 20 # Long polling enabled

  # DLQ Configuration
  enable_dlq                    = true
  max_receive_count             = 3       # Move to DLQ after 3 failed attempts
  dlq_message_retention_seconds = 1209600 # 14 days for DLQ

  # CloudWatch Alarms - Stage에서는 비활성화
  enable_cloudwatch_alarms = false

  # Required Tags
  environment = local.common_tags.environment
  service     = local.common_tags.service_name
  team        = local.common_tags.team
  owner       = local.common_tags.owner
  cost_center = local.common_tags.cost_center
  project     = local.common_tags.project
  data_class  = local.common_tags.data_class
}

# ========================================
# SQS Queue: Product Sync
# ========================================
module "product_sync_queue" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/sqs?ref=main"

  name       = "stage-monitoring-sqs-${var.project_name}-product-sync"
  fifo_queue = false

  # KMS Encryption (required)
  kms_key_id = aws_kms_key.sqs.arn

  # Message Configuration
  visibility_timeout_seconds = 180    # 3 minutes - external API sync time
  message_retention_seconds  = 345600 # 4 days
  max_message_size           = 262144 # 256 KB
  delay_seconds              = 0
  receive_wait_time_seconds  = 20 # Long polling enabled

  # DLQ Configuration
  enable_dlq                    = true
  max_receive_count             = 3       # Move to DLQ after 3 failed attempts
  dlq_message_retention_seconds = 1209600 # 14 days for DLQ

  # CloudWatch Alarms - Stage에서는 비활성화
  enable_cloudwatch_alarms = false

  # Required Tags
  environment = local.common_tags.environment
  service     = local.common_tags.service_name
  team        = local.common_tags.team
  owner       = local.common_tags.owner
  cost_center = local.common_tags.cost_center
  project     = local.common_tags.project
  data_class  = local.common_tags.data_class
}

# ========================================
# IAM Policy for SQS Access (ECS Tasks)
# ========================================
data "aws_iam_policy_document" "sqs_access" {
  statement {
    sid    = "AllowSQSAccess"
    effect = "Allow"
    actions = [
      "sqs:SendMessage",
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:GetQueueUrl",
      "sqs:ChangeMessageVisibility"
    ]
    resources = [
      module.crawling_task_queue.queue_arn,
      module.crawling_task_queue.dlq_arn,
      module.eventbridge_trigger_queue.queue_arn,
      module.eventbridge_trigger_queue.dlq_arn,
      module.product_image_queue.queue_arn,
      module.product_image_queue.dlq_arn,
      module.product_sync_queue.queue_arn,
      module.product_sync_queue.dlq_arn
    ]
  }

  statement {
    sid    = "AllowKMSForSQS"
    effect = "Allow"
    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey"
    ]
    resources = [aws_kms_key.sqs.arn]
  }
}

resource "aws_iam_policy" "sqs_access" {
  name        = "${var.project_name}-sqs-access-${var.environment}"
  description = "IAM policy for CrawlingHub SQS queue access (stage)"
  policy      = data.aws_iam_policy_document.sqs_access.json

  tags = {
    Name        = "${var.project_name}-sqs-access-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-sqs"
  }
}

# ========================================
# SSM Parameters for Cross-Stack Reference
# ========================================
# Stage SSM paths use /sqs-stage/ prefix to avoid conflicts with prod
# ========================================

resource "aws_ssm_parameter" "queue_url" {
  name        = "/${var.project_name}/sqs-stage/crawling-task-queue-url"
  description = "CrawlingHub crawling task queue URL (stage)"
  type        = "String"
  value       = module.crawling_task_queue.queue_url

  tags = {
    Name        = "${var.project_name}-sqs-stage-queue-url"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "queue_arn" {
  name        = "/${var.project_name}/sqs-stage/crawling-task-queue-arn"
  description = "CrawlingHub crawling task queue ARN (stage)"
  type        = "String"
  value       = module.crawling_task_queue.queue_arn

  tags = {
    Name        = "${var.project_name}-sqs-stage-queue-arn"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "dlq_url" {
  name        = "/${var.project_name}/sqs-stage/crawling-task-dlq-url"
  description = "CrawlingHub crawling task DLQ URL (stage)"
  type        = "String"
  value       = module.crawling_task_queue.dlq_url

  tags = {
    Name        = "${var.project_name}-sqs-stage-dlq-url"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "sqs_policy_arn" {
  name        = "/${var.project_name}/sqs-stage/access-policy-arn"
  description = "CrawlingHub SQS access IAM policy ARN (stage)"
  type        = "String"
  value       = aws_iam_policy.sqs_access.arn

  tags = {
    Name        = "${var.project_name}-sqs-stage-policy-arn"
    Environment = var.environment
  }
}

# ========================================
# SSM Parameters for EventBridge Trigger Queue
# ========================================
resource "aws_ssm_parameter" "eventbridge_trigger_queue_url" {
  name        = "/${var.project_name}/sqs-stage/eventbridge-trigger-queue-url"
  description = "CrawlingHub EventBridge trigger queue URL (stage)"
  type        = "String"
  value       = module.eventbridge_trigger_queue.queue_url

  tags = {
    Name        = "${var.project_name}-sqs-stage-eventbridge-trigger-queue-url"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "eventbridge_trigger_queue_arn" {
  name        = "/${var.project_name}/sqs-stage/eventbridge-trigger-queue-arn"
  description = "CrawlingHub EventBridge trigger queue ARN (stage)"
  type        = "String"
  value       = module.eventbridge_trigger_queue.queue_arn

  tags = {
    Name        = "${var.project_name}-sqs-stage-eventbridge-trigger-queue-arn"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "eventbridge_trigger_dlq_url" {
  name        = "/${var.project_name}/sqs-stage/eventbridge-trigger-dlq-url"
  description = "CrawlingHub EventBridge trigger DLQ URL (stage)"
  type        = "String"
  value       = module.eventbridge_trigger_queue.dlq_url

  tags = {
    Name        = "${var.project_name}-sqs-stage-eventbridge-trigger-dlq-url"
    Environment = var.environment
  }
}

# ========================================
# SSM Parameters for Product Image Queue
# ========================================
resource "aws_ssm_parameter" "product_image_queue_url" {
  name        = "/${var.project_name}/sqs-stage/product-image-queue-url"
  description = "CrawlingHub product image queue URL (stage)"
  type        = "String"
  value       = module.product_image_queue.queue_url

  tags = {
    Name        = "${var.project_name}-sqs-stage-product-image-queue-url"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "product_image_queue_arn" {
  name        = "/${var.project_name}/sqs-stage/product-image-queue-arn"
  description = "CrawlingHub product image queue ARN (stage)"
  type        = "String"
  value       = module.product_image_queue.queue_arn

  tags = {
    Name        = "${var.project_name}-sqs-stage-product-image-queue-arn"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "product_image_dlq_url" {
  name        = "/${var.project_name}/sqs-stage/product-image-dlq-url"
  description = "CrawlingHub product image DLQ URL (stage)"
  type        = "String"
  value       = module.product_image_queue.dlq_url

  tags = {
    Name        = "${var.project_name}-sqs-stage-product-image-dlq-url"
    Environment = var.environment
  }
}

# ========================================
# SSM Parameters for Product Sync Queue
# ========================================
resource "aws_ssm_parameter" "product_sync_queue_url" {
  name        = "/${var.project_name}/sqs-stage/product-sync-queue-url"
  description = "CrawlingHub product sync queue URL (stage)"
  type        = "String"
  value       = module.product_sync_queue.queue_url

  tags = {
    Name        = "${var.project_name}-sqs-stage-product-sync-queue-url"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "product_sync_queue_arn" {
  name        = "/${var.project_name}/sqs-stage/product-sync-queue-arn"
  description = "CrawlingHub product sync queue ARN (stage)"
  type        = "String"
  value       = module.product_sync_queue.queue_arn

  tags = {
    Name        = "${var.project_name}-sqs-stage-product-sync-queue-arn"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "product_sync_dlq_url" {
  name        = "/${var.project_name}/sqs-stage/product-sync-dlq-url"
  description = "CrawlingHub product sync DLQ URL (stage)"
  type        = "String"
  value       = module.product_sync_queue.dlq_url

  tags = {
    Name        = "${var.project_name}-sqs-stage-product-sync-dlq-url"
    Environment = var.environment
  }
}

# ========================================
# EventBridge Scheduler → SQS Integration
# ========================================
# IAM Role for EventBridge Scheduler to send messages to SQS
# Trust policy: scheduler.amazonaws.com (NOT events.amazonaws.com)
# ========================================

resource "aws_scheduler_schedule_group" "crawlinghub" {
  name = "${var.project_name}-schedules"

  tags = {
    Name        = "${var.project_name}-schedules"
    Environment = var.environment
    Service     = "${var.project_name}-eventbridge"
  }
}

resource "aws_iam_role" "eventbridge_scheduler" {
  name = "${var.project_name}-eventbridge-sqs-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "scheduler.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-eventbridge-sqs-role-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-eventbridge"
  }
}

resource "aws_iam_role_policy" "eventbridge_scheduler_sqs" {
  name = "${var.project_name}-eventbridge-sqs-policy-${var.environment}"
  role = aws_iam_role.eventbridge_scheduler.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowSQSSendMessage"
        Effect = "Allow"
        Action = [
          "sqs:SendMessage"
        ]
        Resource = [
          module.eventbridge_trigger_queue.queue_arn
        ]
      },
      {
        Sid    = "AllowKMSForSQS"
        Effect = "Allow"
        Action = [
          "kms:Decrypt",
          "kms:GenerateDataKey"
        ]
        Resource = [aws_kms_key.sqs.arn]
      }
    ]
  })
}

# ========================================
# SSM Parameters for EventBridge Scheduler
# ========================================
resource "aws_ssm_parameter" "eventbridge_role_arn" {
  name        = "/${var.project_name}/eventbridge-stage/role-arn"
  description = "CrawlingHub EventBridge Scheduler IAM role ARN (stage)"
  type        = "String"
  value       = aws_iam_role.eventbridge_scheduler.arn

  tags = {
    Name        = "${var.project_name}-eventbridge-stage-role-arn"
    Environment = var.environment
  }
}
