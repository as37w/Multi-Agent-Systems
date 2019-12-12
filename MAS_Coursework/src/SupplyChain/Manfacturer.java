package SupplyChain;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import set10111.coursework_ontology.Ecommerce.EcommerceOntology;
import set10111.coursework_ontology.elements.*;
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
    private AID customer;
    private ArrayList<AID> customers = new ArrayList<>();
    private ArrayList<Phone> phonesToCreate = new ArrayList<>();
    private ArrayList<AID> suppliers = new ArrayList<>();
    private ArrayList<Integer> serialNumbers = new ArrayList<>();
    private HashMap<String, Integer> warehouse = new HashMap<>();
    private double totalQuantity = 0;
    private double currentMoney = 0;
    private double dailyWarehouseCost = 0;
    private int totalComponents = 0;
    private double profit = 0;
    private int no_of_customers = 0;
    private int phonesMade = 0;


    Phone phone = new Phone();
    Ram ram = new Ram();
    Storage storage = new Storage();
    Battery battery = new Battery();
    Screen screen = new Screen();
    Order order = new Order();
    Order sendToCustomer = new Order();

    //If statement to check warehouse against parts for each list then request from supplier
    ArrayList<Integer> ramList = ram.getRamList();
    ArrayList<Integer> storageList = storage.getStorageList();
    ArrayList<Item> itemList = new ArrayList();




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
                    dailyActivity.addSubBehaviour(new purchaseParts(myAgent));
                    dailyActivity.addSubBehaviour(new recieveParts(myAgent));
                    dailyActivity.addSubBehaviour(new sendOrders(myAgent));
                   dailyActivity.addSubBehaviour(new WarehouseTax(myAgent));
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

        public class recieveOrders extends SimpleBehaviour {
            public recieveOrders(Agent a) {
                super(a);
            }

            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("cust-order"), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    no_of_customers++;
                    try {
                        ContentElement ce = null;
                        ce = getContentManager().extractContent(msg);//ERROR
                        Action available = (Action) ce;
                        SendOrder sendorder = ((SendOrder) available.getAction());// this is the order requested
                        System.out.println( myAgent.getName() + " has recieved order!");
                        phonesToCreate.add(phone);

                        order = sendorder.getOrder();
                        if (order.getOrderCost() < 350) {
                            System.out.println("Order does not meet minimum amount of 350. Order Rejected");
                            order = new Order();
                        } else {


                            phone = order.getPhone();
                            order.setPhoneOrderQuantity(phone.getQuantity());

                            currentMoney = currentMoney += order.getOrderCost();

                            itemList = order.getParts();

                            for (Item parts : itemList) {

                                if (parts instanceof Storage) {
                                    storageList.add(((Storage) parts).getSpace());
                                }
                                if (parts instanceof Ram) {
                                    ramList.add(((Ram) parts).getSize());
                                }


                                customer = sendorder.getCustomer();
                            }

                        }

                    }catch(Codec.CodecException ce){
                        ce.printStackTrace();
                    } catch(OntologyException oe){
                        oe.printStackTrace();
                    }
                }
            }

            public boolean done(){
                if (no_of_customers == 1){
                    no_of_customers = 0;
                    return true;
                }else{
                    return false;
                }
            }
        }

        public class purchaseParts extends OneShotBehaviour {
            public purchaseParts(Agent a) {
                super(a);
            }

            @Override
            public void action() {
                DFAgentDescription supplierTemplate1 = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("supplier");
                supplierTemplate1.addServices(sd);
                try {
                    DFAgentDescription[] agentsType1 = DFService.search(myAgent, supplierTemplate1);
                    for (int i = 0; i < agentsType1.length; i++) {
                        suppliers.add(agentsType1[i].getName()); // this is the AID

                    }

                } catch (FIPAException e) {
                    e.printStackTrace();
                }


                ACLMessage reqOrd = new ACLMessage(ACLMessage.REQUEST);

                reqOrd.addReceiver(suppliers.get(0));
                reqOrd.setLanguage(codec.getName());
                reqOrd.setOntology(ontology.getName());


                reqOrd.setConversationId("manufacturer-order");
                SendOrder sendOrder = new SendOrder();
                sendOrder.setCustomer(this.myAgent.getAID());
                sendOrder.setOrder(order);




                Action request = new Action();
                request.setAction(sendOrder);
                request.setActor(suppliers.get(0));

                try {
                    getContentManager().fillContent(reqOrd, request); //send the wrapper object
                    send(reqOrd);

                } catch (Codec.CodecException ce) {
                    ce.printStackTrace();
                } catch (OntologyException oe) {
                }


            }

        }

        private class recieveParts extends OneShotBehaviour {
            public recieveParts(Agent a) {
                super(a);
            }

            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("supplier-parts"), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    try {
                        ContentElement ce = null;
                        ce = getContentManager().extractContent(msg);//ERROR
                        Action available = (Action) ce;
                        SendOrder sendorder = ((SendOrder) available.getAction());// this is the order requested
                        System.out.println(myAgent.getName() + " has recieved parts from supplier");
                        phonesToCreate.add(phone);

                         order = sendorder.getOrder();
                        phone = order.getPhone();
                        order.setPhoneOrderQuantity(phone.getQuantity());
                        itemList.clear();
                        itemList = order.getParts();

                        sendToCustomer = order;

                        double currentOrderQuantity = order.getPhoneOrderQuantity();


                        for (int i = 0; i < currentOrderQuantity; i++) {
                            try {


                                if (itemList.get(i) instanceof Screen) {
                                    if (((Screen) itemList.get(i)).getLength() == 5) {
                                        warehouse.put("5Screen", warehouse.get("5Screen") + 2);
                                        currentMoney = currentMoney - 100;
                                    } else {
                                        warehouse.put("7Screen", warehouse.get("7Screen") + 2);
                                        currentMoney = currentMoney - 150;
                                    }

                                }
                                if (itemList.get(i) instanceof Battery) {
                                    if (((Battery) itemList.get(i)).getCapacity() == 2000) {
                                        warehouse.put("2000Battery", warehouse.get("2000Battery") + 2);
                                        currentMoney = currentMoney - 70;
                                    } else {
                                        warehouse.put("3000Battery", warehouse.get("3000Battery") + 2);
                                        currentMoney = currentMoney - 100;
                                    }
                                }
                                if (itemList.get(i) instanceof Storage) {
                                    if (((Storage) itemList.get(i)).getSpace() == 64) {
                                        warehouse.put("64GBStorage", warehouse.get("64GBStorage") + 2);
                                        currentMoney = currentMoney - 25;
                                    } else {
                                        warehouse.put("256GBStorage", warehouse.get("256GBStorage") + 2);
                                        currentMoney = currentMoney - 50;
                                    }
                                }
                                if (itemList.get(i) instanceof Ram) {
                                    if (((Ram) itemList.get(i)).getSize() == 4) {
                                        warehouse.put("4GBRam", warehouse.get("4GBRam") + 2);
                                        currentMoney = currentMoney - 30;
                                    } else {
                                        warehouse.put("8GBRam", warehouse.get("8GBRam") + 2);
                                        currentMoney = currentMoney - 60;
                                    }
                                }


                            } catch (NullPointerException ne) {

                            } catch (IndexOutOfBoundsException ie) {

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

        private class sendOrders extends OneShotBehaviour {
            public sendOrders(Agent a) {
                super(a);
            }

            @Override
            public void action() {

                int no_of_4gb = 0;
                int no_of_8gb = 0;
                int no_of_5 = 0;
                int no_of_7 = 0;
                int no_of_3000 = 0;
                int no_of_2000 = 0;
                int no_of_64gb = 0;
                int no_of_256gb = 0;

                int warehouse4gb = 0;
                int warehouse8gb = 0;
                int warehouse5 = 0;
                int warehouse7 = 0;
                int warehouse3000 = 0;
                int warehouse2000 = 0;
                int warehouse64gb = 0;
                int warehouse256gb = 0;

                for (Map.Entry<String, Integer> entry : warehouse.entrySet()) {
                    if (entry.getKey() == "4GBRam") {
                        warehouse4gb = entry.getValue();
                    } else if (entry.getKey() == "8GBRam") {
                        warehouse8gb = entry.getValue();
                    } else if (entry.getKey() == "5Screen") {
                        warehouse5 = entry.getValue();
                    } else if (entry.getKey() == "7Screen") {
                        warehouse7 = entry.getValue();
                    } else if (entry.getKey() == "3000Battery") {
                        warehouse3000 = entry.getValue();
                    } else if (entry.getKey() == "2000Battery") {
                        warehouse2000 = entry.getValue();
                    } else if (entry.getKey() == "64GBStorage") {
                        warehouse64gb = entry.getValue();
                    } else if (entry.getKey() == "256GBStorage") {
                        warehouse256gb = entry.getValue();
                    }
                }

                for (int i = 0; i < sendToCustomer.getPhoneOrderQuantity(); i++) {
                    try {
                        if (itemList.get(i) instanceof Screen) {
                            if (((Screen) itemList.get(i)).getLength() == 5) {
                                no_of_5++;
                            }

                        } else {
                            no_of_7++;
                        }

                        if (itemList.get(i) instanceof Battery) {
                            if (((Battery) itemList.get(i)).getCapacity() == 2000) {
                                no_of_2000++;
                            } else {
                                no_of_3000++;
                            }
                        }
                        if (itemList.get(i) instanceof Storage) {
                            if (((Storage) itemList.get(i)).getSpace() == 64) {
                                no_of_64gb++;
                            } else {
                                no_of_256gb++;
                            }
                        }
                        if (itemList.get(i) instanceof Ram) {
                            if (((Ram) itemList.get(i)).getSize() == 4) {
                                no_of_4gb++;
                            } else {
                                no_of_8gb++;
                            }
                        }
                    } catch (NullPointerException ne) {

                    } catch (IndexOutOfBoundsException ie) {

                    }
                }


                int partsInStock = 0;


                try {


                    if (warehouse4gb >= no_of_4gb) {
                        warehouse.put("4GBRam", warehouse.get("4GBRam") - no_of_4gb);
                        partsInStock++;
                    } else {
                        System.out.println("Not enough 4gb Ram to complete order today");
                    }

                    if (warehouse8gb >= no_of_8gb) {
                        warehouse.put("8GBRam", warehouse.get("8GBRam") - no_of_8gb);
                        partsInStock++;
                    } else {
                        System.out.println("Not enough 8gb Ram to complete order today");
                    }

                    if (warehouse256gb >= no_of_256gb) {
                        warehouse.put("256GBStorage", warehouse.get("256GBStorage") - no_of_256gb);
                        partsInStock++;
                    } else {
                        System.out.println("Not enough 256gb storage to complete order today");
                    }

                    if (warehouse64gb >= no_of_64gb) {
                        warehouse.put("64GBStorage", warehouse.get("64Storage") - no_of_64gb);
                        partsInStock++;
                    } else {
                        System.out.println("Not enough 64gb storage to complete order today");
                    }

                    if (warehouse2000 >= no_of_2000) {
                        warehouse.put("2000Battery", warehouse.get("2000Battery") - no_of_2000);
                        partsInStock++;
                    } else {
                        System.out.println("Not enough 2000 battery to complete order today");
                    }

                    if (warehouse3000 >= no_of_3000) {
                        warehouse.put("3000Battery", warehouse.get("3000Battery") - no_of_3000);
                        partsInStock++;
                    } else {
                        System.out.println("Not enough 3000 battery to complete order today");
                    }

                    if (warehouse5 >= no_of_5) {
                        warehouse.put("5Screen", warehouse.get("5Screen") - no_of_5);
                        partsInStock++;
                    } else {
                        System.out.println("Not enough 5 inch screens to complete order today");
                        partsInStock++;
                    }

                    if (warehouse7 >= no_of_7) {
                        warehouse.put("7Screen", warehouse.get("7Screen") - no_of_7);
                        partsInStock++;
                    } else {
                        System.out.println("Not enough 7 inch screens to complete order today");
                    }
                } catch (NullPointerException ne) {

                }

                if (phonesMade + (int)order.getPhoneOrderQuantity() <= 50 )
                {




                if (partsInStock == 3) {
                    ACLMessage reqOrd = new ACLMessage(ACLMessage.REQUEST);

                    reqOrd.addReceiver(customers.get(0));
                    reqOrd.setLanguage(codec.getName());
                    reqOrd.setOntology(ontology.getName());

                    sendToCustomer = order;
                    reqOrd.setConversationId("completed-order");
                    SendOrder sendOrder = new SendOrder();
                    sendOrder.setCustomer(this.myAgent.getAID());
                    sendOrder.setOrder(sendToCustomer);


                    phonesMade = phonesMade + (int) sendToCustomer.getPhoneOrderQuantity();

                    Action request = new Action();
                    request.setAction(sendOrder);
                    request.setActor(customer);

                    try {
                        getContentManager().fillContent(reqOrd, request); //send the wrapper object
                        send(reqOrd);

                    } catch (Codec.CodecException ce) {
                        ce.printStackTrace();
                    } catch (OntologyException oe) {

                    }
                }

               }
            }

        }


            public class WarehouseTax extends OneShotBehaviour{
                public WarehouseTax (Agent a){
                    super(a);
                }

                @Override
                public void action(){
                    dailyWarehouseCost = 0;
                    warehouse.entrySet().forEach(entry->{
                        totalComponents +=  entry.getValue();
                    });
                    dailyWarehouseCost= totalComponents * 5;

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
                suppliers.clear();
                profit = currentMoney - dailyWarehouseCost;
                System.out.println("Total profit to date: £" +  profit);
                phonesMade = 0;
                msg.setContent("done");
                myAgent.send(msg);
            }

        }
    }








