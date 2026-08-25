pipeline {
    agent any

    environment {
        DEPLOY_USER  = 'jgrando'
        DEPLOY_HOST  = '127.0.0.1'
        SSH_KEY      = '/opt/jenkins-home/.ssh/id_ed25519_deploy'
        SSH_OPTS     = '-o StrictHostKeyChecking=accept-new -o IdentitiesOnly=yes'
        JAR_NAME     = 'divergia-backend-0.0.1-SNAPSHOT.jar'
        EXTRACAO_DIR = '/var/www/divergia/extraction'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 20, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {
        stage('Set Environment') {
            steps {
                script {
                    def branchName = env.BRANCH_NAME ?: env.GIT_BRANCH?.replaceAll('origin/', '') ?: ''
                    env.BRANCH_LABEL = branchName ?: 'desconhecida'
                    if (branchName == 'main') {
                        env.PROFILE      = 'prod'
                        env.DEPLOY_DIR   = '/var/www/divergia/prod/backend'
                        env.SERVICE_NAME = 'divergia-prod'
                        env.ENV_LABEL    = 'PRODUÇÃO'
                    } else if (branchName == 'hml') {
                        env.PROFILE      = 'hml'
                        env.DEPLOY_DIR   = '/var/www/divergia/hml/backend'
                        env.SERVICE_NAME = 'divergia-hml'
                        env.ENV_LABEL    = 'HOMOLOG'
                    } else {
                        env.PROFILE   = ''
                        env.ENV_LABEL = 'N/A'
                    }
                }
                echo "Branch: ${env.BRANCH_LABEL} (GIT_BRANCH=${env.GIT_BRANCH}) → ${env.ENV_LABEL}"
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
                sh 'chmod +x mvnw'
            }
        }

        stage('Testes Java') {
            steps {
                sh './mvnw test -B'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Testes Python') {
            steps {
                dir('extraction-service') {
                    sh '''
                        set -e
                        python3 -m venv .venv-ci
                        . .venv-ci/bin/activate
                        pip install -q -r requirements.txt -r requirements-dev.txt
                        pytest
                    '''
                }
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean package -DskipTests -B'
            }
        }

        stage('Deploy') {
            when {
                expression { env.PROFILE != '' }
            }
            steps {
                script {
                    def jarPath = "target/${env.JAR_NAME}"
                    if (!fileExists(jarPath)) {
                        error "JAR não encontrado: ${jarPath}"
                    }
                    sh """
                        set -e
                        mkdir -p ${env.DEPLOY_DIR}
                        cp ${jarPath} ${env.DEPLOY_DIR}/divergia.jar

                        # Extração é uma instância só, compartilhada entre prod e hml —
                        # atualiza sempre (idempotente), o restart do systemd é que
                        # efetivamente aplica a versão nova.
                        mkdir -p ${EXTRACAO_DIR}
                        rsync -a --delete extraction-service/app/ ${EXTRACAO_DIR}/app/
                        cp extraction-service/requirements.txt ${EXTRACAO_DIR}/requirements.txt

                        ssh -i ${SSH_KEY} ${SSH_OPTS} ${DEPLOY_USER}@${DEPLOY_HOST} \\
                            "sudo systemctl restart ${env.SERVICE_NAME} && sudo systemctl restart divergia-extraction"
                    """
                }
            }
        }

        stage('Health Check') {
            when {
                expression { env.PROFILE != '' }
            }
            steps {
                script {
                    sleep 10
                    def status = sh(
                        script: """
                            ssh -i ${SSH_KEY} ${SSH_OPTS} ${DEPLOY_USER}@${DEPLOY_HOST} \\
                                "sudo systemctl is-active ${env.SERVICE_NAME} && sudo systemctl is-active divergia-extraction"
                        """,
                        returnStdout: true
                    ).trim()
                    if (!status.readLines().every { it == 'active' }) {
                        error "Serviço(s) não ativo(s): ${status}"
                    }
                }
                echo "Serviços ${env.SERVICE_NAME} e divergia-extraction estão ativos."
            }
        }
    }

    post {
        success {
            echo "DivergIA backend [${env.ENV_LABEL}] concluído com sucesso."
        }
        failure {
            echo "DivergIA backend [${env.ENV_LABEL}] falhou. Verifique os logs."
        }
        always {
            cleanWs(deleteDirs: true, notFailBuild: true)
        }
    }
}
