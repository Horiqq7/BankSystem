package org.poo.commerciant;

import org.poo.fileio.CommerciantInput;

import java.util.List;

public class Commerciant {
    private int id;
    private String description;
    private List<String> commerciants;

    public Commerciant(CommerciantInput commerciantInput) {
        this.id = commerciantInput.getId();
        this.description = commerciantInput.getDescription();
        this.commerciants = commerciantInput.getCommerciants();
    }

    @Override
    public String toString() {
        return "Commerciant{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", commerciants=" + commerciants +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getCommerciants() {
        return commerciants;
    }

    public void setCommerciants(List<String> commerciants) {
        this.commerciants = commerciants;
    }
}
