def call() {
   // def utilitiesBuild = new Utilities(this)
   pipeline {
      agent any

      stages {
         stage('Checkout') {
            steps {
               checkout scm
            }
         }

         stage('Get Latest Tag') {
            steps {
               script {
                  // Lấy tag mới nhất từ repo
                  sh 'git fetch --tags'
                  def lastTag = sh(script: 'git describe --tags `git rev-list --tags --max-count=1` || echo 0.0.0', returnStdout: true).trim()

                  // Tăng version (ví dụ tăng patch)
                  def (major, minor, patch) = lastTag.tokenize('.')
                  def newTag = "${major}.${minor}.${(patch as int) + 1}"

                  echo "Last tag: ${lastTag}"
                  echo "New tag: ${newTag}"
                  env.IMAGE_TAG = newTag
               }
            }
         }

         stage('Build Docker Image') {
            steps {
               script {
                  withCredentials([usernamePassword(credentialsId: Constants.DOCKERHUB_CREDENTIALS, usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
                     sh """
                        docker login -u ${DOCKERHUB_USER} -p ${DOCKERHUB_PASS}
                        docker build -t ${DOCKER_IMAGE}:${env.IMAGE_TAG} .
                        docker push ${Constants.DOCKER_IMAGE_fe}:${env.IMAGE_TAG}
                     """
                  }
               }
            }
         }

         stage('Update Git Tag') {
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