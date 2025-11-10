#!/bin/bash
# ============================================================================
# Build and Push All CrawlingHub Applications to ECR
# ============================================================================
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
AWS_REGION="ap-northeast-2"
AWS_ACCOUNT_ID="646886795421"
ECR_REPOSITORY="crawlinghub-prod"
ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}"

# Applications to build
APPLICATIONS=("web-api" "scheduler" "sqs-listener")

echo "🚀 CrawlingHub Build and Push Script"
echo "===================================="
echo ""

# ============================================================================
# Step 1: ECR Login
# ============================================================================
echo -e "${YELLOW}📦 Step 1: Logging into ECR...${NC}"
aws ecr get-login-password --region ${AWS_REGION} | \
    docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ ECR login successful${NC}"
else
    echo -e "${RED}❌ ECR login failed${NC}"
    exit 1
fi

echo ""

# ============================================================================
# Step 2: Build and Push Each Application
# ============================================================================
for APP in "${APPLICATIONS[@]}"; do
    echo -e "${YELLOW}🔨 Step 2.${APP}: Building ${APP}...${NC}"

    # Build Docker image
    docker build \
        --platform linux/amd64 \
        --build-arg BOOTSTRAP=${APP} \
        -t ${ECR_REPOSITORY}:${APP}-latest \
        -t ${ECR_URI}:${APP}-latest \
        .

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Build successful: ${APP}${NC}"
    else
        echo -e "${RED}❌ Build failed: ${APP}${NC}"
        exit 1
    fi

    echo ""
    echo -e "${YELLOW}📤 Step 3.${APP}: Pushing ${APP} to ECR...${NC}"

    # Push to ECR
    docker push ${ECR_URI}:${APP}-latest

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Push successful: ${APP}${NC}"
    else
        echo -e "${RED}❌ Push failed: ${APP}${NC}"
        exit 1
    fi

    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
done

# ============================================================================
# Summary
# ============================================================================
echo -e "${GREEN}✅ All applications built and pushed successfully!${NC}"
echo ""
echo "📋 Summary:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
for APP in "${APPLICATIONS[@]}"; do
    echo "  ✅ ${ECR_URI}:${APP}-latest"
done
echo ""
echo "🎉 Ready for ECS deployment!"
