def call(String fromBranch, String toBranch) {
    def utilitiesBuild = new src.Utilities(this)
    pipeline {
        agent any
        stages {
            stage('Setup Repo') {
                steps {
                    script {
                        sh 'mkdir -p fe be'
                    }
                }
            }
            stage('Git Merge') {
                parallel {
                    stage('FE') {
                        steps {
                            script {
                                dir('fe') {
                                    stage('Checkout') {
                                        git branch: fromBranch, url: env.REPO_URL, credentialsId: env.GIT_CREDENTIALS
                                    }
                                    stage('Merge') {
                                        withCredentials([gitUsernamePassword(credentialsId: env.GIT_CREDENTIALS, gitToolName: 'git-tool')]) {
                                            utilitiesBuild.mergeCode(fromBranch, toBranch)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    stage('BE') {
                        steps {
                            script {
                                dir('be') {
                                    stage('Checkout') {
                                        git branch: fromBranch, url: env.REPO_URL, credentialsId: env.GIT_CREDENTIALS
                                    }
                                    stage('Merge') {
                                        withCredentials([gitUsernamePassword(credentialsId: env.GIT_CREDENTIALS, gitToolName: 'git-tool')]) {
                                            utilitiesBuild.mergeCode(fromBranch, toBranch)
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
