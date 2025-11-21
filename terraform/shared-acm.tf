# ========================================
# Shared Infrastructure Reference: ACM
# ========================================
# SSL/TLS Certificate for ALB HTTPS
# Wildcard certificate: *.set-of.com
# ========================================

# ACM Certificate ARN
data "aws_ssm_parameter" "acm_certificate_arn" {
  name = "/shared/acm/certificate-arn"
}

# ========================================
# Locals for easy access
# ========================================
locals {
  certificate_arn = data.aws_ssm_parameter.acm_certificate_arn.value
}
