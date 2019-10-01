import jade.core.AID;
import jade.core.Agent;

public class BookBuyerAgent extends Agent {
    //The title of books to buy
    private String targetBookTitle;
    private AID[] sellerAgents = {new AID("seller1", AID.ISLOCALNAME),
                                  new AID("seller2", AID.ISLOCALNAME)};




    protected void setup() {
        //Printout a welcome message
        System.out.println("Hello! BuyerAgent" + getAID().getName()+"is ready.");
        //Get title of book to buy as a startup argument.
        Object[] args = getArguments();
        if(args != null && args.length > 0) {
            targetBookTitle = (String) args[0];
            System.out.println("Trying to buy" + targetBookTitle);
        }
        else {
            //Make the agent terminate immediately
            System.out.println("No book title specified");
            doDelete();
        }

    }

    //Put agent clean-up operations here
    protected void takeDown() {
        System.out.println("Buyer-Agent" + getAID().getName()+"Terminating");
    }
}
