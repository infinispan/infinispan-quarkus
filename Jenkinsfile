#!/usr/bin/env groovy

pipeline {
    agent {
        label 'slave-group-graalvm'
    }

    stages {
        stage('Prepare') {
            steps {
                script {
                    env.MAVEN_HOME = tool('Maven')
                    env.MAVEN_OPTS = '-Xmx1g -XX:+HeapDumpOnOutOfMemoryError'
                    env.JAVA_HOME = tool('GraalVM 20')
                }
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                // First we compile Infinispan SNAPSHOT
                sh 'git clone --single-branch --branch 14.0.x --depth 1 https://github.com/infinispan/infinispan.git'
                dir('infinispan') {
                    sh '$MAVEN_HOME/bin/mvn clean install -DskipTests'
                    deleteDir()
                }

                // Then we build infinispan-quarkus
                sh '$MAVEN_HOME/bin/mvn clean install -B -V -e -DskipTests'
            }
        }

        stage('Image') {
            when {
                expression {
                    return !env.BRANCH_NAME.startsWith("PR-") || pullRequest.labels.contains('Image Required')
                }
            }
            steps {
                script {
                    def mvnCmd = '-q -Dexec.executable=echo -Dexec.args=\'${project.version}\' --non-recursive exec:exec'
                    def SERVER_VERSION = sh(
                            script: "${MAVEN_HOME}/bin/mvn ${mvnCmd}",
                            returnStdout: true
                    ).trim()
                    def REPO = 'quay.io/infinispan-test/server-native'
                    def TAG = env.BRANCH_NAME
                    def IMAGE_BRANCH = env.CHANGE_ID ? pullRequest.base : env.BRANCH_NAME

                    sh "rm -rf infinispan-images"
                    sh "git clone --single-branch --branch ${IMAGE_BRANCH} --depth 1 https://github.com/infinispan/infinispan-images.git"


                    dir('infinispan-images') {
                        sh "cekit -v --descriptor server-dev-native.yaml build --overrides '{\"name\":\"${REPO}\", \"version\":\"${TAG}\"}' --overrides '{\"artifacts\":[{\"name\":\"server\",\"path\":\"../server-runner/target/infinispan-quarkus-server-runner-${SERVER_VERSION}-runner\"},{\"name\":\"cli\",\"path\":\"../cli/target/infinispan-cli\"}]}' docker\n"

                        withDockerRegistry(credentialsId: 'Quay-InfinispanTest', url: 'https://quay.io') {
                            sh "docker push ${REPO}:${TAG}"
                        }
                        sh "docker rmi ${REPO}:${TAG}"
                        deleteDir()
                    }

                    // CHANGE_ID is set only for pull requests, so it is safe to access the pullRequest global variable
                    if (env.CHANGE_ID) {
                        pullRequest.comment("Image pushed for Jenkins build [#${env.BUILD_NUMBER}](${env.BUILD_URL}):\n```\n${REPO}:${TAG}\n```")
                    }
                }
            }
        }

        stage('Tests') {
            steps {
                sh '$MAVEN_HOME/bin/mvn verify -B -V -e -Dmaven.test.failure.ignore=true -Dansi.strip=true'

                // TODO Add StabilityTestDataPublisher after https://issues.jenkins-ci.org/browse/JENKINS-42610 is fixed
                // Capture target/surefire-reports/*.xml, target/failsafe-reports/*.xml,
                // target/failsafe-reports-embedded/*.xml, target/failsafe-reports-remote/*.xml
                junit testResults: '**/target/*-reports*/**/TEST-*.xml',
                        testDataPublishers: [[$class: 'ClaimTestDataPublisher']],
                        healthScaleFactor: 100, allowEmptyResults: true

                // Workaround for SUREFIRE-1426: Fail the build if there a fork crashed
                script {
                    if (manager.logContains('org.apache.maven.surefire.booter.SurefireBooterForkException:.*')) {
                        echo 'Fork error found'
                        manager.buildFailure()
                    }
                }

                // Dump any dump files to the console
                sh 'find . -name "*.dump*" -exec echo {} \\; -exec cat {} \\;'
                sh 'find . -name "hs_err_*" -exec echo {} \\; -exec grep "^# " {} \\;'
            }
        }
    }

    post {
        always {
            sh 'git clean -ffd -e "*.hprof" || echo "git clean failed, exit code $?"'
        }

        failure {
            echo 'post build status: failure'
            emailext to: '${DEFAULT_RECIPIENTS}', subject: '${DEFAULT_SUBJECT}', body: '${DEFAULT_CONTENT}'
            sh 'docker kill $(docker ps -q) || true'
        }

        success {
            echo 'post build status: success'
            emailext to: '${DEFAULT_RECIPIENTS}', subject: '${DEFAULT_SUBJECT}', body: '${DEFAULT_CONTENT}'
        }

        cleanup {
            // Remove all created SNAPSHOT artifacts to ensure a clean build on every run
            sh 'find ~/.m2/repository -type d -name "*-SNAPSHOT" -prune -exec rm -rf {} \\;'
        }
    }
}
