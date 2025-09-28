def call() {
    pipeline {
        agent { label 'agent1' }

        stages {
            stage('Checkout') {
                when {
                    expression {
                        // Chỉ chạy nếu branch là "local"
                        def currentBranch = env.GIT_BRANCH?.replaceFirst(/^origin\//, '') ?: sh(
                            script: 'git rev-parse --abbrev-ref HEAD',
                            returnStdout: true
                        ).trim()
                        echo "[INFO] Current branch: ${currentBranch}"
                        return currentBranch == 'local'
                    }
                }
                steps {
                    checkout scm
                }
            }

            stage('Get Latest Tag') {
                when {
                    expression { env.GIT_BRANCH?.endsWith('local') }
                }
                steps {
                    script {
                        sh 'git fetch --tags'

                        def lastTag = sh(
                            script: 'git describe --tags `git rev-list --tags --max-count=1` || echo 0.0.0',
                            returnStdout: true
                        ).trim()

                        def (major, minor, patch) = lastTag.tokenize('.')*.toInteger()
                        def newTag = "${major}.${minor}.${patch + 1}"

                        echo "Last tag: ${lastTag}"
                        echo "New tag: ${newTag}"

                        env.IMAGE_TAG = newTag
                    }
                }
            }

            stage('Build & Push Docker Image') {
                when {
                    expression { env.GIT_BRANCH?.endsWith('local') }
                }
                steps {
                    script {
                        withCredentials([usernamePassword(
                            credentialsId: Constants.DOCKERHUB_CREDENTIALS,
                            usernameVariable: 'DOCKERHUB_USER',
                            passwordVariable: 'DOCKERHUB_PASS'
                        )]) {
                            sh """
                                echo "[INFO] Docker login..."
                                echo ${DOCKERHUB_PASS} | docker login -u ${DOCKERHUB_USER} --password-stdin

                                echo "[INFO] Building image ${Constants.DOCKER_IMAGE}:${env.IMAGE_TAG}"
                                docker build -t ${Constants.DOCKER_IMAGE}:${env.IMAGE_TAG} -f DockerBuild/Dockerfile .

                                echo "[INFO] Pushing image ${Constants.DOCKER_IMAGE}:${env.IMAGE_TAG}"
                                docker push ${Constants.DOCKER_IMAGE}:${env.IMAGE_TAG}
                            """
                        }
                    }
                }
            }

            stage('Update Git Tag') {
                when {
                    expression { env.GIT_BRANCH?.endsWith('local') }
                }
                steps {
                    script {
                        sh """
                            git config user.email 'tienkbtnhp@gmail.com'
                            git config user.name 'TienHunter'
                            git tag ${env.IMAGE_TAG}
                            git push origin ${env.IMAGE_TAG}
                        """
                    }
                }
            }
        }
    }
}
