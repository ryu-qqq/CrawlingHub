#!/bin/bash

# ============================================================================
# SSM Parameter Store & Secrets Manager 초기 설정 스크립트
# ============================================================================
# Purpose: CrawlingHub 인프라를 위한 SSM Parameter 및 Secret 생성
# Reference: claudedocs/DB_ACCESS_GUIDE.md
# ============================================================================

set -e  # Exit on error

AWS_REGION="ap-northeast-2"
AWS_ACCOUNT_ID="646886795421"

echo "🚀 CrawlingHub SSM Parameter 설정 시작..."
echo "Region: ${AWS_REGION}"
echo "Account: ${AWS_ACCOUNT_ID}"
echo ""

# ============================================================================
# 1. Shared Network Parameters (공유 네트워크)
# ============================================================================

echo "📡 [1/4] Shared Network Parameters 생성 중..."

# VPC ID
aws ssm put-parameter \
  --name "/shared/network/vpc-id" \
  --type "String" \
  --value "vpc-0f162b9e588276e09" \
  --description "Shared VPC ID for all projects" \
  --region ${AWS_REGION} \
  --overwrite \
  --tags "Key=Environment,Value=prod" "Key=ManagedBy,Value=terraform" \
  || echo "⚠️  VPC ID parameter already exists or failed"

# Private Subnet IDs
aws ssm put-parameter \
  --name "/shared/network/private-subnet-ids" \
  --type "String" \
  --value "subnet-09692620519f86cf0,subnet-0d99080cbe134b6e9" \
  --description "Comma-separated private subnet IDs (ap-northeast-2a, 2b)" \
  --region ${AWS_REGION} \
  --overwrite \
  --tags "Key=Environment,Value=prod" "Key=ManagedBy,Value=terraform" \
  || echo "⚠️  Private subnet IDs parameter already exists or failed"

# Public Subnet IDs
aws ssm put-parameter \
  --name "/shared/network/public-subnet-ids" \
  --type "String" \
  --value "subnet-0bd2fc282b0fb137a,subnet-0c8c0ad85064b80bb" \
  --description "Comma-separated public subnet IDs (ap-northeast-2a, 2b)" \
  --region ${AWS_REGION} \
  --overwrite \
  --tags "Key=Environment,Value=prod" "Key=ManagedBy,Value=terraform" \
  || echo "⚠️  Public subnet IDs parameter already exists or failed"

echo "✅ Shared Network Parameters 완료"
echo ""

# ============================================================================
# 2. Shared RDS Parameters (공유 RDS)
# ============================================================================

echo "🗄️  [2/4] Shared RDS Parameters 생성 중..."

# RDS Endpoint
aws ssm put-parameter \
  --name "/shared/rds/db-instance-address" \
  --type "String" \
  --value "prod-shared-mysql.cfacertspqbw.ap-northeast-2.rds.amazonaws.com" \
  --description "Shared RDS MySQL endpoint for all projects" \
  --region ${AWS_REGION} \
  --overwrite \
  --tags "Key=Environment,Value=prod" "Key=ManagedBy,Value=terraform" \
  || echo "⚠️  RDS endpoint parameter already exists or failed"

# RDS Port
aws ssm put-parameter \
  --name "/shared/rds/db-instance-port" \
  --type "String" \
  --value "3306" \
  --description "Shared RDS MySQL port" \
  --region ${AWS_REGION} \
  --overwrite \
  --tags "Key=Environment,Value=prod" "Key=ManagedBy,Value=terraform" \
  || echo "⚠️  RDS port parameter already exists or failed"

echo "✅ Shared RDS Parameters 완료"
echo ""

# ============================================================================
# 3. Secrets Manager Secret (DB 비밀번호)
# ============================================================================

echo "🔐 [3/4] Secrets Manager Secret 생성 중..."

