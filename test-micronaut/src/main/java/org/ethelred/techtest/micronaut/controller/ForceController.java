package org.ethelred.techtest.micronaut.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import java.util.UUID;
import org.ethelred.techtest.micronaut.model.Force;
import org.ethelred.techtest.micronaut.model.ForceRepository;
import org.ethelred.techtest.micronaut.model.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author edward
 */
 @Controller("/api/forces")
public class ForceController {
    static Logger LOGGER = LoggerFactory.getLogger(ForceController.class);

    ForceRepository forceRepository;
    UserRepository userRepository;

    public ForceController(ForceRepository forceRepository, UserRepository userRepository) {
        this.forceRepository = forceRepository;
        this.userRepository = userRepository;
    }

    @Get
    public Iterable<Force> getForces() {
        return forceRepository.findAll();
    }

    @Post
    public Force create(@Body ForceSpec forceSpec) {
                LOGGER.info("create {}", forceSpec);

        var user = userRepository.findById(forceSpec.getPlayerId());
        var force = new Force(forceSpec.getName(), forceSpec.getFaction());
        force.setPlayer(user.orElseThrow());
        return forceRepository.save(force);
    }

    @Post("/{id}")
    public Force update(@PathVariable UUID id, @Body ForceUpdateSpec forceUpdateSpec) {
                LOGGER.info("update {} {}", id, forceUpdateSpec);

        var force = forceRepository.findById(id).orElseThrow();
        var update = new Force(forceUpdateSpec.getName(), forceUpdateSpec.getFaction());
        update.setId(force.getId());
        forceRepository.update(id, forceUpdateSpec.getName(), forceUpdateSpec.getFaction());
        return update;
    }

    @Delete("/{id}")
    public HttpResponse delete(@PathVariable UUID id) {
        LOGGER.info("delete {}", id);
        forceRepository.deleteById(id);
        return HttpResponse.noContent();
    }
}
