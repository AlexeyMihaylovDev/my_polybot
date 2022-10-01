

provider "aws" {
  region = var.region

}
terraform {
  backend "s3" {
    bucket = "terraforstate-backet-alexey"
    key    = "aws/terraform/terraform.tfstate"
    encrypt = true
    region = "eu-central-1"
  }
}


