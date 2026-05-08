# Create an EC2 instance
resource "aws_instance" "deployment-test-server" {
    ami = "ami-05cf1e9f73fbad2e2"
    instance_type = "t3.micro"
    key_name      = "my-aws-key"
    # attach role EC2-code-deploy-s3-grab 
    iam_instance_profile = "EC2-code-deploy-s3-grab"
    vpc_security_group_ids = [aws_security_group.deployment_test_server_ssh.id]
    tags = {
        Name = "test-server"
    }
}

# Create a role for the deployment test server
