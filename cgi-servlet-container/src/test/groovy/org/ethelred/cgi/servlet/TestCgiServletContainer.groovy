package org.ethelred.cgi.servlet

import groovy.transform.Memoized
import org.ethelred.cgi.CgiHandler
import org.ethelred.cgi.CgiRequest
import org.ethelred.cgi.CgiServer
import org.ethelred.cgi.Options
import org.ethelred.cgi.TestCgiRequest
import spock.lang.Specification

import javax.servlet.http.HttpServlet
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class TestCgiServletContainer extends Specification {
    def 'container inits server'() {
        given:
        def server = Mock(CgiServer)

        when:
        def container = new CgiServletContainer(server, Options.empty())

        then:
        1 * server.init(_,_)
    }

    def 'container requires servlet to start'() {
        given:
        def server = Mock(CgiServer)
        def container = new CgiServletContainer(server, Options.empty())

        when:
        container.start()

        then:
        thrown(IllegalStateException)
    }

    def 'container registers a handler'() {
        given:
        def server = Mock(CgiServer)
        def container = new CgiServletContainer(server, Options.empty())
        def servlet = Mock(HttpServlet)
        container.servlet = servlet

        when:
        container.start()

        then:
        1 * servlet.init(!null)
        1 * server.start(!null)
    }

    def 'cgi request results in servlet request'() {
        given:
        def server = new StubCgiServer()
        def container = new CgiServletContainer(server, Options.empty())
        def servlet = Mock(HttpServlet)
        container.servlet = servlet
        def cgiRequest = new TestCgiRequest()
        container.start()

        when:
        server.handler.handleRequest(cgiRequest)

        then:
        1 * servlet.service(_, _)
    }

    class StubCgiServer implements CgiServer {
        CgiHandler handler

        @Override
        void init(Callback callback, Options options) {

        }

        @Override
        void start(CgiHandler handler) {
            this.handler = handler
        }

        @Override
        void shutdown() {
            throw new UnsupportedOperationException("CgiServer.shutdown")
        }

        @Override
        boolean isSingleRequest() {
            throw new UnsupportedOperationException("CgiServer.isSingleRequest")
        }

        @Override
        void waitForCompletion(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException("CgiServer.waitForCompletion")
        }

        @Override
        boolean isRunning() {
            throw new UnsupportedOperationException("CgiServer.isRunning")
        }
    }

}
