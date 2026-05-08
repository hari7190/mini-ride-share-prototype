data "aws_vpc" "default" {
    default = true
}

resource "aws_security_group" "deployment_test_server_ssh" {
    name        = "deployment-test-server-ssh"
    description = "Allow SSH from a trusted CIDR only"
    vpc_id      = data.aws_vpc.default.id

    ingress {
        description = "SSH from laptop"
        from_port   = 22
        to_port     = 22
        protocol    = "tcp"
        cidr_blocks = [var.ssh_ingress_cidr]
    }

    egress {
        from_port   = 0
        to_port     = 0
        protocol    = "-1"
        cidr_blocks = ["0.0.0.0/0"]
    }

    tags = {
        Name = "deployment-test-server-ssh"
    }
}
