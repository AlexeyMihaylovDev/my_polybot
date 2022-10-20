
@Library('global_jenkins_functions') _


import groovy.time.TimeDuration
import groovy.transform.Field


/*************************************************************** | PARAMETERS | ******************************************************************/

// GLOABAL PARAMETERS


@Field JOB = [
        allModules: [
                "BOT": [
                        dockerfile               : "Dockerfiles/Dockerfile_bot",
                        tag                      : "",
                        last_tagName             : "",
                        details                  : "bot"

                ],
                "WORKER": [
                        dockerfile               : "Dockerfiles/Dockerfile_worker",
                        tag                      : "",
                        last_tagName             : "",
                        details                  : "worker"


                ]
        ]
]



JOB.git_project_url = "git@github.com:AlexeyMihaylovDev/PolyBot.git"
JOB.project_name = "PolyBot"
JOB.devops_sys_user = "my_polybot_key"
JOB.branch = "int_terraform"
JOB.ssh_key = "ubuntu_bot_instances"
JOB.email_recepients = "mamtata2022@gmail.com" //TODO: add all developers of projects
JOB.ansible_inventory = ""



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
        ansiColor('xterm')
    }

    agent none

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
            agent {
                docker {
                    label 'linux'
                    image '352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_jenk_agent:final'
                    args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
        }
        stage('Inputs') {
            when { expression { params.Build_Type != "auto_trigger" } }
            steps {
                script {
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
                                      ]]],
                            booleanParam(description: 'Click to checkbox if you want to run deploy stages', name: 'Continue_Deploy'),
                            booleanParam(description: 'Run Code analysis SonarQube', name: 'sonar_code_analysis'),
                            booleanParam(description: 'Run Tests_UI', name: 'test_ui')
                    ]
                    println("-------------------------Inputs provided by user:--------------------------------")
                    JOB.params.Build_Type = userInput["Build_Type"]
                    JOB.params.modules = userInput["Modules"]
                    JOB.tagName = userInput['TAG']
                    JOB.deploy = userInput['Continue_Deploy']
                    JOB.sonar = userInput['sonar_code_analysis']
                    JOB.test_ui = userInput['test_ui']

                    println(JOB.params.modules)

                }

            }
            agent {
                docker {
                    label 'linux'
                    image '352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_jenk_agent:final'
                    args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
        }

        stage('Auto input') {
            when { expression { params.Build_Type == "auto_trigger" } }

            steps {
                script {
                    JOB.params.Build_Type = "Build_Type_automaticly"
                    JOB.params.modules = "BOT - (latest_tag:  )"
                    JOB.tagName = "${env.BUILD_NUMBER}"
                    JOB.deploy = params.Deploy
                    JOB.tagName = env.BUILD_NUMBER
                    JOB.sonar = false

                    println(JOB.params.Build_Type)
                    println(JOB.params.modules)
                    println(JOB.deploy)
                    println(JOB.tagName)
                    println(JOB.sonar)
                }
            }
            agent {
                docker {
                    label 'linux'
                    image '352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_jenk_agent:final'
                    args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
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
            agent {
                docker {
                    label 'linux'
                    image '352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_jenk_agent:final'
                    args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
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
            agent {
                docker {
                    label 'linux'
                    image '352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_jenk_agent:final'
                    args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }

        }
        stage('Code analysis by sonar') {
            when { expression { JOB.sonar == true } }
            steps {
                script {
                    println("===================================${STAGE_NAME}=============================================")

                    def scannerHome = tool 'SonarScannerDoc'
                    withSonarQubeEnv('SonarScanner') {
                        sh "${scannerHome}/bin/sonar-scanner"
                    }
                }
            }
            agent {
                docker {
                    label 'linux'
                    image '352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_jenk_agent:final'
                    args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
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
            agent {
                docker {
                    label 'linux'
                    image '352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_jenk_agent:final'
                    args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }

        }

        stage("build") {
            steps {
                sh "aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin $REGISTRY_URL"
                script {
                    println("=================================" + JOB.modules)

                    JOB.modules.each { moduleName, moduleDetails ->
                        if (JOB.modules[moduleName]['details'] == "bot") {
                            sh "docker build -t $BOT_ECR_NAME:${JOB.tagName} -f  ${JOB.modules[moduleName]['dockerfile']} ."
                            sh "docker tag $BOT_ECR_NAME:${JOB.tagName} $REGISTRY_URL/$BOT_ECR_NAME:${JOB.tagName}"
                            sh "docker push $REGISTRY_URL/$BOT_ECR_NAME:${JOB.tagName}"

                        } else {
                            sh "docker build -t $BOT_ECR_NAME:${JOB.tagName} -f  ${JOB.modules[moduleName]['dockerfile']} ."
                            sh "docker tag $BOT_ECR_NAME:${JOB.tagName} $REGISTRY_URL/$BOT_ECR_NAME:${JOB.tagName}"
                            sh "docker push $REGISTRY_URL/$BOT_ECR_NAME:${JOB.tagName}"
                        }
                    }

                }
            }
            agent {
                docker {
                    label 'linux'
                    image '352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_jenk_agent:final'
                    args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }

        }
        stage("Generate Ansible Inventory") {
            when { expression { JOB.deploy == true } }
            environment {
                IMAGE_ID = "${env.REGISTRY_URL}/${env.BOT_ECR_NAME}:${JOB.tagName}"
            }
            steps {

                sh '/usr/local/bin/ansible-galaxy collection install community.general'
                sh 'aws ec2 describe-instances --region $BOT_EC2_REGION --filters "Name=tag:App,Values=$BOT_EC2_APP_TAG,running" --filters Name=instance-state-name,Values=running --query "Reservations[].Instances[]" > hosts.json'
                sh 'python3 ${PREPAIR_ANSIBLE_INV_PATH}'
                sh '''
        echo "Inventory generated"
        cat hosts
        '''
                withCredentials([sshUserPrivateKey(credentialsId: "${JOB.ssh_key}", usernameVariable: 'ssh_user', keyFileVariable: 'privatekey')]) {
                    sh '''
           /usr/local/bin/ansible-playbook $ANSIBLE_INVENROTY_PATH --extra-vars "registry_region=$REGISTRY_REGION  registry_url=$REGISTRY_URL bot_image=$IMAGE_ID" --user=${ssh_user} -i hosts --private-key ${privatekey}
            '''
                }
            }
            agent {
                docker {
                    label 'linux'
                    image '352708296901.dkr.ecr.eu-central-1.amazonaws.com/alexey_jenk_agent:final'
                    args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }

        }

        stage("Run UI Tests ") {
            when { expression { JOB.test_ui == true } }
            steps {
                println("===================================${STAGE_NAME}=============================================")
                git branch: "test_project", url: "https://github.com/AlexeyMihaylovDev/selenoid.git"
                dir('terraform') {
                    sh "terraform init"
                    sh "terraform plan -out=myplan.txt"
                    sh ' terraform apply "myplan.txt"'
                    sh 'terraform output -json > output.json'
                    sh 'python3  readJson.py'
                    sh 'cp ip_output.txt  $WORKSPACE/src/main/resources/'

                }
                sleep(time:3,unit:"MINUTES")
                sh 'mvn --version'
                sh 'mvn clean install -Dsurefire.suiteXmlFiles=Testng_test.xml -Dlog4j.configurationFile=src/main/resources/log4J2.xml'
                println('Public allure reports')
                allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]
            }
            agent {
                node { label 'linux' }
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