# Secrets Manager에 crawler_user 비밀번호 생성
SECRET_NAME="prod-crawlinghub-db-password"
SECRET_VALUE='{
  "username": "crawler_user",
  "password": "K0g)yCq%QOhJsVCj4-PYTUrVAA$8e4j-"
}'

# Secret 생성 (이미 존재하면 업데이트)
aws secretsmanager create-secret \
  --name "${SECRET_NAME}" \
  --description "CrawlingHub database (crawler) credentials for crawler_user" \
  --secret-string "${SECRET_VALUE}" \
  --region ${AWS_REGION} \
  --tags "Key=Environment,Value=prod" "Key=Service,Value=crawlinghub" "Key=ManagedBy,Value=terraform" \
  2>/dev/null || \
aws secretsmanager update-secret \
  --secret-id "${SECRET_NAME}" \
  --secret-string "${SECRET_VALUE}" \
  --region ${AWS_REGION}

echo "✅ Secret '${SECRET_NAME}' 생성/업데이트 완료"

# SSM Parameter에 Secret 이름 저장
aws ssm put-parameter \
  --name "/crawlinghub/prod/db-user-password-secret-name" \
  --type "String" \
  --value "${SECRET_NAME}" \
  --description "Secrets Manager secret name for CrawlingHub DB credentials" \
  --region ${AWS_REGION} \
  --overwrite \
  --tags "Key=Environment,Value=prod" "Key=Service,Value=crawlinghub" "Key=ManagedBy,Value=terraform" \
  || echo "⚠️  DB secret name parameter already exists or failed"

echo "✅ Secrets Manager Secret 완료"
echo ""

# ============================================================================
# 4. CrawlingHub-specific Parameters
# ============================================================================

echo "🕷️  [4/4] CrawlingHub-specific Parameters 생성 중..."

# Redis Endpoint (ElastiCache Terraform이 생성할 예정이므로 placeholder)
aws ssm put-parameter \
  --name "/crawlinghub/prod/redis/endpoint" \
  --type "String" \
  --value "crawlinghub-redis-prod.cfacertspqbw.cache.amazonaws.com" \
  --description "Redis cluster endpoint for CrawlingHub (placeholder)" \
  --region ${AWS_REGION} \
  --overwrite \
  --tags "Key=Environment,Value=prod" "Key=Service,Value=crawlinghub" "Key=ManagedBy,Value=terraform" \
  || echo "⚠️  Redis endpoint parameter already exists or failed"

# Redis Port
aws ssm put-parameter \
  --name "/crawlinghub/prod/redis/port" \
  --type "String" \
  --value "6379" \
  --description "Redis cluster port for CrawlingHub" \
  --region ${AWS_REGION} \
  --overwrite \
  --tags "Key=Environment,Value=prod" "Key=Service,Value=crawlinghub" "Key=ManagedBy,Value=terraform" \
  || echo "⚠️  Redis port parameter already exists or failed"

# SQS Queue URL (SQS Terraform이 생성할 예정이므로 placeholder)
aws ssm put-parameter \
  --name "/crawlinghub/prod/sqs/schedule-trigger-queue-url" \
  --type "String" \
  --value "https://sqs.ap-northeast-2.amazonaws.com/${AWS_ACCOUNT_ID}/crawlinghub-schedule-trigger-prod.fifo" \
  --description "SQS queue URL for CrawlingHub scheduler (placeholder)" \
  --region ${AWS_REGION} \
  --overwrite \
  --tags "Key=Environment,Value=prod" "Key=Service,Value=crawlinghub" "Key=ManagedBy,Value=terraform" \
  || echo "⚠️  SQS URL parameter already exists or failed"

