package org.ethelred.techtest.micronaut.model;

import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;

import java.util.UUID;

@MappedEntity
public class Force {
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Id
    @AutoPopulated
    private UUID id;

    private String name;
    private String faction;

    @Relation(value = Relation.Kind.MANY_TO_ONE)
    private User player;

    public Force(String name, String faction, User player) {
        this.name = name;
        this.faction = faction;
        this.player = player;
    }

    public String getFaction() {
        return faction;
    }

    public String getName() {
        return name;
    }

    public User getPlayer() {
        return player;
    }
}
