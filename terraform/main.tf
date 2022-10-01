

provider "aws" {
  region = var.region

}
terraform {
  backend "s3" {
    bucket = var.bucket_name_tf
    key    = "aws/terraform/terraform.tfstate"
    encrypt = true
    region = var.region
  }
}


