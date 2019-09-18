import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Exercise1 {
	public static void main(String[] args) {
		//setup the JADE environment
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		ContainerController myContainer = myRuntime.createMainContainer(myProfile);
		try{
			//start the agent controller, which is itself an agent (rma)
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			int agentcounter = 1;
			int agentmax = 11;
			//Now start our own SimpleAgent, called Fred.
			for (agentcounter=1; agentcounter<agentmax; agentcounter++)
			 {
				  rma = myContainer.createNewAgent("Fred" +agentcounter, SimpleAgent.class.getCanonicalName(), null);
				  rma.start();
			 }
			
		}catch(Exception e) {
			System.out.println("Exception starting agent:" + e.toString());
		}
	}
}
