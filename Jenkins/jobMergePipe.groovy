pipeline {
    agent any
    stages {
        stage('Load environment') {
            steps {
                script {
                    def props = readProperties file: 'Jenkins/project/env.properties'
                    env.REPO_URL = props.REPO_URL
                    env.SOURCE_BRANCH = props.SOURCE_BRANCH
                    env.TARGET_BRANCH = props.TARGET_BRANCH
                    env.GIT_CREDENTIALS = props.GIT_CREDENTIALS
                }
            }
        }
        stage('Prepare folders') {
            steps {
                sh 'mkdir -p fe be'
            }
        }
        stage('FE: Clone, merge, push') {
            steps {
                dir('fe') {
                    echo "[FE] Cloning ${env.SOURCE_BRANCH} from ${env.REPO_URL}"
                    git branch: "${env.SOURCE_BRANCH}", url: "${env.REPO_URL}", credentialsId: "${env.GIT_CREDENTIALS}"
                    echo "[FE] Merging ${env.SOURCE_BRANCH} into ${env.TARGET_BRANCH} và đẩy lên repo"
                    sh '''
                        git config user.email "tienkbtnhp@gmail.com"
                        git config user.name "TienHunter"
                        git checkout ${env.TARGET_BRANCH}
                        git merge origin/${env.SOURCE_BRANCH}
                        git push origin ${env.TARGET_BRANCH}
                    '''
                }
            }
        }
        stage('BE: Clone, merge, push') {
            steps {
                dir('be') {
                    echo "[BE] Cloning ${env.SOURCE_BRANCH} from ${env.REPO_URL}"
                    git branch: "${env.SOURCE_BRANCH}", url: "${env.REPO_URL}", credentialsId: "${env.GIT_CREDENTIALS}"
                    echo "[BE] Merging ${env.SOURCE_BRANCH} into ${env.TARGET_BRANCH} và đẩy lên repo"
                    sh '''
                        git config user.email "tienkbtnhp@gmail.com"
                        git config user.name "TienHunter"
                        git checkout ${env.TARGET_BRANCH}
                        git merge origin/${env.SOURCE_BRANCH}
                        git push origin ${env.TARGET_BRANCH}
                    '''
                }
            }
        }
    }
}
