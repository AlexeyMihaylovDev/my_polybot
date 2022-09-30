resource "aws_autoscaling_group" "Polybot-aws_autoscaling_group" {
  name                 = "${var.project_name}-autoscaling-group"
  desired_capacity     = 0
  max_size             = 3
  min_size             = 0
  vpc_zone_identifier  = [aws_subnet.public-subnet-1a.id, aws_subnet.public-subnet-2b.id]
  default_cooldown     = 60
  launch_configuration = aws_launch_configuration.launch_config.name
  force_delete         = true
  tag {
    key                 = var.key_name
    propagate_at_launch = true
    value               = "ec2 instance"

  }

  #  launch_template {
  #    id = aws_launch_template.EX1_polybot_temp.id
  #    version = aws_launch_template.EX1_polybot_temp.latest_version
  #  }
}

resource "aws_launch_configuration" "launch_config" {
  image_id      = data.aws_ami.ubuntu.id
  instance_type = "t2.micro"
  key_name      = var.key_name
  #  user_data       = file("script_worker.sh")
  name            = var.project_name
  security_groups = [aws_security_group.EX1_polybot-secure-group.id]


}
#resource "aws_autoscaling_policy" "cpu-policy" {
#  autoscaling_group_name = aws_autoscaling_group.Polybot-aws_autoscaling_group.name
#  name                   = "sqs11-target-tracking-scaling-policy"
#  policy_type = "TargetTrackingScaling"
#  target_tracking_configuration {
#    customized_metric_specification {
#      metric_name = "backlog_per_instance"
#      namespace   = "Alexey_Dima_polybot_metric/AutoScaling"
#      statistic   = "Average"
#      unit = "Count"
#
#    }
#    target_value = 5
#  }
#}
#resource "aws_autoscaling_notification" "exam-notify" {
#  group_names   = [aws_autoscaling_group.Polybot-aws_autoscaling_group.name]
#  notifications = [
#  "autoscaling EC2_INSTANCE_LAUNCH",
#  "autoscaling EC2_INSTANCE_TERMINATE",
#  "autoscaling EC2_INSTANCE_LAUNCH"]
##  topic_arn     = "aws_sns_topic.cpu-sns.arn"
#
#}

