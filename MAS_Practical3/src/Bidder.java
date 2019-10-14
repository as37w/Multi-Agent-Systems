import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Bidder extends Agent {
    private String targetItem;
    private AID[] auctioneerAgents = {(new AID("auctioneer1", AID.ISLOCALNAME)),new AID("auctioneer2", AID.ISLOCALNAME)};

    protected void setup() {

        System.out.println("Hello! Auctioneer " + getAID().getName() + " is ready.");
        //Get the title of book to buy as a startup argument.
        Object[] args = getArguments();
        if(args != null && args.length > 0){
            targetItem = (String) args[0];
            System.out.println("Trying to buy" + targetItem);

            addBehaviour(new TickerBehaviour(this, 60000) {
                @Override
                protected void onTick() {
                    //Update the list of auctions
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("auction");
                    template.addServices(sd);

                    try{
                        DFAgentDescription[] result = DFService.search(myAgent,template);
                        auctioneerAgents = new AID[result.length];
                        for( int i = 0; i < result.length; ++i){
                            auctioneerAgents[i] = result[i].getName();
                        }

                    }
                    catch(FIPAException fe){
                        fe.printStackTrace();
                    }

                    //Perform the request
                    myAgent.addBehaviour(new RequestPerformer());


                }
            });
        }else{
            //Terminate agent
            System.out.println("No bid specified");
            doDelete();
        }
    }

    //Put agent clean-up operations here
    protected void takeDown() {
        System.out.println("Bidder-Agent" + getAID().getName() + "Terminating");
    }

    private class RequestPerformer extends Behaviour {
        private  AID highestBidder;
        private int currentBid;
        private int repliesCNT = 0;
        private MessageTemplate mt;
        private int step = 0;

        public void action(){
            switch (step) {
                case 0:
                    //Send the cfp to all auctioneers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < auctioneerAgents.length; ++i){
                        cfp.addReceiver(auctioneerAgents[i]);
                    }
                    cfp.setContent(targetItem);
                    cfp.setConversationId("auction");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());
                    myAgent.send(cfp);

                    //Prepare the template to get proposal
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("auction"), MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;

                case 1:
                    //Recieve all proposals
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        //reply recieved
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            //This is an offer
                            int price = Integer.parseInt(reply.getContent());
                            if (highestBidder == null || price > currentBid){
                                currentBid = price;
                                highestBidder = reply.getSender();
                            }
                        }
                        repliesCNT++;
                        if(repliesCNT >= auctioneerAgents.length){
                            //We recieved all replies
                            step = 2;
                        }

                    }else{
                        block();
                    }
                    break;

                case 2:
                    //Send the bid
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(highestBidder);
                    order.setContent(targetItem);
                    order.setConversationId("auction");
                    order.setReplyWith("Order" + System.currentTimeMillis());
                    myAgent.send(order);

                    //prepare the template to get the purchase order
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("auction"), MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;

                case 3:
                    //Recieve the order reply
                    reply = myAgent.receive(mt);
                    if(reply != null){
                        //Purchase order reply recieved
                        if(reply.getPerformative() == ACLMessage.INFORM){
                            System.out.println(targetItem + "won by bidder" + myAgent.getName());
                            System.out.println("Price: " + currentBid);
                            myAgent.doDelete();
                        }
                        step = 4;
                    }else{
                        block();
                    }
                    break;
            }
        }

        public boolean done(){
            return((step == 2 && highestBidder == null || step == 4));
        }

    }

}


