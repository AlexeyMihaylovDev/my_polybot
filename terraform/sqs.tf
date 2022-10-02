 resource "aws_sqs_queue" "terraform_queue" {
  name                      = var.sqs_name
  delay_seconds             = 90
  max_message_size          = 262144
  message_retention_seconds = 345600
  receive_wait_time_seconds = 10
  visibility_timeout_seconds = 30
  

  tags = {
    Name = var.sqs_name
    Environment = "production"
  }
}

resource "aws_sqs_queue_policy" "sqs_queue_policy" {
  queue_url = aws_sqs_queue.terraform_queue.id
  policy    = <<POLICY
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": [
                "sqs:*"
            ],
            "Effect": "Allow",
            "Resource": "*"
        }
    ]
}
POLICY
}