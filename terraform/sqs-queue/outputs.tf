# ============================================================================
# Outputs
# ============================================================================

output "queue_name" {
  description = "SQS queue name"
  value       = aws_sqs_queue.schedule_trigger.name
}

output "queue_url" {
  description = "SQS queue URL"
  value       = aws_sqs_queue.schedule_trigger.url
}

output "queue_arn" {
  description = "SQS queue ARN"
  value       = aws_sqs_queue.schedule_trigger.arn
}

output "dlq_name" {
  description = "SQS DLQ name"
  value       = aws_sqs_queue.schedule_trigger_dlq.name
}

output "dlq_url" {
  description = "SQS DLQ URL"
  value       = aws_sqs_queue.schedule_trigger_dlq.url
}

output "dlq_arn" {
  description = "SQS DLQ ARN"
  value       = aws_sqs_queue.schedule_trigger_dlq.arn
}
