
@Library('global_jenkins_functions') _

import groovy.transform.Field


/*************************************************************** | PARAMETERS | ******************************************************************/

// GLOABAL PARAMETERS


@Field JOB = [
        allModules: [
                "BOT": [
                        dockerfile               : "Dockerfile_bot",
                        tag                      : "",
                        last_tagName             : "",
                        details                  : "bot"
                ],
                "WORKER": [
                        dockerfile               : "Dockerfile_worker",
                        tag                      : "",
                        last_tagName             : "",
                        details                  : "worker"

                ]
        ]
]



JOB.git_project_url = "git@github.com:AlexeyMihaylovDev/PolyBot.git"
JOB.project_name = "PolyBot"
JOB.devops_sys_user = "bot_user"
JOB.branch = "deploy"
JOB.ssh_key = "ubuntu_ssh"
JOB.email_recepients = "" //TODO: add all developers of projects



properties([parameters([
        [$class: 'ParameterSeparatorDefinition', name: 'HEADER', sectionHeader: 'You will be prompted for parameters during the run.'],
        [$class: 'WHideParameterDefinition', defaultValue: "", description: '', name: 'Build_Type'],
        [$class: 'WHideParameterDefinition', defaultValue: "System", description: '', name: 'JOB_EXECUTOR'],
        [$class: 'WHideParameterDefinition', defaultValue: "System", description: '', name: 'JOB_EXECUTOR_ID']
])])

