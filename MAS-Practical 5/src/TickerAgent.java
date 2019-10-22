import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceDescriptor;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

public class TickerAgent extends Agent {
    protected void setup(){
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

        doWait(10000);
        addBehaviour(new SynchAgentsBehaviour(this));
    }

    protected void takeDown() {
        try{
            DFService.deregister(this);
        }

        catch(FIPAException e){
            e.printStackTrace();
        }
    }

    public class SynchAgentsBehaviour extends Behaviour {
        private int step = 0;
        private int numFinRecived = 0;
        private ArrayList<AID> simulatedAgents = new ArrayList<>();

        public SynchAgentsBehaviour(Agent a){
            super(a);
        }

        public void action(){
            switch(step){
                case 0:
                    //find all agents using directory service
                    //here we have two types of agents
                    //"simulation-agent" and "simulation-agent2"
                    DFAgentDescription template1 = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("simulation-agent");
                    template1.addServices(sd);
                    DFAgentDescription template2 = new DFAgentDescription();
                    ServiceDescription sd2 = new ServiceDescription();
                    sd.setType("simulation-agent2");
                    try{
                        simulatedAgents.clear();
                        //search for agents of type simulated agents
                        DFAgentDescription[] agentsType1 = DFService.search(myAgent, template1);
                        for(int i=0; i<agentsType1.length; i++){
                            simulatedAgents.add(agentsType1[i].getName());
                            System.out.println(agentsType1[i].getName());
                        }
                        //Search for agents of type "simulation-agent2"
                        DFAgentDescription[] agentsType2 = DFService.search(myAgent, template2);
                        for(int i=0; i<agentsType2.length; i++){
                            simulatedAgents.add(agentsType2[i].getName());
                            System.out.println(agentsType2[i].getName());
                        }
                    }
                    catch(FIPAException e){
                        e.printStackTrace();
                    }

                    //send new day message to each agent
                    ACLMessage tick = new ACLMessage(ACLMessage.INFORM);
                    tick.setContent("new day");
                    for(AID id : simulatedAgents){
                        tick.addReceiver(id);
                    }

                    myAgent.send(tick);
                    step++;
                    break;

                case 1:
                    //Wait to recieve a message from all agents
                    MessageTemplate mt = MessageTemplate.MatchContent("done");
                    ACLMessage msg = myAgent.receive(mt);
                    if(msg != null){
                        numFinRecived++;
                        if(numFinRecived >= simulatedAgents.size()){
                            step++;
                        }
                    }else{
                        block();
                    }
            }
        }
        public boolean done(){
            return step == 2;
        }

        public void reset(){
            step = 0;
            numFinRecived = 0;
        }

        public int onEnd(){
            System.out.println("End of day");
            reset();
            myAgent.addBehaviour(this);
            return 0;
        }
    }

}


