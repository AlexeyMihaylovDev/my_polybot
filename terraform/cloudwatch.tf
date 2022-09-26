#
#resource "aws_cloudwatch_metric_alarm" "bat" {
#  alarm_name          = "EX1-Polybot-autoscaling-group-Alarm"
#  comparison_operator = "GreaterThanThreshold"
#  evaluation_periods  = "2"
#  metric_name         = "backlog_per_instance"
#  namespace           = "Alexey_Dima_polybot_metric/AutoScaling"
#  period              = "120"
#  statistic           = "Average"
#  threshold           = "4"
#  unit                = "Count"
#
#  dimensions = {
#    AutoScalingGroupName = aws_autoscaling_group.Polybot-aws_autoscaling_group.name
#  }
#
#  alarm_description = "This metric monitors ec2 cpu utilization"
#  alarm_actions     = [aws_autoscaling_policy.cpu-policy.arn]
#}
##resource "aws_sns_topic" "cpu-sns" {
##  name = "alexey-dima-sns"
##  display_name = "example ASG SNS topic"
##}