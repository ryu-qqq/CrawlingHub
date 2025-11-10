# GitHub Actions OIDC for CrawlingHub

GitHub Actions가 AWS 리소스에 접근할 수 있도록 OIDC 인증을 설정합니다.

## 📋 생성되는 리소스

- **OIDC Identity Provider**: GitHub Actions용 OpenID Connect Provider
- **IAM Role**: GitHub Actions가 assume할 수 있는 Role
- **IAM Policies**: ECR, ECS, Terraform 권한
- **SSM Parameter**: Role ARN 저장

## 🚀 사용 방법

### 1. Terraform 초기화 및 Plan

```bash
cd terraform/github-oidc
terraform init
terraform plan
```

### 2. Terraform Apply

```bash
terraform apply
```

Apply 완료 후 출력되는 `github_actions_role_arn`을 복사합니다:

```
Outputs:
github_actions_role_arn = "arn:aws:iam::ACCOUNT_ID:role/crawlinghub-prod-github-actions-role"
```

### 3. GitHub Secret 설정

Repository Settings → Secrets and variables → Actions → New repository secret

```
Name: AWS_ROLE_ARN
Value: arn:aws:iam::ACCOUNT_ID:role/crawlinghub-prod-github-actions-role
```

### 4. GitHub Actions Workflow 확인

`.github/workflows/build-and-deploy.yml`이 다음과 같이 설정되어 있는지 확인:

```yaml
- name: Configure AWS credentials
  uses: aws-actions/configure-aws-credentials@v4
  with:
    role-to-assume: ${{ secrets.AWS_ROLE_ARN }}
    aws-region: ap-northeast-2
    role-session-name: GitHubActions-CrawlingHub-TerraformPlan
```

## 📦 생성된 권한

### ECR (Docker Registry)
- `ecr:GetAuthorizationToken`
- `ecr:PutImage`
- `ecr:BatchCheckLayerAvailability`
- 기타 Docker push/pull 권한

### ECS (Container Orchestration)
- `ecs:DescribeServices`
- `ecs:RegisterTaskDefinition`
- `ecs:UpdateService`
- `iam:PassRole` (ECS Task용)

### Terraform (Infrastructure as Code)
- S3 backend 접근 (`prod-connectly` bucket)
- DynamoDB lock 접근 (`prod-connectly-tf-lock` table)
- EC2, ElastiCache, SQS Describe 권한

## 🔒 보안

### Trust Policy
GitHub Actions는 다음 조건을 만족할 때만 이 Role을 assume할 수 있습니다:

```json
{
  "StringEquals": {
    "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
  },
  "StringLike": {
    "token.actions.githubusercontent.com:sub": "repo:ryu-qqq/CrawlingHub:*"
  }
}
```

- **Repository**: `ryu-qqq/CrawlingHub`만 허용
- **Audience**: `sts.amazonaws.com`만 허용
- **Session Duration**: 최대 1시간

## 🛠️ 트러블슈팅

### 에러: "Not authorized to perform sts:AssumeRoleWithWebIdentity"

**원인**: OIDC Provider가 아직 생성되지 않았거나, Trust Policy가 잘못 설정됨

**해결**:
```bash
cd terraform/github-oidc
terraform apply
```

### OIDC Provider Thumbprint 업데이트 필요

GitHub이 인증서를 변경한 경우 `main.tf`의 `thumbprint_list` 업데이트:

```bash
# 새 thumbprint 확인
openssl s_client -servername token.actions.githubusercontent.com \
  -showcerts -connect token.actions.githubusercontent.com:443 < /dev/null 2>/dev/null | \
  openssl x509 -fingerprint -noout | cut -d'=' -f2 | tr -d ':' | tr '[:upper:]' '[:lower:]'
```

## 📚 참고 문서

- [GitHub OIDC Documentation](https://docs.github.com/en/actions/deployment/security-hardening-your-deployments/configuring-openid-connect-in-amazon-web-services)
- [AWS IAM OIDC Provider](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_providers_create_oidc.html)
- [GitHub Actions AWS Credentials](https://github.com/aws-actions/configure-aws-credentials)
