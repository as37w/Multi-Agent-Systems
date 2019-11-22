package set10111.coursework_ontology.elements;


import jade.content.AgentAction;
import jade.core.AID;

public class Sells implements AgentAction {
private AID buyer;
private Phone phone;

    public AID getBuyer() {
        return buyer;
    }

    public void setBuyer(AID buyer) {
        this.buyer = buyer;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }
}
