import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Runtime;

public class Application {

	public static void main(String[] args) {
		//setup the JADE environment
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		ContainerController myContainer = myRuntime.createMainContainer(myProfile);
		try{
			//start the agent controller, which is itself an agent (rma)
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			
			//Now start our own SimpleAgent, called Fred.
			AgentController myAgent = myContainer.createNewAgent("Fred", ElapsedAgent.class.getCanonicalName(), null);
			myAgent.start();
		}catch(Exception e) {
			System.out.println("Exception starting agent:" + e.toString());
		}
	}
}
