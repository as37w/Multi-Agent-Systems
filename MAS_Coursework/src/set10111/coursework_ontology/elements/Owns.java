package set10111.coursework_ontology.elements;

import jade.content.Predicate;
import jade.core.AID;

public class Owns implements Predicate {
    private AID owner;
    private Phone phone;

    public AID getOwner(){
        return owner;
    }

    public void setOwner(AID owner) {
        this.owner = owner;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }
}
