package org.ethelred.techtest.micronaut.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import org.ethelred.techtest.micronaut.model.Force;
import org.ethelred.techtest.micronaut.model.ForceRepository;
import org.ethelred.techtest.micronaut.model.User;
import org.ethelred.techtest.micronaut.model.UserRepository;

import java.util.List;
import java.util.UUID;

@Controller("/api/users")
public class UserController {
    UserRepository userRepository;
    ForceRepository forceRepository;

    public UserController(UserRepository userRepository, ForceRepository forceRepository) {
        this.userRepository = userRepository;
        this.forceRepository = forceRepository;
    }

    @Get
    public Iterable<User> getUsers() {
        return userRepository.findAll();
    }

    @Get("/{id}")
    public User getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    @Get("/{id}/forces")
    public List<Force> getPlayerForces(UUID id) {
        var user = userRepository.findById(id);
        return forceRepository.findByPlayer(user.get());
    }
}
