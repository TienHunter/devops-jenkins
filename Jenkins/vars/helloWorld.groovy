def call() {
    pipeline {
        agent { label 'agent1' }
        stages {
            stage('Example Stage') {
                steps {
                    script {
                        echo 'Hello, World!'
                    }
                }
            }
        }
    }
}
