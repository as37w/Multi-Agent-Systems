package SupplyChain;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

public class Ticker extends Agent {
    public static final int NUM_DAYS = 100
            ;
    @Override
    protected void setup() {
        //add this agent to the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ticker-agent");
        sd.setName(getLocalName() + "-ticker-agent");
        dfd.addServices(sd);
        try{
            DFService.register(this, dfd);
        }
        catch(FIPAException e){
            e.printStackTrace();
        }
        //wait for the other agents to start
        doWait(25000);
        addBehaviour(new SynchAgentsBehaviour(this));
    }

    @Override
    protected void takeDown() {
        //Deregister from the yellow pages
        try{
            DFService.deregister(this);
        }
        catch(FIPAException e){
            e.printStackTrace();
        }

    }

    public class SynchAgentsBehaviour extends Behaviour {

        private int step = 0;
        private int numFinReceived = 0; //finished messages from other agents
        private int day = 0;
        private ArrayList<AID> simulationAgents = new ArrayList<>();
        /**
         * @param a	the agent executing the behaviour
         */
        public SynchAgentsBehaviour(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            switch(step) {
                case 0:
                    //find all agents using directory service
                    DFAgentDescription template1 = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("customer");
                    template1.addServices(sd);

                    DFAgentDescription template2 = new DFAgentDescription();
                    ServiceDescription sd2 = new ServiceDescription();
                    sd2.setType("manufacturer");
                    template2.addServices(sd2);

                    DFAgentDescription template3 = new DFAgentDescription();
                    ServiceDescription sd3 = new ServiceDescription();
                    sd3.setType("supplier");
                    template3.addServices(sd3);


                    try{
                        DFAgentDescription[] agentsType1  = DFService.search(myAgent,template1);
                        for(int i=0; i<agentsType1.length; i++){
                            simulationAgents.add(agentsType1[i].getName()); // this is the AID
                        }
                        DFAgentDescription[] agentsType2  = DFService.search(myAgent,template2);
                        for(int i=0; i<agentsType2.length; i++){
                            simulationAgents.add(agentsType2[i].getName()); // this is the AID
                        }

                        DFAgentDescription[] agentsType3  = DFService.search(myAgent,template3);
                        for(int i=0; i<agentsType3.length; i++){
                            simulationAgents.add(agentsType3[i].getName()); // this is the AID
                        }
                    }
                    catch(FIPAException e) {
                        e.printStackTrace();
                    }
                    //send new day message to each agent
                    ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
                    tick.setContent("new day");
                    for(AID id : simulationAgents) {
                        tick.addReceiver(id);
                    }
                    myAgent.send(tick);
                    step++;
                    day++;
                    break;
                case 1:
                    //wait to receive a "done" message from all agents
                    MessageTemplate mt = MessageTemplate.MatchContent("done");
                    ACLMessage msg = myAgent.receive(mt);
                    if(msg != null) {
                        numFinReceived++;
                        if(numFinReceived >= simulationAgents.size()) {
                            step++;
                        }
                    }
                    else {
                        block();
                    }

            }
        }

        @Override
        public boolean done() {
            return step == 2;
        }


        @Override
        public void reset() {
            super.reset();
            step = 0;
            simulationAgents.clear();
            numFinReceived = 0;
        }


        @Override
        public int onEnd() {
            System.out.println("End of day " + day);
            if(day == NUM_DAYS) {
                //send termination message to each agent
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("terminate");
                for(AID agent : simulationAgents) {
                    msg.addReceiver(agent);
                }
                myAgent.send(msg);
                myAgent.doDelete();
            }
            else {
                reset();
                myAgent.addBehaviour(this);
            }

            return 0;
        }



    }



}
