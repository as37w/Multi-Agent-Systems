package SupplyChain;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.content.onto.basic.Action;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import set10111.coursework_ontology.Ecommerce.EcommerceOntology;
import set10111.coursework_ontology.elements.*;


import java.util.HashMap;

public class Supplier2 extends Agent {

    private Codec codec = new SLCodec();
    private Ontology ontology = EcommerceOntology.getInstance();
    //stock list, with serial number as the key
    private HashMap<Integer, Phone> itemsForSale = new HashMap<>();

    protected void setup(){
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        addBehaviour(new QueryBehaviour());
        addBehaviour(new SellBehaviour());
    }

    private class QueryBehaviour extends CyclicBehaviour{
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
            ACLMessage msg = receive(mt);
            if(msg != null){
                try{
                    ContentElement ce = null;
                    System.out.println(msg.getContent());

                    ce = getContentManager().extractContent(msg);

                    if(ce instanceof Owns){
                        Owns owns = (Owns) ce;
                        Phone ph = owns.getPhone();
                        Ram ram = (Ram) ph;
                        Storage storage = (Storage) ph;
                        System.out.println("Amount of ram: " + ((Ram) ph).getSize());
                        System.out.print("Amount of storage: " + ((Storage)ph).getSpace());

                        if(itemsForSale.containsKey(((Ram) ph).getSize())) {
                            System.out.println("Requested Ram in stock");
                        }

                        if(itemsForSale.containsKey(((Storage)ph).getSpace())){
                            System.out.println("Requested Storage in stock");
                        }

                    }

                } catch(Codec.CodecException ce){
                    ce.printStackTrace();
                }

                catch (OntologyException oe){
                    oe.printStackTrace();
                }

            }else{
                block();
            }

        }

    }

    private class SellBehaviour extends CyclicBehaviour{
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = receive(mt);
            if(msg != null){
                try{
                    ContentElement ce = null;
                    System.out.println(msg.getContent());

                    ce = getContentManager().extractContent(msg);

                    if(ce instanceof Action){
                        Concept action = ((Action)ce).getAction();
                        if(action instanceof Sells){
                            Sells order = (Sells)action;
                            Phone ph = order.getPhone();

                            if(ph instanceof Phone){
                                if(itemsForSale.containsKey(((Ram) ph).getSize())) {
                                    System.out.println(((Ram) ph).getSize() + "Ram Sold");
                                }

                                if(itemsForSale.containsKey(((Storage)ph).getSpace())){
                                    System.out.println(((Storage) ph).getSpace() + "Storage Sold");
                                }

                                }

                            }
                        }
                } catch(Codec.CodecException ce){
                    ce.printStackTrace();
                }

                catch (OntologyException oe){
                    oe.printStackTrace();
                }
            }else{
                block();
            }

        }
    }
}
