

resource "aws_instance" "ubuntu_linux" {
  # count                  = var.prefix
  ami                         = data.aws_ami.ubuntu.id #Amazon Linux AMI
  instance_type               = "t2.medium"
  vpc_security_group_ids      = [aws_security_group.EX1_polybot-secure-group.id]
  subnet_id                   = aws_subnet.public-subnet-2b.id
  associate_public_ip_address = true
  key_name                    = var.key_name
  iam_instance_profile        = aws_iam_instance_profile.ec2_profile.name
  user_data                   = file("user_data.sh")
  #  depends_on                  = [aws_vpc.vpc, aws_autoscaling_group.Polybot-aws_autoscaling_group]
  tags = {
    Name        = "${var.project_name}-client"
    environment = "tf"
    App         = "alexey-bot"

  }


























  #  provisioner "file" {
  #    source      = ".telegramToken"
  #    destination = ".telegramToken"
  #
  #    connection {
  #      type        = "ssh"
  #      user        = "ec2-user"
  #      private_key = file("alexeymihaylov_key.pem")
  #      host        = self.public_ip
  #    }
  #  }
  #  provisioner "file" {
  #    source      = "script.sh"
  #    destination = "/tmp/script.sh"
  #  }
  #
  #  provisioner "remote-exec" {
  #    inline = [
  #      "chmod +x /tmp/script.sh",
  #      "/tmp/script.sh args",
  #    ]
  #  }
  #  connection {
  #    type        = "ssh"
  #    user        = "ec2-user"
  #    private_key = file("alexeymihaylov_key.pem")
  #    host        = self.public_ip
  #  }
}



