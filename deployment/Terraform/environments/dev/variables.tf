variable "project_id" {
  description = "GCP project ID for this environment"
  type        = string
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
