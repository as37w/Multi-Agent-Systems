package set10111.coursework_ontology.elements;

import jade.content.AgentAction;
import jade.content.onto.annotations.Slot;
import jade.core.AID;
import jade.core.Agent;

public class SendOrder implements AgentAction {
    private AID customer;
    private Order order;

    public AID getCustomer(){
        return customer;
    }

    @Slot (mandatory = true)
    public void setCustomer(AID customerAID){
        customer = customerAID;
    }

    public Order getOrder(){
        return order;
    }

    @Slot (mandatory = true)
    public void setOrder(Order newOrder){
        order = newOrder;

    }
}
