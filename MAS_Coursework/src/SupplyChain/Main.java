package SupplyChain;

import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Main {

    public static void main(String[] args){

        Profile myProfile = new ProfileImpl();
        Runtime myRuntime = Runtime.instance();

        try{
            ContainerController myContainer = myRuntime.createMainContainer(myProfile);
            AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", (Object[])null);
            rma.start();
            AgentController Manufacturer = myContainer.createNewAgent("seller", Manfacturer.class.getCanonicalName(), (Object[])null);
            Manufacturer.start();

            AgentController Customer = myContainer.createNewAgent("customer", Customer.class.getCanonicalName(), (Object[])null);
            Customer.start();

            AgentController Supplier1 = myContainer.createNewAgent("supplier1", SupplyChain.Supplier1.class.getCanonicalName(), null);
            Supplier1.start();

            AgentController Supplier2 = myContainer.createNewAgent("supplier2", SupplyChain.Supplier2.class.getCanonicalName(), null);
            Supplier2.start();

            AgentController Ticker = myContainer.createNewAgent("Ticker", SupplyChain.Ticker.class.getCanonicalName(), null);
            Ticker.start();


            int numCust = 1;
            for(int i = 0; i <= numCust; i++){
                Customer = myContainer.createNewAgent("Customer" + i, SupplyChain.Customer.class.getCanonicalName(), null);
            }
        } catch (Exception var8) {
            System.out.println("Exception starting agent: " + var8.toString());
        }

    }



}

