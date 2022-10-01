resource "aws_s3_bucket" "bucket" {
  bucket        = var.bucket_name
  force_destroy = true

  tags = {
    Name        = "${var.project_name}-terraform"
    Environment = "tf"
  }
}

resource "aws_s3_bucket_acl" "buck_acl" {
  bucket = aws_s3_bucket.bucket.id
  acl    = "private"
}

### Upload an object
#resource "aws_s3_object" "assassinatos" {
#  bucket = aws_s3_bucket.bucket.id
#  key    = "data/.envfile"
#  acl    = "private"
#  source = ".envfile"
#}
#resource "aws_s3_object" "telegram" {
#  bucket = aws_s3_bucket.bucket.id
#  key    = "data/.telegramToken"
#  acl    = "private"
#  source = "E:/project/INTCollege/BotProject/my_polybot/.telegramToken"
#}
#resource "aws_s3_object" "metric" {
#  bucket = aws_s3_bucket.bucket.id
#  key    = "data/Config2.json"
#  acl    = "private"
#  source = "my_polybot/Config2.json"
#  #  etag   = filemd5("D:/elements/project/PolyBot/.envfile")
#}

