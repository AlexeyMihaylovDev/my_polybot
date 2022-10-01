
@Library('global_jenkins_functions') _

import groovy.transform.Field


/*************************************************************** | PARAMETERS | ******************************************************************/


@Field JOB = [:]


JOB.git_project_url = "git@github.com:AlexeyMihaylovDev/PolyBot.git"
JOB.project_name = "terraform_bot"
JOB.devops_sys_user = "my_polybot_key"
JOB.branch = "main"
JOB.email_recepients = "mamtata2022@gmail.com" //TODO: add all developers of projects




properties([parameters([
        [$class: 'ParameterSeparatorDefinition', name: 'HEADER', sectionHeader: 'You will be prompted for parameters during the run.'],
        [$class: 'WHideParameterDefinition', defaultValue: "", description: '', name: 'Build_Type'],
        [$class: 'WHideParameterDefinition', defaultValue: "System", description: '', name: 'JOB_EXECUTOR'],
        [$class: 'WHideParameterDefinition', defaultValue: "System", description: '', name: 'JOB_EXECUTOR_ID'],
        [$class: 'WHideParameterDefinition', defaultValue: "", description: '', name: 'Deploy']

])])

pipeline {
    options {
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '30'))
        timestamps()
    }

    agent {
        docker { image '276105822531.dkr.ecr.eu-central-1.amazonaws.com/jenk_agent:3'
            label 'linux'
            args  '--user root -v /var/run/docker.sock:/var/run/docker.sock' }
    }
    tools {
        terraform 'terraform'
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
            when{ expression { params.Build_Type != "auto_trigger"}}
            steps {
                script {
                    println("===================================${STAGE_NAME}=============================================")
                    def userInput = input id: 'UserInput', message: 'Please provide parameters.', ok: 'OK', parameters: [
                            choice(name: 'Build_Type', choices: ['APPLY', 'DESTROY', 'PLAN'], description: '\'APPLY\' - Create framework. \'DESTROY\' - Destroy framework. \'PLAN\' - Show framework.'),
                            booleanParam(description: 'Click to checkbox if you want to run deploy stages', name: 'Continue_Deploy')
                    ]
                    println("-------------------------Inputs provided by user:--------------------------------")
                    JOB.Build_Type = userInput["Build_Type"]
                    JOB.deploy = userInput['Continue_Deploy']
                    println(JOB.params.modules)
                }

            }
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
        stage("Run ansible command ") {
            steps {
                script {
                    println("===================================${STAGE_NAME} : ${JOB.Build_Type} =============================================")
                    dir ('terraform'){
                        sh 'terraform init'
                    }


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

