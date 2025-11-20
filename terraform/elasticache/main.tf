# ========================================
# ElastiCache (Redis) for Crawlinghub
# ========================================
# Dedicated Redis cluster using infrastructure module
# Naming: crawlinghub-redis-prod
# ========================================

# ========================================
# Security Group for Redis
# ========================================
resource "aws_security_group" "redis" {
  name        = "${var.project_name}-redis-sg-${var.environment}"
  description = "Security group for ElastiCache Redis"
  vpc_id      = local.vpc_id

  ingress {
    description = "Redis from ECS tasks"
    from_port   = 6379
    to_port     = 6379
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/8"] # VPC CIDR
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-redis-sg-${var.environment}"
  }
}

# ========================================
# ElastiCache Module (from infrastructure repo)
# ========================================
module "redis" {
  source = "git::https://github.com/ryu-qqq/infrastructure.git//terraform/modules/elasticache?ref=main"

  cluster_id = "${var.project_name}-redis-${var.environment}"

  # Engine Configuration
  engine         = "redis"
  engine_version = "7.0"
  node_type      = var.redis_node_type
  num_cache_nodes = 1

  # Network Configuration
  subnet_ids         = local.private_subnets
  security_group_ids = [aws_security_group.redis.id]

  # Parameter Group
  parameter_group_family = "redis7"
  parameters = [
    {
      name  = "maxmemory-policy"
      value = "allkeys-lru"
    }
  ]

  # Maintenance and Backup
  snapshot_retention_limit = 1
  snapshot_window          = "05:00-09:00"
  maintenance_window       = "mon:09:00-mon:10:00"

  # Disable CloudWatch Alarms for now
  enable_cloudwatch_alarms = false

  common_tags = {
    Environment = var.environment
    Service     = "${var.project_name}-redis-${var.environment}"
  }
}

# ========================================
# Variables
# ========================================
variable "redis_node_type" {
  description = "ElastiCache node type"
  type        = string
  default     = "cache.t3.micro"
}

# ========================================
# Outputs
# ========================================
output "redis_endpoint" {
  description = "Redis primary endpoint"
  value       = module.redis.cluster_endpoint
}

output "redis_port" {
  description = "Redis port"
  value       = module.redis.port
}

output "redis_security_group_id" {
  description = "Redis security group ID"
  value       = aws_security_group.redis.id
}
