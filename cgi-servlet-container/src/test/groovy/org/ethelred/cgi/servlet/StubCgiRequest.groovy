package org.ethelred.cgi.servlet

import groovy.transform.Memoized
import org.ethelred.cgi.CgiRequest

import java.nio.charset.StandardCharsets

class StubCgiRequest implements CgiRequest {
    Map<String, String> env = [:]
    String bodyValue
    ByteArrayOutputStream output = new ByteArrayOutputStream()

    @Override
    @Memoized
    InputStream getBody() {
        new ByteArrayInputStream(bodyValue.getBytes(StandardCharsets.UTF_8))
    }

    String getOutputValue() {
        output.toString(StandardCharsets.UTF_8)
    }
}
