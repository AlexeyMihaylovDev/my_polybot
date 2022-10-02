
#!/bin/bash
# install docker
sudo apt-get update
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable" -y
apt-get update
apt-get install docker-ce docker-ce-cli containerd.io -y
systemctl enable docker
systemctl start docker
usermod -aG docker ubuntu

sudo apt-get install python3-pip -y

sudo apt-get  install awscli -y