#resource "aws_ecr_repository" "bot_ecr" {
#  name                 = "ex1-polybot-ecr"
#  image_tag_mutability = "MUTABLE"
#
#  image_scanning_configuration {
#    scan_on_push = true
#  }
#  tags = {
#    Name = "EX1_polybot_ecr-terraform"
#  }
#}