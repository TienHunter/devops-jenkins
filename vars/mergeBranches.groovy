def call() {
    def utilitiesBuild = new Utilities(this)
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
                                    script {
                                        utilitiesBuild.mergeCode(Constants.SOURCE_BRANCH, Constants.TARGET_BRANCH)
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