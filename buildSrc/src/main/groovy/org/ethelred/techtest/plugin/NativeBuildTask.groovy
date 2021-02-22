package org.ethelred.techtest.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

/**
 * TODO
 *
 * @author eharman* @since 2021-02-06
 */
class NativeBuildTask extends DefaultTask {
    def extension

    @Inject
    NativeBuildTask(extension) {
        this.extension = extension
    }

    @TaskAction
    def perform() {
        def thedir = project.rootProject.projectDir
        def ghome = project.gradle.gradleUserHomeDir
        def cp = project.sourceSets.main.runtimeClasspath
        project.exec {
            commandLine "docker", "run",
            "--volume", "${thedir}:${thedir}", "--workdir", project.projectDir,
            "--volume", "${ghome}:${ghome}:ro",
            // "--memory", "4g", "--oom-kill-disable",
            extension.dockerImageName.get(),
            "/root/.sdkman/candidates/java/current/bin/native-image", "--no-fallback",
            "--allow-incomplete-classpath",
            "--static",
            '--report-unsupported-elements-at-runtime',
            '--verbose',
            '-J-Xmx4G',"-R:MinHeapSize=2m", "-R:MaxHeapSize=10m", "-R:MaxNewSize=1m",
//            "-H:+PrintImageObjectTree",
            "-H:Log=registerResource",
            "-H:+TraceClassInitialization",
            "-H:IncludeResources=.*logback.xml",
            "--initialize-at-build-time=org.ethelred",
                    "--initialize-at-build-time=ch.qos.logback,org.slf4j",
//                    "-H:+ReportExceptionStackTraces",
            "-cp", cp.asPath,
            extension.mainClassName.get(),
            "${project.buildDir}/deploy/${extension.appName.get()}"
        }
    }
}
