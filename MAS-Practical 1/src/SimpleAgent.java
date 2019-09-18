import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Runtime;
import jade.core.behaviours.TickerBehaviour;

public class SimpleAgent extends Agent {
	int w = 10;
	protected void setup() {
		addBehaviour(new TickerBehaviour(this, 1000) {
	
		if (w = 10) {
			System.out.println(w + "seconds left.");
		System.out.print("Hello! Agent " +getAID().getName()+"is ready.");
	}
	}
	
}





