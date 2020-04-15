#!groovy
import groovy.json.JsonOutput
import hudson.Util;

pipeline {
    agent any

    triggers {
        pollSCM('H/15 * * * *') //polling for changes, here every 15 min
    }

    stages {
        stage("Setup") {
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
                }
            }
        }
        stage("Build Sprites") {
            steps{
                sh './gradlew clean'
                sh './gradlew desktop:sprites'
            }
        }
        stage("Build Desktop") {
            steps {
                script {
                    sh './gradlew desktop:dist'
                }
            }
        }
        stage("Build HTML") {
            steps {
                script {
                    sh './gradlew html:dist'
                }
            }
        }
        stage("Upload to Host") {
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

def getMessageAttrib() {
    def changes = []
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            def files = new ArrayList(entry.affectedFiles)
            echo "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}"
            def commit = [commitId: "${entry.commitId}", author: "${entry.author}", message: "${entry.msg}", fileCount: "${files.size()}"]
            commit.files = [];
            for (int k = 0; k < files.size(); k++) {
                def file = files[k]
                echo "  ${file.editType.name} ${file.path}"
                commit.files << "${file.editType.name} ${file.path}"
            }
            changes << commit
        }
    }

    def message = [
            buildnumber: "${BUILD_NUMBER}",
            status: "${currentBuild.currentResult}",
            title: "${env.GIT_REPO_NAME}",
            project: "${currentBuild.projectName}",
            duration: "${Util.getTimeSpanString(System.currentTimeMillis() - currentBuild.startTimeInMillis)}",
            commitmessage: "${env.GIT_COMMIT_MSG}",
            buildURL: "${env.BUILD_URL}",
            changesets: changes
    ]

    return message
}

def getBeginMessage() {
    def message = getMessageAttrib()
    message.status = "STARTING"
    return JsonOutput.prettyPrint(JsonOutput.toJson(message))

}

def getMessage() {
    def message = getMessageAttrib()
    if (currentBuild.resultIsBetterOrEqualTo("SUCCESS")) {
        message.link = "http://${env.REMOTE_DIR}"
    }
    return JsonOutput.prettyPrint(JsonOutput.toJson(message))
}