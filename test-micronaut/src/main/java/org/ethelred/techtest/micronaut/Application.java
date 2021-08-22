package org.ethelred.techtest.micronaut;

import com.github.javafaker.Faker;
import com.github.vatbub.randomusers.Generator;
import com.github.vatbub.randomusers.result.RandomUser;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;
import org.ethelred.techtest.micronaut.model.Force;
import org.ethelred.techtest.micronaut.model.ForceRepository;
import org.ethelred.techtest.micronaut.model.User;
import org.ethelred.techtest.micronaut.model.UserRepository;

import javax.inject.Inject;
import java.util.Random;
import java.util.stream.IntStream;

public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }

    @Inject
    public Application(UserRepository userRepository, ForceRepository forceRepository) {
        this.userRepository = userRepository;
        this.forceRepository = forceRepository;
    }

    UserRepository userRepository;
    ForceRepository forceRepository;
    Random random = new Random();
    Faker faker = new Faker();

    @EventListener
    public void createTestData(StartupEvent e) {
        _createTestUsers();
    }

    private void _createTestUsers() {
        var ruSpec = new RandomUser.RandomUserSpec();
//        ruSpec.setSeed(25778L);
        Generator.generateRandomUsers(ruSpec, 5)
                .forEach(ru -> {
                    var user = userRepository.save(new User(ru.getLogin().getUsername()));
                    _createTestForces(user);
                });

    }

    private void _createTestForces(User player) {
        IntStream.rangeClosed(1, random.nextInt(5) + 1)
                .forEach(i -> {
                    var force = new Force(faker.cat().name(), faker.cat().breed(), player);
                    forceRepository.save(force);
                });
    }
}
