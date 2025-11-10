# ============================================================================
# Local Variables
# ============================================================================

locals {
  # Service Configuration
  service_name = "crawlinghub"
  environment  = "prod"

  # Common Tags (Required by governance)
  required_tags = {
    Environment = "prod"
    Service     = "crawlinghub"
    Owner       = "windsurf@ryuqqq.com"
    CostCenter  = "engineering"
    Lifecycle   = "production"
    DataClass   = "internal"
    ManagedBy   = "terraform"
    Repository  = "infrastructure"
  }

  # Resource Naming
  name_prefix = "${local.service_name}-${local.environment}"

  # Container Configuration
  container_name = "crawlinghub"
  container_port = 8080

  # CloudWatch Log Group
  log_group_name     = "/aws/ecs/${local.name_prefix}"
  log_retention_days = 30
}
