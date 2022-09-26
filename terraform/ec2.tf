

resource "aws_instance" "my_Amazon_linux" {
  # count                  = var.prefix
  ami = "ami-0a1ee2fb28fe05df3" #Amazon Linux AMI
  #  ami                         = "ami-06402f02caa521327" # My Amazon Linux AMI
  instance_type               = "t2.medium"
  vpc_security_group_ids      = [aws_security_group.EX1_polybot-secure-group.id]
  subnet_id                   = aws_subnet.public-subnet-2b.id
  associate_public_ip_address = true
  key_name                    = "alexeymihaylov_key"
  user_data                   = file("script.sh")
#  depends_on                  = [aws_vpc.vpc, aws_autoscaling_group.Polybot-aws_autoscaling_group]
  tags = {
    Name        = "${var.project_name} -client"
    environment = "tf"
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



