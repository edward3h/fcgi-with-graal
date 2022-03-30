package org.ethelred.techtest

import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Specification

/**
 * TODO
 *
 * @author eharman* @since 2021-02-07
 */
@Testcontainers
class TestHelloWorld extends Specification {

    GenericContainer container = new GenericContainer<>("ghcr.io/edward3h/fake_dh:v0.1")
    .withFileSystemBind("${System.getProperty("buildDir")}/deploy", "/app")

    def 'running executable prints hello world'() {
        given:
        def executable = System.getProperty('appName')

        when:
        def command = "/app/$executable"
        def result = container.execInContainer(command).stdout

        then:
        result == "Hello World!\n"
    }
}
