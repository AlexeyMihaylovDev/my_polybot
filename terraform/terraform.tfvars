# Rename or copy this file to terraform.tfvars
# Prefix must be all lowercase letters, digits, and hyphens.
# Make sure it is at least 5 characters long.

prefix              = "2"
region              = "eu-central-1"
cidr_block_vpc      = "11.0.0.0/16"
route_table         = "0.0.0.0/0"
availability_zone_a = "eu-central-1a"
availability_zone_b = "eu-central-1b"
bucket_name         = "polybot-bucket"
acl_value           = "private"
project_name        = "Polybot"
key_name            = "polybot"
bucket_name_tf      = "terraform-state-backet"