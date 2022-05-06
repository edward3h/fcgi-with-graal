package org.ethelred.cgi.servlet

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import spock.lang.Specification
import org.ethelred.cgi.TestCgiRequest

class TestRequestWrapper extends Specification {
    def 'no request params'() {
        given:
        def cgiRequest = new TestCgiRequest(REQUEST_METHOD: "GET")

        when:
        def servletRequest = new RequestWrapper(cgiRequest, null)

        then:
        servletRequest.parameterMap == [:]
    }

    def 'simple param get'() {
        given:
        def cgiRequest = new TestCgiRequest(REQUEST_METHOD: "GET", QUERY_STRING: "foo=bar")

        when:
        def servletRequest = new RequestWrapper(cgiRequest, null)

        then:
        servletRequest.getParameter("foo") == "bar"
        servletRequest.getParameterValues("foo") == ["bar"] as String[]
    }

    def 'multivalued param get'() {
        given:
        def cgiRequest = new TestCgiRequest(REQUEST_METHOD: "GET", QUERY_STRING: "foo=bar&foo=baz")

        when:
        def servletRequest = new RequestWrapper(cgiRequest, null)

        then:
        servletRequest.getParameter("foo") == "bar"
        servletRequest.getParameterValues("foo") == ["bar", "baz"] as String[]
    }

    def 'more params get'() {
        given:
        def cgiRequest = new TestCgiRequest(REQUEST_METHOD: "GET", QUERY_STRING: "foo=bar&fruit=coconut&foo=baz")

        when:
        def servletRequest = new RequestWrapper(cgiRequest, null)

        then:
        servletRequest.getParameter("foo") == "bar"
        servletRequest.getParameterValues("foo") == ["bar", "baz"] as String[]
        servletRequest.parameterMap == ["foo":["bar", "baz"] as String[], "fruit":["coconut"] as String[]]
    }

    def 'simple param post'() {
        given:
        def cgiRequest = new TestCgiRequest(REQUEST_METHOD: "POST", CONTENT_TYPE: HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED as String, "foo=bar")

        when:
        def servletRequest = new RequestWrapper(cgiRequest, null)

        then:
        servletRequest.getParameter("foo") == "bar"
        servletRequest.getParameterValues("foo") == ["bar"] as String[]
    }

    def 'mix query and body'() {
        given:
        def cgiRequest = new TestCgiRequest(REQUEST_METHOD: "POST", CONTENT_TYPE: HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED as String, QUERY_STRING: "foo=bar", "fruit=coconut&foo=baz")

        when:
        def servletRequest = new RequestWrapper(cgiRequest, null)

        then:
        servletRequest.getParameter("foo") == "bar"
        servletRequest.getParameterValues("foo") == ["bar", "baz"] as String[]
        servletRequest.parameterMap == ["foo":["bar", "baz"] as String[], "fruit":["coconut"] as String[]]
    }

    def 'multipart form data'() {
        given:
        def cgiRequest = new TestCgiRequest(REQUEST_METHOD: "PUT",
                CONTENT_TYPE: "multipart/form-data; boundary=----WebKitFormBoundaryscziUhw4zQjbPXtK",
        """------WebKitFormBoundaryscziUhw4zQjbPXtK
Content-Disposition: form-data; name="name"

Rodolfo Cummerata
------WebKitFormBoundaryscziUhw4zQjbPXtK
Content-Disposition: form-data; name="colour"

#b10606
------WebKitFormBoundaryscziUhw4zQjbPXtK--
""")

        when:
        def servletRequest = new RequestWrapper(cgiRequest, null)

        then:
        servletRequest.getParts().size() == 2
        servletRequest.getPart("name").getContentType() == "text/plain"
        servletRequest.getPart("name").inputStream.text == "Rodolfo Cummerata"

    }
}