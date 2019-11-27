package SupplyChain;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.*;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import set10111.coursework_ontology.Ecommerce.EcommerceOntology;
import set10111.coursework_ontology.elements.*;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.HashMap;

public class Manfacturer extends Agent {
    private Codec codec = new SLCodec();
    private Ontology ontology = EcommerceOntology.getInstance();
    private AID tickerAgent;
    private ArrayList<AID> customers = new ArrayList<>();
    private ArrayList<Phone> phonesToCreate = new ArrayList<>();
    private ArrayList<AID> suppliers = new ArrayList<>();
    private ArrayList<Integer> serialNumbers = new ArrayList<>();
    private HashMap<String, Integer> warehouse = new HashMap<>();

    Phone phone = new Phone();
    Ram ram = new Ram();
    Storage storage = new Storage();
    Battery battery = new Battery();
    Screen screen = new Screen();

    //If statement to check warehouse against parts for each list then request from supplier
    ArrayList<Integer> batteryList = battery.getBatteryList();
    ArrayList<Integer> screenList = screen.getScreenList();
    ArrayList<Integer> ramList = ram.getRamList();
    ArrayList<Integer> storageList = storage.getStorageList();

    protected void setup() {
        warehouse.put("2000Battery",0);
        warehouse.put("3000Battery", 0);
        warehouse.put("4GBRam", 0);
        warehouse.put("8GBRam", 0);
        warehouse.put("64GBStorage", 0);
        warehouse.put("256GBStorage", 0);
        warehouse.put("5Screen", 0);
        warehouse.put("7Screen", 0);

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("manufacturer");
        sd.setName(getLocalName() + "-manufacturer-agent");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        addBehaviour(new TickerWaiter(this));
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
                    SequentialBehaviour dailyActivity = new SequentialBehaviour();
                    dailyActivity.addSubBehaviour(new findCustomers(myAgent));
                    dailyActivity.addSubBehaviour(new recieveOrders(myAgent));
                    dailyActivity.addSubBehaviour(new purchaseParts());
                    dailyActivity.addSubBehaviour(new sendOrders());
                    dailyActivity.addSubBehaviour(new manageInventory());
                    CyclicBehaviour os = new OffersServer(myAgent);
                    myAgent.addBehaviour(os);
                    ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();
                    cyclicBehaviours.add(os);
                    myAgent.addBehaviour(new EndDayListener(myAgent, cyclicBehaviours));
                } else {
                    //termination message to end simulation
                    myAgent.doDelete();
                }
            } else {
                block();
            }
        }

        public class findCustomers extends OneShotBehaviour {
            public findCustomers(Agent a) {
                super(a);
            }

            @Override
            public void action() {
                DFAgentDescription customerTemplate = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("customer");
                customerTemplate.addServices(sd);
                try {
                    customers.clear();
                    DFAgentDescription[] agentsType1 = DFService.search(myAgent, customerTemplate);
                    for (int i = 0; i < agentsType1.length; i++) {
                        customers.add(agentsType1[i].getName()); // this is the AID
                    }
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
            }
        }

        public class recieveOrders extends CyclicBehaviour {
            public recieveOrders(Agent a) {
                super(a);
            }

            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    try {
                        ContentElement ce = null;
                        ce = getContentManager().extractContent(msg);//ERROR
                        Action available = (Action) ce;
                        phone = (Phone) available.getAction(); // this is the order requested
                        System.out.println("Manufacturer has received: Order ID: " + phone.getSerialNumber() + " Quantity: " + phone.getQuantity());
                        phonesToCreate.add(phone);

                    } catch (Codec.CodecException ce) {
                        ce.printStackTrace();
                    } catch (OntologyException oe) {
                        oe.printStackTrace();
                    }

                    for(Phone phone : phonesToCreate){




                }

            }


        }
        public class purchaseParts extends CyclicBehaviour{
            public purchaseParts(Agent a){super(a);}

            @Override
            public void action() {
                DFAgentDescription supplierTemplate1 = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("supplier1");
                supplierTemplate1.addServices(sd);
                try{
                    suppliers.clear();
                    DFAgentDescription[] agentsType1  = DFService.search(myAgent,supplierTemplate1);
                    for(int i=0; i<agentsType1.length; i++){
                        suppliers.add(agentsType1[i].getName()); // this is the AID
                    }

                }catch (FIPAException e){
                    e.printStackTrace();
                }

                DFAgentDescription supplierTemplate2 = new DFAgentDescription();
                ServiceDescription sd1 = new ServiceDescription();
                sd.setType("supplier2");
                supplierTemplate2.addServices(sd1);
                try{
                    suppliers.clear();
                    DFAgentDescription[] agentsType2 = DFService.search(myAgent, supplierTemplate2);
                    for(int i=0; i<agentsType2.length; i++){
                        suppliers.add(agentsType2[i].getName()); // this is the AID
                    }

                }catch (FIPAException e){
                    e.printStackTrace();
                }

                ACLMessage reqOrd = new ACLMessage(ACLMessage.REQUEST);


                //there should only be one processor
                reqOrd.addReceiver(suppliers.get(0));
                reqOrd.setLanguage(codec.getName());
                reqOrd.setOntology(ontology.getName());

                Action request = new Action();
                request.setAction(phone);
                request.setActor(suppliers.get(0));
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


        }

    }
}
