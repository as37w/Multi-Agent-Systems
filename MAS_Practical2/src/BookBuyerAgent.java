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

public class BookBuyerAgent extends Agent {
    //The title of books to buy
    private String targetBookTitle;
    private AID[] sellerAgents = {new AID("seller1", AID.ISLOCALNAME),
            new AID("seller2", AID.ISLOCALNAME)};


    protected void setup() {
        //Printout a welcome message
        System.out.println("Hello! BuyerAgent" + getAID().getName() + "is ready.");
        //Get title of book to buy as a startup argument.
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            targetBookTitle = (String) args[0];
            System.out.println("Trying to buy " + targetBookTitle);


            addBehaviour(new TickerBehaviour(this, 6000) {
                protected void onTick() {
                    //Update the list of seller agents
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("book-selling");
                    template.addServices(sd);
                    try{
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        sellerAgents = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            sellerAgents[i] = result[i].getName();
                        }
                    }
                    catch (FIPAException fe){
                        fe.printStackTrace();
                    }

                    //Perform the request
                    myAgent.addBehaviour(new RequestPerformer());

                }
            });
        } else {
            //Make the agent terminate immediately
            System.out.println("No book title specified");
            doDelete();
        }

    }


    //Put agent clean-up operations here
    protected void takeDown() {
        System.out.println("Buyer-Agent" + getAID().getName() + "Terminating");
    }


   private class RequestPerformer extends Behaviour {
        private AID bestSeller;//The agent who provides the best offer
        private int bestPrice;//The best offered price
        private int repliesCnt = 0;
        private MessageTemplate mt; //The template to receive replies
        private int step = 0;

        public void action() {
            switch (step) {
                case 0:
                    //Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < sellerAgents.length; ++i) {
                        cfp.addReceiver(sellerAgents[i]);
                    }
                    cfp.setContent(targetBookTitle);
                    cfp.setConversationId("book-trade");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); //unique values
                    myAgent.send(cfp);

                    //prepare the template to get proposal
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    //Recieve all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        //reply recieved
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            //This is an offer
                            int price = Integer.parseInt(reply.getContent());
                            if (bestSeller == null || price < bestPrice) {
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= sellerAgents.length) {
                            //We receieved all replies
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
            }
        }

        public boolean done() {
            return ((step == 2 && bestSeller == null || step == 4));
        }

    }
}//End of inner class RequestPerformer

