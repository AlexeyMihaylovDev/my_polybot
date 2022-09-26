#!/bin/bash
docker run  --env-file  /home/ec2-user/app/.envfile  -d -v /home/ec2-user/app/:/.secret  352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexeyimapolybot:latest