# SQS Queue ARN (SQS Terraform이 생성할 예정이므로 placeholder)
aws ssm put-parameter \
  --name "/crawlinghub/prod/sqs/schedule-trigger-queue-arn" \
  --type "String" \
  --value "arn:aws:sqs:ap-northeast-2:${AWS_ACCOUNT_ID}:crawlinghub-schedule-trigger-prod.fifo" \
  --description "SQS queue ARN for CrawlingHub scheduler (placeholder)" \
  --region ${AWS_REGION} \
  --overwrite \
  --tags "Key=Environment,Value=prod" "Key=Service,Value=crawlinghub" "Key=ManagedBy,Value=terraform" \
  || echo "⚠️  SQS ARN parameter already exists or failed"

echo "✅ CrawlingHub-specific Parameters 완료"
echo ""

# ============================================================================
# 검증
# ============================================================================

echo "🔍 생성된 Parameter 검증 중..."
echo ""

echo "📋 Shared Network Parameters:"
aws ssm get-parameter --name "/shared/network/vpc-id" --region ${AWS_REGION} --query 'Parameter.Value' --output text 2>/dev/null || echo "  ❌ VPC ID not found"
aws ssm get-parameter --name "/shared/network/private-subnet-ids" --region ${AWS_REGION} --query 'Parameter.Value' --output text 2>/dev/null || echo "  ❌ Private subnets not found"
aws ssm get-parameter --name "/shared/network/public-subnet-ids" --region ${AWS_REGION} --query 'Parameter.Value' --output text 2>/dev/null || echo "  ❌ Public subnets not found"
echo ""

echo "📋 Shared RDS Parameters:"
aws ssm get-parameter --name "/shared/rds/db-instance-address" --region ${AWS_REGION} --query 'Parameter.Value' --output text 2>/dev/null || echo "  ❌ RDS address not found"
aws ssm get-parameter --name "/shared/rds/db-instance-port" --region ${AWS_REGION} --query 'Parameter.Value' --output text 2>/dev/null || echo "  ❌ RDS port not found"
echo ""

echo "📋 CrawlingHub DB Secret:"
aws ssm get-parameter --name "/crawlinghub/prod/db-user-password-secret-name" --region ${AWS_REGION} --query 'Parameter.Value' --output text 2>/dev/null || echo "  ❌ DB secret name not found"
aws secretsmanager get-secret-value --secret-id "${SECRET_NAME}" --region ${AWS_REGION} --query 'SecretString' --output text 2>/dev/null | jq . || echo "  ❌ Secret not found"
echo ""

echo "📋 CrawlingHub Redis Parameters:"
aws ssm get-parameter --name "/crawlinghub/prod/redis/endpoint" --region ${AWS_REGION} --query 'Parameter.Value' --output text 2>/dev/null || echo "  ❌ Redis endpoint not found"
aws ssm get-parameter --name "/crawlinghub/prod/redis/port" --region ${AWS_REGION} --query 'Parameter.Value' --output text 2>/dev/null || echo "  ❌ Redis port not found"
echo ""

echo "📋 CrawlingHub SQS Parameters:"
aws ssm get-parameter --name "/crawlinghub/prod/sqs/schedule-trigger-queue-url" --region ${AWS_REGION} --query 'Parameter.Value' --output text 2>/dev/null || echo "  ❌ SQS URL not found"
aws ssm get-parameter --name "/crawlinghub/prod/sqs/schedule-trigger-queue-arn" --region ${AWS_REGION} --query 'Parameter.Value' --output text 2>/dev/null || echo "  ❌ SQS ARN not found"
echo ""

echo "✅ 모든 SSM Parameter 설정 완료!"
echo ""
echo "⚠️  주의사항:"
echo "1. Redis Endpoint는 ElastiCache Terraform 배포 후 업데이트하세요"
echo "2. SQS URL/ARN은 SQS Terraform 배포 후 업데이트하세요"
echo ""
echo "📝 다음 단계:"
echo "1. Terraform 배포 순서:"
echo "   a. terraform/elasticache-redis 배포 → Redis Endpoint SSM 업데이트"
echo "   b. terraform/sqs-queue 배포 → SQS URL/ARN SSM 업데이트"
echo "   c. terraform/ecs-service 배포"
