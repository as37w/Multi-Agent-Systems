import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

public class Auctioneer extends Agent {

    private Hashtable catalogue;

    private AuctioneerGui myGui;

    protected void setup() {
        catalogue = new Hashtable();

        myGui = new AuctioneerGui(this);
        myGui.show();

        addBehaviour(new OfferRequestsServer());

        addBehaviour(new PurchaseOrdersServer());


        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Auction");
        sd.setName("JADE-Auction");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

    }

    protected void takeDown() {

        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Auctioneer" + getAID().getName() + "terminating.");


    }

    public void updateCatalogue(final String title, final int price) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                catalogue.put(title, new Integer(price));
            }
        });
    }

    private class PurchaseOrdersServer extends CyclicBehaviour {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        ACLMessage msg = myAgent.receive(mt);
        public void action() {
            if (msg != null) {
                //Accept proposal recieved. Process it
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                Integer price = (Integer) catalogue.remove(title);
                if (price != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(title + "Sold to bidder" + msg.getSender().getName());
                }else{
                    //The item has been sold to another buyer in the meanwhile
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-avilable");
                }
                myAgent.send(reply);

            }else{
                block();
            }
        }
    }

    private class OfferRequestsServer extends CyclicBehaviour {
        public void action(){
            ACLMessage msg = myAgent.receive();
            if(msg != null) {
                //Message recieved process it
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                Integer price = (Integer) catalogue.get(title);
                if(price != null){
                    //The requested item is available for sale. reply with price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                }else{
                    //The item is unavailable
                    block();
                }
                myAgent.send(reply);
            }

        }
    }


}