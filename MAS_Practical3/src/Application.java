import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;


public class Application  {
    public static void main (String[] args){
        Profile myProfile = new ProfileImpl();
        Runtime myRuntime = Runtime.instance();
        ContainerController myContainer = myRuntime.createMainContainer(myProfile);

        String[] items = {"book", "iPhone"};

        Integer[] bid = {12,14};

        try{
            AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
            rma.start();
            AgentController myAgent = myContainer.createNewAgent("Bidder", Bidder.class.getCanonicalName(), items);
            myAgent.start();

            AgentController myAgent2 = myContainer.createNewAgent("Auctioneer1", Auctioneer.class.getCanonicalName(),null );
            myAgent2.start();





        }catch(Exception e){
            System.out.println("Exception starting agent" + e.toString());
        }

    }
}
