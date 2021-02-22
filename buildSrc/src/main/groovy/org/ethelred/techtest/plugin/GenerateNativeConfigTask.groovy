package org.ethelred.techtest.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

/**
 * TODO
 *
 * @author eharman* @since 2021-02-06
 */
class GenerateNativeConfigTask extends DefaultTask {
    def extension

    @Inject
    GenerateNativeConfigTask(extension) {
        this.extension = extension
    }

    @TaskAction
    def perform() {
        def envMap = extension.generateNativeConfigEnvironment.get()
        def thedir = project.rootProject.projectDir
        def ghome = project.gradle.gradleUserHomeDir
        def cp = project.sourceSets.main.runtimeClasspath
        def outdir = "${project.projectDir}/src/main/resources/META-INF/native-image"
        project.mkdir outdir
        project.exec {
            environment = envMap
            executable "docker"
            args  "run",
                "--volume", "${thedir}:${thedir}", "--workdir", project.projectDir,
                "--volume", "${ghome}:${ghome}:ro"
            args envMap.collect { k, v -> ["--env", "$k=$v"]}.flatten()
                // "--memory", "4g", "--oom-kill-disable",
            args extension.dockerImageName.get(),
                "/root/.sdkman/candidates/java/current/bin/java", "-agentlib:native-image-agent=config-output-dir=$outdir",
                "-cp", cp.asPath,
                extension.mainClassName.get()
        }
    }
}
