package org.ethelred.cgi.servlet

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import spock.lang.Specification

class TestRequestWrapper extends Specification {
    def 'no request params'() {
        given:
        def cgiRequest = new StubCgiRequest(env: [REQUEST_METHOD: "GET"])

        when:
        def servletRequest = new RequestWrapper(cgiRequest, null)

        then:
        servletRequest.parameterMap == [:]
    }

    def 'simple param get'() {
        given:
        def cgiRequest = new StubCgiRequest(env: [REQUEST_METHOD: "GET", QUERY_STRING: "foo=bar"])

        when:
        def servletRequest = new RequestWrapper(cgiRequest, null)

        then:
        servletRequest.getParameter("foo") == "bar"
        servletRequest.getParameterValues("foo") == ["bar"] as String[]
    }

    def 'multivalued param get'() {
        given:
        def cgiRequest = new StubCgiRequest(env: [REQUEST_METHOD: "GET", QUERY_STRING: "foo=bar&foo=baz"])

        when:
        def servletRequest = new RequestWrapper(cgiRequest, null)

        then:
        servletRequest.getParameter("foo") == "bar"
        servletRequest.getParameterValues("foo") == ["bar", "baz"] as String[]
    }

    def 'more params get'() {
        given:
        def cgiRequest = new StubCgiRequest(env: [REQUEST_METHOD: "GET", QUERY_STRING: "foo=bar&fruit=coconut&foo=baz"])

        when:
        def servletRequest = new RequestWrapper(cgiRequest, null)

        then:
        servletRequest.getParameter("foo") == "bar"
        servletRequest.getParameterValues("foo") == ["bar", "baz"] as String[]
        servletRequest.parameterMap == ["foo":["bar", "baz"] as String[], "fruit":["coconut"] as String[]]
    }

    def 'simple param post'() {
        given:
        def cgiRequest = new StubCgiRequest(env: [REQUEST_METHOD: "POST", CONTENT_TYPE: HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED as String], bodyValue: "foo=bar")

        when:
        def servletRequest = new RequestWrapper(cgiRequest, null)

        then:
        servletRequest.getParameter("foo") == "bar"
        servletRequest.getParameterValues("foo") == ["bar"] as String[]
    }

    def 'mix query and body'() {
        given:
        def cgiRequest = new StubCgiRequest(env: [REQUEST_METHOD: "POST", CONTENT_TYPE: HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED as String, QUERY_STRING: "foo=bar"], bodyValue: "fruit=coconut&foo=baz")

        when:
        def servletRequest = new RequestWrapper(cgiRequest, null)

        then:
        servletRequest.getParameter("foo") == "bar"
        servletRequest.getParameterValues("foo") == ["bar", "baz"] as String[]
        servletRequest.parameterMap == ["foo":["bar", "baz"] as String[], "fruit":["coconut"] as String[]]
    }
}