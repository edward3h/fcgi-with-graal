package org.ethelred.cgi.micronaut

import io.micronaut.context.DefaultApplicationContext
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import org.ethelred.cgi.CgiHandler
import org.ethelred.cgi.CgiParam
import org.ethelred.cgi.TestCgiRequest
import spock.lang.Specification

class TestMicronautCgiHandler extends Specification {

    void "cgiHandler is built"() {
        given:
        CgiHandler cgiHandler = new DefaultApplicationContext("test")
            .start()
            .getBean(CgiHandler)

        expect:
        cgiHandler instanceof MicronautCgiHandler
    }

    void "get hello endpoint"() {
        given:
        def cgiHandler = new DefaultApplicationContext("test")
                .start()
                .getBean(CgiHandler)
        def cgiRequest = new TestCgiRequest(REQUEST_URI: "/hello")

        when:
        cgiHandler.handleRequest(cgiRequest)

        then:
        def response = cgiRequest.outputAsString
        response.contains("Status: 200")
        response.contains("Content-Type: text/plain")
        response.contains("Hello World")
    }

    void "post hello endpoint"() {
        given:
        def cgiHandler = new DefaultApplicationContext("test")
                .start()
                .getBean(CgiHandler)
        def cgiRequest = new TestCgiRequest(REQUEST_URI: "/hello", REQUEST_METHOD: "POST")

        when:
        cgiHandler.handleRequest(cgiRequest)

        then:
        def response = cgiRequest.outputAsString
        response.contains("Status: 405")
        response.contains("Method Not Allowed")
    }

    @Controller
    static class HelloController {
        @Get('/hello')
        @Produces(MediaType.TEXT_PLAIN)
        String hello() {
            return "Hello World"
        }
    }
}
