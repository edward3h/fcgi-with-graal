package org.ethelred.techtest.micronaut.controller;

import java.util.UUID;

/**
 *
 * @author edward
 */
public class ForceSpec {
    private String name;
    private String faction;
    private UUID playerId;


    public ForceSpec(String name, String faction, UUID playerId) {
        this.name = name;
        this.faction = faction;
        this.playerId = playerId;
    }

    public ForceSpec() {
    }

    public String getName() {
        return name;
    }

    public String getFaction() {
        return faction;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ForceSpec{name=").append(name);
        sb.append(", faction=").append(faction);
        sb.append(", playerId=").append(playerId);
        sb.append('}');
        return sb.toString();
    }


}
