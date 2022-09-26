# Create Public Subnet 1
# terraform aws create subnet
  resource "aws_subnet" "public-subnet-1a" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "11.0.0.0/24"
  map_public_ip_on_launch = true
  availability_zone       = var.availability_zone_a
  tags = {
    Name = "${var.project_name}-public-subnet-1--terraform"
  }
}
# Create Public Subnet 2
# terraform aws create subnet
resource "aws_subnet" "public-subnet-2b" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "11.0.3.0/24"
  map_public_ip_on_launch = true
  availability_zone       = var.availability_zone_b
  tags = {
    Name = "${var.project_name}-public-subnet-2--terraform"
  }
}


# # Create Private Subnet 1
# # terraform aws create subnet
# resource "aws_subnet" "private-subnet-1" {

#   vpc_id                  = aws_vpc.vpc.id
#   cidr_block              = "11.0.2.0/24"
#   map_public_ip_on_launch = false
#   availability_zone       = var.availability_zone_a

#   tags = {
#     Name = "Alexey-private-alexey-subnet-1-terraform"
#   }
# }
