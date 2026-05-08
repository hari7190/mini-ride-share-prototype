variable "project_id" {
  description = "GCP project ID for this environment"
  type        = string
  // variable validation
  validation {
    condition = length(var.project_id) > 0
    error_message = "Project ID must not be empty."
  }
}

variable "region" {
  description = "GCP region for resources"
  type        = string
  default     = "northamerica-northeast2"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "terraform_service_account_id" {
  description = "Account ID for Terraform service account (without domain)"
  type        = string
  default     = "terraform-dev"
}
