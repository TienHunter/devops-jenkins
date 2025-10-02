def call() {
    pipeline {
        agent { label 'agent1' }

        stages {
            stage('Checkout') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: Constants.GIT_CREDENTIALS,
                        usernameVariable: 'GIT_USER',
                        passwordVariable: 'GIT_PASS'
                    )]) {
                        sh '''
                            echo "[INFO] GitHub login..."
                            git config --global credential.helper store
                            git config --global user.email "tienkbtnhp@gmail.com"
                            git config --global user.name "TienHunter"
                            if [ ! -d .git ]; then
                              git clone https://$GIT_USER:$GIT_PASS@github.com/TienHunter/devops-jenkins.git .
                            else
                              git fetch origin
                              git checkout local || git checkout -b local origin/local
                              git pull origin local
                        '''
                    }
                }
            }

            stage('Get Latest Tag') {
                steps {
                    script {
                        sh 'git fetch --tags'

                        def lastTag = sh(
                            script: 'git describe --tags `git rev-list --tags --max-count=1` || echo 0.0.0',
                            returnStdout: true
                        ).trim()

                        def tagParts = lastTag.tokenize('.')
                        def major = tagParts.size() > 0 ? tagParts[0].toInteger() : 0
                        def minor = tagParts.size() > 1 ? tagParts[1].toInteger() : 0
                        def patch = tagParts.size() > 2 ? tagParts[2].toInteger() : 0
                        def newTag = "${major}.${minor}.${patch + 1}"

                        echo "Last tag: ${lastTag}"
                        echo "New tag: ${newTag}"

                        env.IMAGE_TAG = newTag
                    }
                }
            }

            stage('Build & Push Docker Image') {
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

                                echo "[INFO] Building image ${Constants.DOCKER_IMAGE_FE}:${env.IMAGE_TAG}"
                                docker build -t ${Constants.DOCKER_IMAGE_FE}:${env.IMAGE_TAG} -f DockerBuild/Dockerfile .

                                echo "[INFO] Pushing image ${Constants.DOCKER_IMAGE_FE}:${env.IMAGE_TAG}"
                                docker push ${Constants.DOCKER_IMAGE_FE}:${env.IMAGE_TAG}
                            """
                        }
                    }
                }
            }

            stage('Update Git Tag') {
                steps {
                     withCredentials([usernamePassword(
                        credentialsId: Constants.GIT_CREDENTIALS,
                        usernameVariable: 'GIT_USER',
                        passwordVariable: 'GIT_PASS'
                    )]) {
                        sh '''
                            git config user.email 'tienkbtnhp@gmail.com'
                            git config user.name 'TienHunter'
                            git tag ${env.IMAGE_TAG}
                            git push origin ${env.IMAGE_TAG}
                        '''
                    }
                }
            }
        }
    }
}
