package org.ethelred.techtest

import spock.lang.Specification

/**
 * TODO
 *
 * @author eharman* @since 2021-02-07
 */
class TestHelloWorld extends Specification {

    def 'running executable on deploy host prints hello world'() {
        given:
        def host = System.getProperty('host')
        def executable = System.getProperty('appName')

        when:
        def command = "ssh $host ./$executable"
        def result = command.execute().in.text

        then:
        result == "Hello World!\n"
    }
}
