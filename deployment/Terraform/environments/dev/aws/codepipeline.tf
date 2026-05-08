# Create s3 bucket for codepipeline
resource "aws_s3_bucket" "codepipeline_bucket" {
    bucket = "mini-ride-share-app-codepipeline-bucket"
    tags = {
        Name = "mini-ride-share-app-codepipeline-bucket"
    }
}
