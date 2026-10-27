pipeline {
    agent any
    environment {
        IMAGE = 'campus-equipment-booking:set-b'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log --oneline -n 5'
            }
        }
        stage('Build & Test') {
            steps {
                sh 'mvn -B clean test'
            }
            post {
                always { junit 'target/surefire-reports/*.xml' }
            }
        }
        stage('Package') {
            steps {
                sh 'mvn -B package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        stage('Docker Build') {
            steps {
                sh 'docker build -t $IMAGE .'
            }
        }
        stage('Container Health Check') {
            steps {
                sh '''
                  docker rm -f booking-ci || true
                  docker run -d --name booking-ci -p 8090:8080 $IMAGE
                  sleep 15
                  curl -f http://localhost:8090/actuator/health
                '''
            }
            post {
                always { sh 'docker rm -f booking-ci || true' }
            }
        }
        stage('Deploy to Minikube') {
            steps {
                sh '''
                  kubectl apply -f k8s/
                  kubectl rollout status deployment/campus-equipment-booking --timeout=120s
                  kubectl get deployments,pods,svc -l app=campus-equipment-booking
                '''
            }
        }
    }
}
