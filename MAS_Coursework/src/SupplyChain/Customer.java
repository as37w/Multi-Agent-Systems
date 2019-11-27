package SupplyChain;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import set10111.coursework_ontology.Ecommerce.EcommerceOntology;
import set10111.coursework_ontology.elements.*;


import java.util.ArrayList;

public class Customer extends Agent {
    private int ordersSent = 0;
    private Codec codec = new SLCodec();
    private Ontology ontology = EcommerceOntology.getInstance();
    private ArrayList<AID> manufacturers = new ArrayList<>();
    private ArrayList<Phone> phonesToBuy = new ArrayList<>();
    private AID tickerAgent;
    int numOrdersSent;
    Phone phone = new Phone();
    Ram ram = new Ram();
    Storage storage = new Storage();
    Battery battery = new Battery();
    Screen screen = new Screen();


    @Override
    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("customer");
        sd.setName(getLocalName() + "-customer-agent");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        addBehaviour(new TickerWaiter(this));
    }

    @Override
    protected void takeDown() {
        //Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    public class TickerWaiter extends CyclicBehaviour {

        //behaviour to wait for a new day
        public TickerWaiter(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("new day"),
                    MessageTemplate.MatchContent("terminate"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                if (tickerAgent == null) {
                    tickerAgent = msg.getSender();
                }
                if (msg.getContent().equals("new day")) {
                    //spawn new sequential behaviour for day's activities
                    SequentialBehaviour dailyActivity = new SequentialBehaviour();
                    //sub-behaviours will execute in the order they are added
                    dailyActivity.addSubBehaviour(new findManufacturer(myAgent));
                    dailyActivity.addSubBehaviour(new sendOrders(myAgent));
                    dailyActivity.addSubBehaviour(new endDay(myAgent));
                    myAgent.addBehaviour(dailyActivity);
                } else {
                    //termination message to end simulation
                    myAgent.doDelete();
                }
            } else {
                block();
            }
        }

    }

    public class findManufacturer extends OneShotBehaviour {
        public findManufacturer(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            DFAgentDescription manufacturerTemplate = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("manufacturer");
            manufacturerTemplate.addServices(sd);
            try {
                manufacturers.clear();
                DFAgentDescription[] agentsType1 = DFService.search(myAgent, manufacturerTemplate);
                for (int i = 0; i < agentsType1.length; i++) {
                    manufacturers.add(agentsType1[i].getName()); // this is the AID
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }

        }

    }

    public class sendOrders extends OneShotBehaviour {
        public sendOrders(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            generateOrders();

            ACLMessage reqOrd = new ACLMessage(ACLMessage.REQUEST);

            //there should only be one processor
            reqOrd.addReceiver(manufacturers.get(0));
            reqOrd.setLanguage(codec.getName());
            reqOrd.setOntology(ontology.getName());

            Action request = new Action();
            request.setAction(phone);
            request.setActor(manufacturers.get(0));
            try {
                getContentManager().fillContent(reqOrd, request); //send the wrapper object
                send(reqOrd);
            } catch (Codec.CodecException ce) {
                ce.printStackTrace();
            } catch (OntologyException oe) {
                oe.printStackTrace();
            }

        }


    }


    public void generateOrders() {

        if (Math.random() < 0.5) {
            //small phone
            screen.setScreenList(5);
            battery.setBatteryList(2000);
        } else {
            //phablet
            screen.setScreenList(7);
            battery.setBatteryList(30000);
        }
        if (Math.random() < 0.5) {
            ram.setRamList(4);
        } else {
            ram.setRamList(8);
        }
        if (Math.random() < 0.5) {
            storage.setStorageList(64);
        } else {
            storage.setStorageList(256);
        }

        phone.setSerialNumber();

        if (phone.getPricePerUnit() > 350) {
            phone.setQuantity(Math.floor(1 + 50 * Math.random()));
            phone.setPricePerUnit(Math.floor(100 + 500 * Math.random()));
            phone.setnumDaysDue(Math.floor(1 + 10 * Math.random()));
            phone.setPerDayPenalty(phone.getQuantity() * Math.floor(1 + 50 * Math.random()));
            phonesToBuy.add(phone);
            System.out.println("Customer ordered: " + phone.getQuantity() + ", order due in " + phone.getNumDaysDue() + " days.");
            ordersSent++;
        } else {
            System.out.println("Customer order did not meet minimum amount");
            ordersSent++;
        }
    }

    private class endDay extends OneShotBehaviour {
        public endDay(Agent a){super(a);}
        @Override
        public void action() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(tickerAgent);
            msg.setContent("done");
            myAgent.send(msg);
            ACLMessage customerDone = new ACLMessage(ACLMessage.INFORM);
            customerDone.setContent("done");
            for(AID manufacturer : manufacturers){
                customerDone.addReceiver(manufacturer);
            }
            myAgent.send(customerDone);
        }


    }
}
