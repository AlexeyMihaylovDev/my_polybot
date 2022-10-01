resource "aws_launch_template" "EX1_polybot_temp" {
  name                   = "${var.project_name}-template-alexey"
  image_id               = "ami-044fc6ddbb6094b68" // worker-ami
  instance_type          = "t2.small"
  key_name               = var.key_name
#  vpc_security_group_ids = [aws_security_group.EX1_polybot-secure-group.id]
#    user_data              = base64encode(data.template_file.test.rendered)

  tags = {
    Name = "${var.project_name}-scalling"
  }
  tag_specifications {
    resource_type = "instance"

    tags = {
      Name = "${var.project_name}-worker"
    }
  }
#  network_interfaces {
#    associate_public_ip_address = true
#    subnet_id = "subnet-04900a3592e4a846e"
#  }


  lifecycle {
    create_before_destroy = true
  }

}


#data "template_file" "test" {
#  template = file("templet.sh")
#}
