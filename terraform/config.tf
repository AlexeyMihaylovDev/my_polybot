terraform {
  backend "s3" {
    bucket = var.bucket_name_tf
    key    = "my-terraform-project"
    region = var.region

  }
}