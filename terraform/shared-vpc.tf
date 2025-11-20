# ========================================
# Shared Infrastructure Reference: VPC
# ========================================
# Read-only reference to shared VPC resources
# Managed by infrastructure repository
# ========================================

# VPC ID
data "aws_ssm_parameter" "vpc_id" {
  name = "/shared/network/vpc-id"
}

# Public Subnets (for ALB)
data "aws_ssm_parameter" "public_subnet_ids" {
  name = "/shared/network/public-subnet-ids"
}

# Private Subnets (for ECS Tasks)
data "aws_ssm_parameter" "private_subnet_ids" {
  name = "/shared/network/private-subnet-ids"
}

# Data Subnets (for RDS - reference only)
data "aws_ssm_parameter" "data_subnet_ids" {
  name = "/shared/network/data-subnet-ids"
}

# ========================================
# Locals for easy access
# ========================================
locals {
  vpc_id          = data.aws_ssm_parameter.vpc_id.value
  public_subnets  = split(",", data.aws_ssm_parameter.public_subnet_ids.value)
  private_subnets = split(",", data.aws_ssm_parameter.private_subnet_ids.value)
  data_subnets    = split(",", data.aws_ssm_parameter.data_subnet_ids.value)
}