pipeline {
    options {
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '30'))
    }
    agent {
        node { label 'linux' }
    }

    stages {
        stage('Get & Print Job Parameters') {
            steps {
                script {
                    def cause = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause')

                    println("userName: ${cause.userName}")
                    JOB.allModules.each { moduleName, moduleDetails ->
                    }
                    //"params" is immutable, so in order to modify it need to copy it to another object.
                    JOB.params = [:]
                    params.each { key, value ->
                        if (!value.equals("")) {
                            println("${key}: ${value}")
                            JOB.params["${key}"] = "${value}"
                            println(JOB.params)
                        }
                    }
                }
            }
        }
        stage('Inputs') {
            steps {
                script {
                    if (currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause')['userId']) {
                        println("===================================${STAGE_NAME}=============================================")
                        String modul_tag = JOB.allModules.collect {
                            "${it.getKey()} - (latest_tag:  ${it.getValue().last_tagName})"
                        }.join(",")
                        def userInput = input id: 'UserInput', message: 'Please provide parameters.', ok: 'OK', parameters: [
                                [$class: 'WHideParameterDefinition', defaultValue: modul_tag, description: '', name: 'last_tag'],
                                choice(name: 'Build_Type', choices: ['MANUAL', 'CI', 'DAILY'], description: '\'CI\' - Compilation only. \'DAILY\' - Compiltaion and code analysis. \'MANUAL\' - Release version.'),
                                string(description: 'Please write tag name \n***** (If you select multiple modules to build the tag name will be ignored) ******* ', name: 'TAG', trim: true),
                                [$class: 'CascadeChoiceParameter', choiceType: 'PT_CHECKBOX', filterLength: 1, filterable: false,
                                 name  : 'Modules', referencedParameters: 'last_tag',
                                 script: [$class: 'GroovyScript', fallbackScript: [classpath: [], oldScript: '', sandbox: true, script: 'return [\'error\']'],
                                          script: [classpath: [], oldScript: '', sandbox: true, script: '''return last_tag.split(",").toList()'''.toString()
                                          ]]]]
                        println("-------------------------Inputs provided by user:--------------------------------")
                        JOB.params.Build_Type = userInput["Build_Type"]
                        JOB.params.modules = userInput["Modules"]
                        JOB.tagName = userInput['TAG']

                        println(JOB.params.modules)
                    } else {
                        JOB.params.Build_Type = "Build_Type_automaticly"
                        JOB.params.modules = "BOT - (latest_tag:  ),WORKER - (latest_tag:  )"
                        JOB.tagName = "${env.BUILD_NUMBER}"

                        println(JOB.params.modules)
                    }

                }
            }
        }
        stage('Set Additional Parameters') {
            steps {
                script {
                    println("Modules string:\n " + JOB.params.modules)
                    JOB.modules = [:]

                    JOB.params.modules.split(",").collect { it.replaceAll(" - .+\$", "") }.each { moduleName ->
                        JOB.modules[moduleName] = JOB.allModules[moduleName]

                        println(JOB.modules)
                    }
                }
            }
        }

        stage('Clone') {
            steps {
                script {
                    // Delete Workspace
                    cleanWs()
                    // Clone PolyBot repository.
                    git branch: "${JOB.branch}", credentialsId: "${JOB.devops_sys_user}", url: 'git@github.com:AlexeyMihaylovDev/PolyBot.git'
                    JOB.gitCommitHash = global_gitInfo.getCommitHash(JOB.branch)
                    println("====================${JOB.gitCommitHash}==============")
                }
            }
        }
        stage("set git tag") {
            steps {
                script {
                    JOB.commitAuthor = global_gitInfo.getCommitAuthor()
                    JOB.commitEmail = global_gitInfo.getCommitEmail()
                    JOB.lastCommitMassage = global_gitInfo.getLastCommitMassage()
                }
            }
        }

        stage("build") {
            steps {
                sh "aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin 352708296901.dkr.ecr.eu-central-1.amazonaws.com"
                script {
                    println("=================================" + JOB.modules)

                    JOB.modules.each { moduleName, moduleDetails ->
                        if (JOB.modules[moduleName]['details'] == "bot") {
                            sh "docker build -t alexey_bot_client:${JOB.tagName} -f  ${JOB.modules[moduleName]['dockerfile']} ."
                            sh "docker tag alexey_bot_client:${JOB.tagName} 352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_bot_client:${JOB.tagName}"
                            sh "docker push 352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_bot_client:${JOB.tagName}"

                        } else {
                            sh "docker build -t alexey_worker:${JOB.tagName} -f  ${JOB.modules[moduleName]['dockerfile']} ."
                            sh "docker tag alexey_worker:${JOB.tagName} 352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_worker:${JOB.tagName}"
                            sh "docker push 352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_worker:${JOB.tagName}"
                        }

                    }

                }
            }
        }
        stage("Install Ansible") {
            steps {
//                sh 'python3 -m pip install ansible'
                sh '/usr/bin/ansible-galaxy collection install community.general'
            }
        }
        stage("Generate Ansible Inventory") {
            environment {
                BOT_EC2_APP_TAG = "alexey-bot"
                BOT_EC2_REGION = "eu-central-1"
            }
            steps {
                sh 'aws ec2 describe-instances --region $BOT_EC2_REGION --filters "Name=tag:App,Values=$BOT_EC2_APP_TAG" --query "Reservations[].Instances[]" > hosts.json'
                sh 'python3 prepare_ansible_inv.py'
                sh '''
        echo "Inventory generated"
        cat hosts
        '''
            }
        }
        stage('Ansible Bot Deploy') {
            environment {
                ANSIBLE_HOST_KEY_CHECKING = "False"
                REGISTRY_URL = "352708296901.dkr.ecr.eu-central-1.amazonaws.com"
                REGISTRY_REGION = "eu-central-1"
            }

            steps {
                withCredentials([sshUserPrivateKey(credentialsId: "${JOB.ssh_key}", usernameVariable: 'ssh_user', keyFileVariable: 'privatekey')]) {
                    sh '''
           /usr/bin/ansible-playbook botDeploy.yaml --extra-vars "registry_region=$REGISTRY_REGION  registry_url=$REGISTRY_URL bot_image=352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_bot_client:100" --user=${ssh_user} -i hosts --private-key ${privatekey}
            '''
                }
            }
        }
    }
    post {

        always {
            script {
                currentBuild.description = ("Branch : ${JOB.branch}\n GitCommiter : ${JOB.commitAuthor}\nGitLastMassage: ${JOB.lastCommitMassage}")


            }
        }
    }
}
