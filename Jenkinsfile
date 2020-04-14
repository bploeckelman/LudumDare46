#!groovy
import groovy.json.JsonOutput

pipeline {
    agent any

    triggers {
        pollSCM('H/15 * * * *') //polling for changes, here every 15 min
    }

    stages {
        stage("Build") {
            steps {
                script {
                    env.GIT_COMMIT_MSG = sh (script: 'git log -1 --pretty=%B ${GIT_COMMIT}', returnStdout: true).trim()
                    env.GIT_REPO_NAME = env.GIT_URL.replaceFirst(/^.*\/([^\/]+?).git$/, '$1')
                    env.REMOTE_DIR =  "inthelifeofdoug.com/LudumDareBuilds/${env.GIT_REPO_NAME}/${env.BRANCH_NAME}/${BUILD_NUMBER}"
                    mqttNotification brokerUrl: 'tcp://home.inthelifeofdoug.com:1883',
                            credentialsId: 'mqttcreds',
                            message: getBeginMessage(),
                            qos: '2',
                            topic: "jenkins/${env.GIT_REPO_NAME}"

                    sh './gradlew clean'
                    sh './gradlew desktop:sprites'
                    sh './gradlew html:dist'
                }
            }
        }
        stage("UploadSSH") {
            steps{
                script {
                    sshPublisher(
                            publishers: [
                                    sshPublisherDesc(
                                            configName: "wxpick",
                                            verbose: true,
                                            transfers: [
                                                    sshTransfer(
                                                            sourceFiles: "html/build/dist/**",
                                                            removePrefix: "html/build/dist/",
                                                            remoteDirectory: "${env.REMOTE_DIR}",
                                                    )
                                            ])
                            ])
                }
            }
        }

    }

    post{
        always {
            mqttNotification brokerUrl: 'tcp://home.inthelifeofdoug.com:1883',
                    credentialsId: 'mqttcreds',
                    message: getMessage(),
                    qos: '2',
                    topic: "jenkins/${env.GIT_REPO_NAME}"
        }
    }


}

def getBeginMessage() {
    def message = [
            buildnumber: "${BUILD_NUMBER}",
            status: "Starting",
            title: "${env.GIT_REPO_NAME}",
            project: "${currentBuild.projectName}",
            duration: "${currentBuild.durationString}",
            commitmessage: "${env.GIT_COMMIT_MSG}"
    ]
    return JsonOutput.toJson(message)

}

def getMessage() {
    def message = [
            buildnumber: "${BUILD_NUMBER}",
            status: "${currentBuild.currentResult}",
            title: "${env.GIT_REPO_NAME}",
            project: "${currentBuild.projectName}",
            duration: "${currentBuild.durationString}",
            commitmessage: "${env.GIT_COMMIT_MSG}"
    ]
    if (currentBuild.resultIsBetterOrEqualTo("SUCCESS")) {
        message.link = "http://${env.REMOTE_DIR}"
    }

    return JsonOutput.toJson(message)
}