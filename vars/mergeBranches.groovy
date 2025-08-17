def call(String fromBranch, String toBranch) {
    def utilitiesBuild = new Utilities(this)
    pipeline {
        agent { label 'agent1' }
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
                }
            }
        }
    }
}
