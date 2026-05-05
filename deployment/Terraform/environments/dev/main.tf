terraform {
    required_providers {
        google = {
            source = "hashicorp/google"
            version = "~> 5.0"
        }
    }
}

provider "google" {
    project = var.project_id
    region = var.region
    impersonate_service_account = "${var.terraform_service_account_id}@${var.project_id}.iam.gserviceaccount.com"
}

resource "google_project_service" "compute"{
    project = var.project_id
    service = "compute.googleapis.com"
    disable_on_destroy = false
}

resource "google_project_service" "container"{
    project = var.project_id
    service = "container.googleapis.com"
    disable_on_destroy = false
}

resource "google_storage_bucket" "artifacts" {
    name = "${var.project_id}-${var.environment}-artifacts"
    location = upper(var.region)
    force_destroy = false
    public_access_prevention = "enforced"
    uniform_bucket_level_access = true

    labels = {
        environment = var.environment,
        managed_by = "terraform"
    }
}

output "artifacts_bucket_name" {
    value = google_storage_bucket.artifacts.name
}

# Terraform service account
resource "google_service_account" "terraform" {
    account_id = var.terraform_service_account_id
    display_name = "Terraform Service Account for ${var.environment} environment"
}

locals {
    terraform_sa_roles = [
        "roles/storage.admin",
        "roles/storage.objectViewer",
        "roles/iam.serviceAccountUser",
        "roles/serviceusage.serviceUsageAdmin",
        "roles/resourcemanager.projectIamAdmin"
    ]
}

resource "google_project_iam_member" "terraform_storage_admin" {
    for_each = toset(local.terraform_sa_roles)
    project = var.project_id
    role = each.value
    member = "serviceAccount:${google_service_account.terraform.email}"
}
