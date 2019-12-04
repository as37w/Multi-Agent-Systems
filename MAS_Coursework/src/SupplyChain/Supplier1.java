package SupplyChain;

import jade.content.Concept;
import jade.content.ContentElement;
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
import java.util.HashMap;

public class Supplier1 extends Agent {
    private Codec codec = new SLCodec();
    private Ontology ontology = EcommerceOntology.getInstance();
    //stock list, with serial number as the key
    private HashMap<Integer, Phone> itemsForSale = new HashMap<>();
    private AID tickerAgent;
    ArrayList<Item> itemList = new ArrayList();
    ArrayList<Item> partsOrdered = new ArrayList<>();
    private ArrayList<AID> manufacturer = new ArrayList<>();
    int daycounter = 0;
    Ram ram = new Ram();
    Storage storage = new Storage();
    Battery battery = new Battery();
    Screen screen = new Screen();

    ArrayList<Integer> batteryList = battery.getBatteryList();
    ArrayList<Integer> screenList = screen.getScreenList();
    ArrayList<Integer> ramList = ram.getRamList();
    ArrayList<Integer> storageList = storage.getStorageList();
    Order order = new Order();
    Phone phone = new Phone();


    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);


        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("supplier");
        sd.setName(getLocalName() + "-supplier-agent");
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
                    System.out.println("doo");
                    SequentialBehaviour dailyActivity = new SequentialBehaviour();
                    dailyActivity.addSubBehaviour(new recieveOrders(myAgent));
                    dailyActivity.addSubBehaviour(new sendParts(myAgent));
               //     dailyActivity.addSubBehaviour(new QueryBehaviour());
                 //   dailyActivity.addSubBehaviour(new SellBehaviour());
                    dailyActivity.addSubBehaviour(new endDay(myAgent));
                    myAgent.addBehaviour(dailyActivity);

                }
            }
        }


        private class QueryBehaviour extends CyclicBehaviour {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    try {
                        ContentElement ce = null;
                        System.out.println(msg.getContent());


                        ce = getContentManager().extractContent(msg);
                    /*

                    if(ce instanceof Owns){
                        Owns owns = (Owns) ce;
                        Phone ph = owns.getPhone();
                        System.out.println("Amount of ram: " + ramList.indexOf(ram.getSerialNumber()));
                        System.out.print("Amount of storage: " + storageList.indexOf(storage.getSerialNumber()));

                        if(itemsForSale.containsKey(ramList.indexOf(ram.getSerialNumber()))){
                          //  System.out.println("Requested Ram in stock");
                        }

                        if(itemsForSale.containsKey(storageList.indexOf(storage.getSerialNumber()))){
                            System.out.println("Requested Storage in stock");
                        }

                    }
                    */

                    } catch (Codec.CodecException ce) {
                        ce.printStackTrace();
                    } catch (OntologyException oe) {
                        oe.printStackTrace();
                    }


                } else {
                    block();
                }


            }

        }

        private class SellBehaviour extends CyclicBehaviour {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    try {
                        ContentElement ce = null;
                        System.out.println(msg.getContent());


                        ce = getContentManager().extractContent(msg);


                        if (ce instanceof Action) {
                            Concept action = ((Action) ce).getAction();
                            if (action instanceof Sells) {
                                Sells order = (Sells) action;
                                Phone ph = order.getPhone();

                                if (ph instanceof Phone) {
                                    if (itemsForSale.containsKey(ramList.indexOf(ram.getRamList()))) {
                                        System.out.println(ramList.indexOf(ram.getRamList()) + "Ram Sold");
                                    }

                                    if (itemsForSale.containsKey(storageList.indexOf(storage.getStorageList()))) {
                                        System.out.println(storageList.indexOf(storage.getStorageList()) + "Storage Sold");
                                    }


                                }


                            }

                        }
                    } catch (Codec.CodecException ce) {
                        ce.printStackTrace();
                    } catch (OntologyException oe) {
                        oe.printStackTrace();
                    }
                } else {
                    block();
                }


            }
        }

        private class recieveOrders extends OneShotBehaviour {
            public recieveOrders(Agent a) {
                super(a);
            }

            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("manufacturer-order"), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    try {
                        ContentElement ce = null;
                        ce = getContentManager().extractContent(msg);//ERROR
                        Action available = (Action) ce;
                        SendOrder sendorder = ((SendOrder) available.getAction());// this is the order requested
                        System.out.println("Supplier has received: Order ID: " + sendorder.getOrder());


                        order = sendorder.getOrder();
                        phone = order.getPhone();
                        order.setPhoneOrderQuantity(phone.getQuantity());

                        itemList = order.getParts();

                        System.out.println("snake" + order.getParts());


                        for (Item parts : itemList) {


                            if (parts instanceof Screen) {
                                screenList.add(((Screen) parts).getLength());
                            }
                            if (parts instanceof Battery) {
                                batteryList.add(((Battery) parts).getCapacity());
                            }
                            if (parts instanceof Storage) {
                                storageList.add(((Storage) parts).getSpace());
                            }
                            if (parts instanceof Ram) {
                                ramList.add(((Ram) parts).getSize());
                            }


                        }

                    } catch (Codec.CodecException ce) {
                        ce.printStackTrace();
                    } catch (OntologyException oe) {
                        oe.printStackTrace();
                    }


                }
            }
        }

        private class sendParts extends OneShotBehaviour {
            public sendParts(Agent a) {
                super(a);
            }

            @Override
            public void action() {
                if(daycounter == 1){
                    daycounter = 0;
                }
                if (itemList != null) {
                    DFAgentDescription manufacturerTemplate = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("manufacturer");
                    manufacturerTemplate.addServices(sd);
                    try {
                        DFAgentDescription[] agentsType1 = DFService.search(myAgent,  manufacturerTemplate);
                        for (int i = 0; i < agentsType1.length; i++) {
                            manufacturer.add(agentsType1[i].getName()); // this is the AID

                        }

                    } catch (FIPAException e) {
                        e.printStackTrace();
                    }




                    ACLMessage reqOrd = new ACLMessage(ACLMessage.REQUEST);

                    reqOrd.addReceiver(manufacturer.get(0));
                    reqOrd.setLanguage(codec.getName());
                    reqOrd.setOntology(ontology.getName());


                    reqOrd.setConversationId("supplier-parts");
                    SendOrder sendOrder = new SendOrder();
                    sendOrder.setCustomer(this.myAgent.getAID());
                    sendOrder.setOrder(order);






                    Action request = new Action();
                    request.setAction(sendOrder);
                    request.setActor(manufacturer.get(0));

                    System.out.println("Supplier has sent parts requested for: Order ID: " + sendOrder.getOrder());
                    try {
                        getContentManager().fillContent(reqOrd, request); //send the wrapper object
                        send(reqOrd);

                    } catch (Codec.CodecException ce) {
                        ce.printStackTrace();
                    } catch (OntologyException oe){
                    }

                    daycounter++;
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





