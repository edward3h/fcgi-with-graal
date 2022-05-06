package org.ethelred.cgi.servlet

import spock.lang.Specification

import java.nio.charset.StandardCharsets

class TestContentType extends Specification {
    def "test some good values"(headerText, mimetype, charset, boundary) {
        when:
        def contentType = ContentType.parse(headerText)

        then:
        contentType.mimetype == mimetype
        contentType.charset == charset
        contentType.boundary == boundary

        where:
        headerText | mimetype | charset | boundary
        "application/x-www-form-urlencoded" | "application/x-www-form-urlencoded" | StandardCharsets.ISO_8859_1 | null
        "application/x-www-form-urlencoded; charset=utf-8" | "application/x-www-form-urlencoded" | StandardCharsets.UTF_8 | null
        "multipart/form-data; boundary=sdjks75" | "multipart/form-data" | StandardCharsets.ISO_8859_1 | "sdjks75"
        "multipart/form-data; boundary=----WebKitFormBoundaryscziUhw4zQjbPXtK" | "multipart/form-data" | StandardCharsets.ISO_8859_1 | "----WebKitFormBoundaryscziUhw4zQjbPXtK"

    }

    def "test some bad values"(headerText) {
        when:
        def contentType = ContentType.parse(headerText)

        then:
        thrown(IllegalArgumentException)

        where:
        headerText | _
        "yoyo" | _
    }
}
