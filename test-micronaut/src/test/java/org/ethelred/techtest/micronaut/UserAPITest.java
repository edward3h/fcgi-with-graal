package org.ethelred.techtest.micronaut;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import javax.inject.Inject;
import org.ethelred.techtest.micronaut.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
 
/**
 *
 * @author edward
 */
 @MicronautTest
public class UserAPITest {

     @Inject
     @Client("/")
     HttpClient client;

     @Test
     public void getUsers() {
         var request = HttpRequest.GET("/api/users");
         var response = client.toBlocking().retrieve(request, Argument.listOf(User.class));
         assertNotNull(response);
         assertEquals(5, response.size());
     }

}