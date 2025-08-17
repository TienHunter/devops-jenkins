def call() {
    def utilitiesBuild = new Utilities(this)
    pipeline {
        agent { label 'agent1' }
        stages {
            stage('Load Env') {
                steps {
                    script {
                        def props = readProperties file: 'project/env.properties'
                        env.REPO_URL = props['REPO_URL']
                        env.SOURCE_BRANCH = props['SOURCE_BRANCH']
                        env.TARGET_BRANCH = props['TARGET_BRANCH']
                        env.GIT_CREDENTIALS = props['GIT_CREDENTIALS']
                    }
                }
            }
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
                                        git branch: env.SOURCE_BRANCH, url: env.REPO_URL, credentialsId: env.GIT_CREDENTIALS
                                    }
                                }
                            }
                            stage('Merge') {
                                steps {
                                    script {
                                        utilitiesBuild.mergeCode(env.SOURCE_BRANCH, env.TARGET_BRANCH)
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