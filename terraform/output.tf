output "public_dns" {
  description = "The public DNS address assigned to the instance"
  value = try(aws_instance.ubuntu_linux.public_dns, "")
}
output "public_ip" {
  description = "The public IP address assigned to the instance"
  value = try(aws_instance.ubuntu_linux.public_ip, "")
}