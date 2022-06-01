package org.ethelred.techtest.micronaut;
import java.util.Random;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.ethelred.techtest.micronaut.model.Force;
import org.ethelred.techtest.micronaut.model.ForceRepository;
import org.ethelred.techtest.micronaut.model.User;
import org.ethelred.techtest.micronaut.model.UserRepository;

import com.github.vatbub.randomusers.Generator;
import com.github.vatbub.randomusers.result.RandomUser;

import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;
import net.datafaker.Faker;

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
        if (userRepository.count() < 1) {
            _createTestUsers();
        }
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
                    var force = new Force(faker.cat().name(), faker.cat().breed());
                    force.setPlayer(player);
                    forceRepository.save(force);
                });
    }
}
