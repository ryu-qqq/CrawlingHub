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
