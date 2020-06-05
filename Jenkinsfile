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
                sh 'git clone --single-branch --branch master --depth 1 https://github.com/infinispan/infinispan.git'
                dir('infinispan') {
                    sh '$MAVEN_HOME/bin/mvn clean install -DskipTests'
                    deleteDir()
                }

                // Then we build infinispan-quarkus
                sh '$MAVEN_HOME/bin/mvn clean install -Dnative -B -V -e -DskipTests'
            }
        }

        stage('Tests') {
            steps {
                sh '$MAVEN_HOME/bin/mvn verify -Dnative -B -V -e -Dmaven.test.failure.ignore=true -Dansi.strip=true'

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
