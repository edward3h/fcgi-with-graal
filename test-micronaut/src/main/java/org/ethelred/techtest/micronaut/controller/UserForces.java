package org.ethelred.techtest.micronaut.controller;

import io.micronaut.core.annotation.Introspected;
import org.ethelred.techtest.micronaut.model.Force;
import org.ethelred.techtest.micronaut.model.User;

import java.util.List;

@Introspected
public class UserForces {
    User user;
    List<Force> forces;

    public UserForces(User user, List<Force> forces) {
        this.user = user;
        this.forces = forces;
    }
}
