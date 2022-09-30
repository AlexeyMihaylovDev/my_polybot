
@Library('global_jenkins_functions') _

import groovy.transform.Field



@Field JOB = [:]


JOB.git_project_url = "git@github.com:AlexeyMihaylovDev/PolyBot.git"
JOB.project_name = "PolyBot"
JOB.devops_sys_user = "my_polybot_key"
JOB.branch = "main"
JOB.ssh_key = "ubuntu_ssh"
JOB.email_recepients = "mamtata2022@gmail.com" //TODO: add all developers of projects
JOB.ansible_inventory = ""


pipeline {
    options {
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '30'))
        timestamps()
    }

    agent {
        docker { image '352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_jenk_agent:ubuntu'
            label 'aws_linux'
            args  '--user root -v /var/run/docker.sock:/var/run/docker.sock' }
    }


    environment {
        ANSIBLE_HOST_KEY_CHECKING = "False"
        REGISTRY_URL = "352708296901.dkr.ecr.eu-central-1.amazonaws.com"
        REGISTRY_REGION = "eu-central-1"
        BOT_ECR_NAME = "alexey_bot_client"
        IMAGE_ID = "${env.REGISTRY_URL}/alexey_bot_client"
        BOT_EC2_APP_TAG = "alexey-bot"
        BOT_EC2_REGION = "eu-central-1"
        ANSIBLE_INVENROTY_PATH = "ansible/botDeploy.yaml"
        PREPAIR_ANSIBLE_INV_PATH = "ansible/prepare_ansible_inv.py"
    }
        stage('Clone') {
            steps {
                script {
                    // Clone PolyBot repository.
                    git branch: "${JOB.branch}", credentialsId: "${JOB.devops_sys_user}", url: 'git@github.com:AlexeyMihaylovDev/my_polybot.git'
                    JOB.gitCommitHash = global_gitInfo.getCommitHash(JOB.branch)
                    println("====================${JOB.gitCommitHash}==============")
                }
            }

        }
    stages {
        stage ('terraform Action') {
            steps {
                script {

                }
            }
        }

    }


    post {
        always {
            script {
                currentBuild.description = ("Branch : ${JOB.branch}\n GitCommiter : ${JOB.commitAuthor}\nDeploy_server: ${JOB.deploy}")

                EMAIL_MAP = [
                        "Modules"       : JOB.params.modules,
                        "Build Type"    : JOB.params.Build_Type,
                        "Deploy To Env" : JOB.params.Deploy_Environment,
                        "Job Name"      : JOB_NAME,
                        "Run deploy "   : JOB.deploy,
                        "Build Number"  : BUILD_NUMBER,
                        "Git tag Name"  : JOB.tagName,
                        "Branch"        : "${JOB.branch}",
                        "More Info At"  : "<a href=${BUILD_URL}console> Click here to view build console on Jenkins. </a>",
                        "painted"       : "false"
                ]
                global_sendGlobalMail.sendByMapFormat(JOB.email_recepients, currentBuild.result, EMAIL_MAP, JOB.emails,
                        "Jenkins Report", "Build Notification - Jenkins Report", "BOT build")

            }
        }
    }
}

