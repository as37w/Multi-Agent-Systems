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
            AgentController Manufacturer = myContainer.createNewAgent("manufacturer", Manfacturer.class.getCanonicalName(), (Object[])null);
            Manufacturer.start();

            AgentController Customer = myContainer.createNewAgent("customer", Customer.class.getCanonicalName(), (Object[])null);
            Customer.start();

            AgentController Supplier1 = myContainer.createNewAgent("supplier1", SupplyChain.Supplier1.class.getCanonicalName(), null);
            Supplier1.start();

            AgentController Supplier2 = myContainer.createNewAgent("supplier2", SupplyChain.Supplier2.class.getCanonicalName(), null);
            Supplier2.start();

            AgentController Ticker = myContainer.createNewAgent("Ticker", SupplyChain.Ticker.class.getCanonicalName(), null);
            Ticker.start();


        } catch (Exception var8) {
            System.out.println("Exception starting agent: " + var8.toString());
        }

    }



}

