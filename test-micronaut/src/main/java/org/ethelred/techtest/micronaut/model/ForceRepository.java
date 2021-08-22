package org.ethelred.techtest.micronaut.model;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface ForceRepository extends CrudRepository<Force, UUID> {
    @Join("player")
    List<Force> findByPlayer(User player);
}
