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
import sun.plugin2.message.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        warehouse.put("2000Battery", 0);
        warehouse.put("3000Battery", 0);
        warehouse.put("4GBRam", 0);
        warehouse.put("8GBRam", 0);
        warehouse.put("64GBStorage", 0);
        warehouse.put("256GBStorage", 0);
        warehouse.put("5Screen", 0);
        warehouse.put("7Screen", 0);

        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

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
                    //    dailyActivity.addSubBehaviour(new purchaseParts());
                    //  dailyActivity.addSubBehaviour(new sendOrders());
                    //dailyActivity.addSubBehaviour(new manageInventory());
                    //CyclicBehaviour os = new OffersServer(myAgent);
                    //myAgent.addBehaviour(os);
                    //ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();
                    //cyclicBehaviours.add(os);
                    //myAgent.addBehaviour(new EndDayListener(myAgent, cyclicBehaviours));
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

        public class recieveOrders extends OneShotBehaviour {
            public recieveOrders(Agent a) {
                super(a);
            }

            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchConversationId("cust-order"), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    try {
                        System.out.println("YEET" + msg.getContent());
                        ContentElement ce = null;
                        ce = getContentManager().extractContent(msg);//ERROR
                        Action available = (Action) ce;
                        SendOrder sendorder =((SendOrder) available.getAction());// this is the order requested
                        System.out.println(sendorder.toString());
                        System.out.println("Manufacturer has received: Order ID: " + sendorder.getOrder());
                        phonesToCreate.add(phone);

                        Order order = sendorder.getOrder();
                        ArrayList<Item> items = new ArrayList<>();
                        items = order.getParts();

                        for(Item parts : items){

                            if(parts instanceof Screen){
                                System.out.println("Manufacturor has received Parts: "+((Screen) parts).getLength());
                                screenList.add(((Screen) parts).getLength());
                            }
                            if(parts instanceof Battery){
                                System.out.println("Manufacturor has received Parts: "+((Battery)parts).getCapacity());
                                batteryList.add(((Battery)parts).getCapacity());
                            }
                            if(parts instanceof Storage){
                                System.out.println("Manufacturor has received Parts: "+((Storage)parts).getSpace());
                                storageList.add(((Storage)parts).getSpace());
                            }
                            if(parts instanceof Ram){
                                System.out.println("Manufacturor has received Parts: "+((Ram)parts).getSize());
                                ramList.add(((Ram)parts).getSize());
                            }

                        }

                    } catch (Codec.CodecException ce) {
                        ce.printStackTrace();
                    } catch (OntologyException oe) {
                        oe.printStackTrace();
                    }

                    for (Phone phone : phonesToCreate) {

                    }

                }
            }

            public class purchaseParts extends CyclicBehaviour {
                public purchaseParts(Agent a) {
                    super(a);
                }

                @Override
                public void action() {
                    DFAgentDescription supplierTemplate1 = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("supplier1");
                    supplierTemplate1.addServices(sd);
                    try {
                        suppliers.clear();
                        DFAgentDescription[] agentsType1 = DFService.search(myAgent, supplierTemplate1);
                        for (int i = 0; i < agentsType1.length; i++) {
                            suppliers.add(agentsType1[i].getName()); // this is the AID
                        }

                    } catch (FIPAException e) {
                        e.printStackTrace();
                    }

                    DFAgentDescription supplierTemplate2 = new DFAgentDescription();
                    ServiceDescription sd1 = new ServiceDescription();
                    sd.setType("supplier2");
                    supplierTemplate2.addServices(sd1);
                    try {
                        suppliers.clear();
                        DFAgentDescription[] agentsType2 = DFService.search(myAgent, supplierTemplate2);
                        for (int i = 0; i < agentsType2.length; i++) {
                            suppliers.add(agentsType2[i].getName()); // this is the AID
                        }

                    } catch (FIPAException e) {
                        e.printStackTrace();
                    }

                    ACLMessage reqOrd = new ACLMessage(ACLMessage.REQUEST);

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


                    for (Map.Entry<String, Integer> entry : warehouse.entrySet()) {
                        int no_of_4gb = 0;
                        int no_of_8gb = 0;
                        int no_of_5 = 0;
                        int no_of_7 = 0;
                        int no_of_3000 = 0;
                        int no_of_2000 = 0;
                        int no_of_64gb = 0;
                        int no_of_256gb = 0;
                        if (entry.getKey() == "4GBRam") {
                            no_of_4gb = entry.getValue();
                        } else if (entry.getKey() == "8GBRam") {
                            no_of_8gb = entry.getValue();
                        } else if (entry.getKey() == "5Screen") {
                            no_of_5 = entry.getValue();
                        } else if (entry.getKey() == "7Screen") {
                            no_of_7 = entry.getValue();
                        } else if (entry.getKey() == "3000Battery") {
                            no_of_3000 = entry.getValue();
                        } else if (entry.getKey() == "2000Battery") {
                            no_of_2000 = entry.getValue();
                        } else if (entry.getKey() == "64GBStorage") {
                            no_of_64gb = entry.getValue();
                        } else if (entry.getKey() == "256GBStorage") {
                            no_of_256gb = entry.getValue();
                        }
                    }


                }

            }


        }

        private class endDay extends OneShotBehaviour {
            public endDay(Agent a) {
                super(a);
            }

            @Override
            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(tickerAgent);
                msg.setContent("done");
                myAgent.send(msg);
            }

        }

    }
}
