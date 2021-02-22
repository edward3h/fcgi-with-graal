package org.ethelred.techtest.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DeployTask extends DefaultTask {
    def extension

    @Inject
    DeployTask(extension) {
        this.extension = extension
    }

    @TaskAction
    def perform() {
        project.exec {
            executable "rsync"
            args "-av"
            args project.fileTree("${project.buildDir}/deploy/").collect()
            args "${extension.remoteHost.get()}:${extension.deployDir.get()}"
        }
    }

}