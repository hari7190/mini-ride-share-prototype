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

    uniform_bucket_level_access = true

    labels = {
        environment = var.environment,
        managed_by = "terraform"
    }
}

output "artifacts_bucket_name" {
    value = google_storage_bucket.artifacts.name
}
