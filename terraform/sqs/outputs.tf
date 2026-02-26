# ========================================
# Outputs
# ========================================

output "queue_url" {
  description = "URL of the crawling task SQS queue"
  value       = module.crawling_task_queue.queue_url
}

output "queue_arn" {
  description = "ARN of the crawling task SQS queue"
  value       = module.crawling_task_queue.queue_arn
}

output "queue_name" {
  description = "Name of the crawling task SQS queue"
  value       = module.crawling_task_queue.queue_name
}

output "dlq_url" {
  description = "URL of the dead letter queue"
  value       = module.crawling_task_queue.dlq_url
}

output "dlq_arn" {
  description = "ARN of the dead letter queue"
  value       = module.crawling_task_queue.dlq_arn
}

output "dlq_name" {
  description = "Name of the dead letter queue"
  value       = module.crawling_task_queue.dlq_name
}

output "kms_key_arn" {
  description = "ARN of the KMS key used for SQS encryption"
  value       = aws_kms_key.sqs.arn
}

output "sqs_access_policy_arn" {
  description = "ARN of the IAM policy for SQS access"
  value       = aws_iam_policy.sqs_access.arn
}

# ========================================
# EventBridge Trigger Queue Outputs
# ========================================
output "eventbridge_trigger_queue_url" {
  description = "URL of the EventBridge trigger SQS queue"
  value       = module.eventbridge_trigger_queue.queue_url
}

output "eventbridge_trigger_queue_arn" {
  description = "ARN of the EventBridge trigger SQS queue"
  value       = module.eventbridge_trigger_queue.queue_arn
}

output "eventbridge_trigger_queue_name" {
  description = "Name of the EventBridge trigger SQS queue"
  value       = module.eventbridge_trigger_queue.queue_name
}

output "eventbridge_trigger_dlq_url" {
  description = "URL of the EventBridge trigger dead letter queue"
  value       = module.eventbridge_trigger_queue.dlq_url
}

output "eventbridge_trigger_dlq_arn" {
  description = "ARN of the EventBridge trigger dead letter queue"
  value       = module.eventbridge_trigger_queue.dlq_arn
}

# ========================================
# Product Image Queue Outputs
# ========================================
output "product_image_queue_url" {
  description = "URL of the product image SQS queue"
  value       = module.product_image_queue.queue_url
}

output "product_image_queue_arn" {
  description = "ARN of the product image SQS queue"
  value       = module.product_image_queue.queue_arn
}

output "product_image_queue_name" {
  description = "Name of the product image SQS queue"
  value       = module.product_image_queue.queue_name
}

output "product_image_dlq_url" {
  description = "URL of the product image dead letter queue"
  value       = module.product_image_queue.dlq_url
}

output "product_image_dlq_arn" {
  description = "ARN of the product image dead letter queue"
  value       = module.product_image_queue.dlq_arn
}

# ========================================
# Product Sync Queue Outputs
# ========================================
output "product_sync_queue_url" {
  description = "URL of the product sync SQS queue"
  value       = module.product_sync_queue.queue_url
}

output "product_sync_queue_arn" {
  description = "ARN of the product sync SQS queue"
  value       = module.product_sync_queue.queue_arn
}

output "product_sync_queue_name" {
  description = "Name of the product sync SQS queue"
  value       = module.product_sync_queue.queue_name
}

output "product_sync_dlq_url" {
  description = "URL of the product sync dead letter queue"
  value       = module.product_sync_queue.dlq_url
}

output "product_sync_dlq_arn" {
  description = "ARN of the product sync dead letter queue"
  value       = module.product_sync_queue.dlq_arn
}

# ========================================
# EventBridge Scheduler Outputs
# ========================================
output "eventbridge_scheduler_role_arn" {
  description = "ARN of the EventBridge Scheduler IAM role"
  value       = aws_iam_role.eventbridge_scheduler.arn
}

output "eventbridge_schedule_group_name" {
  description = "Name of the EventBridge Schedule Group"
  value       = aws_scheduler_schedule_group.crawlinghub.name
}
