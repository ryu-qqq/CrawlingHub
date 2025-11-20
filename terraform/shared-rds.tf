# ========================================
# Shared Infrastructure Reference: RDS
# ========================================
# Read-only reference to shared MySQL database
# Credentials stored in Secrets Manager
# ========================================

# RDS Endpoint
data "aws_ssm_parameter" "rds_address" {
  name = "/shared/rds/db-instance-address"
}

data "aws_ssm_parameter" "rds_port" {
  name = "/shared/rds/db-instance-port"
}

# RDS Security Group (for egress rules)
data "aws_ssm_parameter" "rds_security_group_id" {
  name = "/shared/rds/security-group-id"
}

# Secrets Manager reference (Master Secret ARN)
data "aws_ssm_parameter" "rds_secret_arn" {
  name = "/shared/rds/db-master-secret-arn"
}

data "aws_secretsmanager_secret" "rds" {
  arn = data.aws_ssm_parameter.rds_secret_arn.value
}

data "aws_secretsmanager_secret_version" "rds" {
  secret_id = data.aws_secretsmanager_secret.rds.id
}

# ========================================
# Locals for easy access
# ========================================
locals {
  rds_credentials       = jsondecode(data.aws_secretsmanager_secret_version.rds.secret_string)
  rds_endpoint          = "${data.aws_ssm_parameter.rds_address.value}:${data.aws_ssm_parameter.rds_port.value}"
  rds_host              = data.aws_ssm_parameter.rds_address.value
  rds_port              = data.aws_ssm_parameter.rds_port.value
  rds_username          = local.rds_credentials.username
  rds_password          = local.rds_credentials.password
  rds_dbname            = "crawlinghub"
  rds_security_group_id = data.aws_ssm_parameter.rds_security_group_id.value
}
