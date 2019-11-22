package SupplyChain;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class Ticker extends Agent {

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
        addBehaviour();
    }
}
