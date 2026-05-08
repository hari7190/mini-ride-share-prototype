terraform {
    required_providers {
        aws = {
            source = "hashicorp/aws"
            version = "~> 6.0"
        }
    }
}

# define backend mini-ride-share-app-terraform-backend
terraform {
    backend "s3" {
        bucket = "mini-ride-share-app-terraform-backend"
        key = "terraform/state/dev"
        region = "us-east-1"
        encrypt = true
        use_lockfile = true
    }
}

provider "aws" {
    region = "us-east-1"
}

