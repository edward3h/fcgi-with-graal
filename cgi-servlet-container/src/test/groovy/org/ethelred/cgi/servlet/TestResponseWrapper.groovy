package org.ethelred.cgi.servlet

import spock.lang.Specification

class TestResponseWrapper extends Specification {
    def 'default charset'() {
        when:
        def response = new ResponseWrapper(null, null)

        then:
        response.characterEncoding == 'ISO-8859-1'
        response.contentType == null
    }

    def 'default charset with content type'() {
        given:
        def response = new ResponseWrapper(null, null)

        when:
        response.contentType = 'text/html'

        then:
        response.characterEncoding == 'ISO-8859-1'
        response.contentType == 'text/html; charset=ISO-8859-1'
    }

    def 'charset via content type'() {
        given:
        def response = new ResponseWrapper(null, null)

        when:
        response.contentType = 'text/html; charset=utf-8'

        then:
        response.characterEncoding == 'UTF-8'
        response.contentType == 'text/html; charset=UTF-8'
    }

    // ibm437
    def 'content type then character encoding'() {
        given:
        def response = new ResponseWrapper(null, null)

        when:
        response.contentType = 'text/html; charset=utf-8'
        response.characterEncoding = 'ibm437'

        then:
        response.characterEncoding == 'IBM437'
        response.contentType == 'text/html; charset=IBM437'
    }
}
