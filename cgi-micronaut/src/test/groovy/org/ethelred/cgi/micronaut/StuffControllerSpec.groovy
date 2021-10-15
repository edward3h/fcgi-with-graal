package org.ethelred.cgi.micronaut

import groovy.json.JsonSlurper
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class StuffControllerSpec extends Specification {

    @Inject
    EmbeddedServer embeddedServer

    @Inject
    @Client("/")
    HttpClient client

    void "test list response"() {
        when:
        def expected = """[{"id": 1, "name":"pot", "count": 2},{"id": 2, "name":"jar", "count": 19},{"id": 3, "name":"chair", "count": 4}]"""
        def response = client.toBlocking()
                .retrieve(HttpRequest.GET('/stuff'))

        then:
            new JsonSlurper().parseText(expected) == new JsonSlurper().parseText(response)
    }

    void "test item response"() {
        when:
        def expected = """{"id": 2, "name":"jar", "count": 19}"""
        def response = client.toBlocking()
                .retrieve(HttpRequest.GET('/stuff/2'))

        then:
        new JsonSlurper().parseText(expected) == new JsonSlurper().parseText(response)
    }

    void "test add item"() {
        when:
        def expected = """{"id": 4, "name":"table", "count": 1}"""
        def response = client.toBlocking()
            .retrieve(HttpRequest.POST('/stuff', """{"name": "table", "count": 1}"""))

        then:
        new JsonSlurper().parseText(expected) == new JsonSlurper().parseText(response)
    }

    void "test query response"() {
        when:
        def expected = """[{"id": 2, "name":"jar", "count": 19},{"id": 3, "name":"chair", "count": 4}]"""
        def response = client.toBlocking()
                .retrieve(HttpRequest.GET('/stuff?query=r'))

        then:
        new JsonSlurper().parseText(expected) == new JsonSlurper().parseText(response)
    }

    void "test add item form"() {
        when:
        def expected = """{"id": 5, "name":"mug", "count": 8}"""
        def response = client.toBlocking()
                .retrieve(HttpRequest.POST('/stuff', """name=mug&count=8""")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )

        then:
        new JsonSlurper().parseText(expected) == new JsonSlurper().parseText(response)
    }
}