variable "ssh_ingress_cidr" {
  description = "CIDR allowed to SSH to the deployment test server (for example, 203.0.113.10/32)."
  type        = string
}

variable "ssh_ingress_cidr_secondary" {
  description = "Optional second CIDR for SSH (e.g. office network). Leave empty to omit this rule."
  type        = string
  default     = ""
}
