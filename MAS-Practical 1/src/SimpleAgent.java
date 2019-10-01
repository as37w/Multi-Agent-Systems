import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import java.util.Random;

public class SimpleAgent extends Agent {
    long t0 = System.currentTimeMillis();
    int time = 10;
    int low = 60000;
    int high = 120000;
    protected void setup(){
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                System.out.println("Hello! Agent" + getAID().getName()+" is ready.");
                Random r = new Random();
                int deleteTime = r.nextInt(high-low) + low;
                if(System.currentTimeMillis() == deleteTime){
                    System.out.println("Agent" + getAID().getName() + "Has died :(");
                    myAgent.doDelete();
                }
            }



        });
    }
}
