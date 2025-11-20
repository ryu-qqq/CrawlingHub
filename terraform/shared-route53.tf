# ========================================
# Shared Infrastructure Reference: Route53
# ========================================
# Hosted Zone for DNS records
# Domain: set-of.com
# ========================================

# Route53 Hosted Zone ID
data "aws_ssm_parameter" "route53_zone_id" {
  name = "/shared/route53/hosted-zone-id"
}

# ========================================
# Locals for easy access
# ========================================
locals {
  route53_zone_id = data.aws_ssm_parameter.route53_zone_id.value
  fqdn            = "${var.subdomain}.${var.domain_name}"
}
