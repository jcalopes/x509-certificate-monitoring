pipeline {
    agent any

    tools {
        jdk 'JDK 11'
    }

    stages {

        stage('Build') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build -x test'
            }
            post {
                success {
                    archiveArtifacts artifacts: "build/libs/*.jar", fingerprint: true
                }
            }
        }

        stage('Run') {
            environment {
                BITBUCKET_CRED = credentials('bitbucket_credentials')
                KEEPASS_CRED = credentials('keepass_credentials')
                JIRA_CRED = credentials('jira_token')
                CONFLUENCE_CRED = credentials('confluence_token')
                AWS_CREDENTIALS = credentials('aws_acm_cma')
            }
            steps {
                sh 'docker build --build-arg CONFIG_NAME=mupi-project -t cert-app:lts .'
                sh 'docker run \
                    -e CMA_CRAWLER_BITBUCKET_USER=$BITBUCKET_CRED_USR \
                    -e CMA_CRAWLER_BITBUCKET_TOKEN=$BITBUCKET_CRED_PSW \
                    -e CMA_CRAWLER_BITBUCKET_CREDENTIALS_PASS=$KEEPASS_CRED \
                    -e CMA_NOTIFIER_JIRA_TOKEN=$JIRA_CRED \
                    -e CMA_NOTIFIER_CONFLUENCE_TOKEN=$CONFLUENCE_CRED \
                    -e CMA_EXTRACTOR_AWS_ACCESSKEY=$AWS_CREDENTIALS_USR \
                    -e CMA_EXTRACTOR_AWS_SECRETKEY=$AWS_CREDENTIALS_PSW \
                --name cert-container cert-app:lts'
                sh 'docker cp cert-container:certificatesOverview.csv .'
            }
           post {
               always {
                   script {
                        sh 'docker rm cert-container'
                        sh 'docker rmi cert-app:lts'
                   }
               }
               success {
                    archiveArtifacts artifacts: "certificatesOverview.csv", fingerprint: true, allowEmptyArchive: true
               }
           }
        }
    }

    post{
        always{
            script {
                emailext attachmentsPattern: 'certificatesOverview.csv',
                         body: 'Attention: Some certificates have expired or will expire soon. Report has been generated with further information. ${SCRIPT, template="groovy-html.template"}',
                         to: 'Joao.Carlos.Lopes@criticaltechworks.com',
                         subject: "[Jenkins] ${currentBuild.fullDisplayName}",
                         mimeType: 'text/html'
            }
            cleanWs()
        }
    }
}