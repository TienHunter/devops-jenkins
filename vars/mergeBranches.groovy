def call() {
    def utilitiesBuild = new Utilities(this)
    echo "[CONFIG] REPO_URL: ${Constants.REPO_URL}"
    echo "[CONFIG] SOURCE_BRANCH: ${Constants.SOURCE_BRANCH}"
    echo "[CONFIG] TARGET_BRANCH: ${Constants.TARGET_BRANCH}"
    echo "[CONFIG] GIT_CREDENTIALS: ${Constants.GIT_CREDENTIALS}"
    pipeline {
        agent { label 'agent1' }
        stages {
            stage('Setup Repo') {
                steps {
                    sh 'mkdir -p fe be'
                }
            }
            stage('Git Merge') {
                parallel {
                    stage('FE') {
                        stages {
                            stage('Checkout') {
                                steps {
                                    dir('fe') {
                                        git branch: Constants.SOURCE_BRANCH, url: Constants.REPO_URL, credentialsId: Constants.GIT_CREDENTIALS
                                    }
                                }
                            }
                            stage('Merge') {
                                steps {
                                    dir('fe') {
                                        script {
                                            echo "[FE] Bắt đầu merge code từ ${Constants.SOURCE_BRANCH} sang ${Constants.TARGET_BRANCH}"
                                            withCredentials([usernamePassword(credentialsId: Constants.GIT_CREDENTIALS, usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
                                                utilitiesBuild.mergeCode(Constants.SOURCE_BRANCH, Constants.TARGET_BRANCH)
                                            }
                                            echo "[FE] Đã hoàn thành merge code."
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}