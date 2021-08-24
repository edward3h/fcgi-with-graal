
package org.ethelred.techtest.micronaut.controller;

/**
 *
 * @author edward
 */
public class ForceUpdateSpec {
    private String name;
    private String faction;

    public ForceUpdateSpec(String name, String faction) {
        this.name = name;
        this.faction = faction;
    }

    public ForceUpdateSpec() {
    }


    public String getName() {
        return name;
    }

    public String getFaction() {
        return faction;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ForceUpdateSpec{name=").append(name);
        sb.append(", faction=").append(faction);
        sb.append('}');
        return sb.toString();
    }


    
}
