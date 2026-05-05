terraform {
    backend "gcs" {
        bucket = "mini-ride-share-app-terraform-backend"
        prefix = "terraform/state/dev"
    }
}
