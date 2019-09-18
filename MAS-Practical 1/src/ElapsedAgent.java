import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;

public class ElapsedAgent extends Agent {
	long t0 = System.currentTimeMillis();
	Behaviour loop;
	public void setup() {
		//Create a new TickerBehaviour 
		loop = new TickerBehaviour(this, 300) {
			//call onTick every 300ms
			
				protected void onTick() {
					//print elapsed time since launch
					System.out.println(System.currentTimeMillis()-t0 +            ": " + myAgent.getLocalName());
					if(System.currentTimeMillis()-t0 > 60000) {
						System.out.println("1 min reached. RIP Agent");
						myAgent.doDelete();
					}
				}
				
			
		};
		addBehaviour(loop);
	}
}
