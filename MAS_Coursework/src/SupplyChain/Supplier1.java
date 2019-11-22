package SupplyChain;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.*;
import set10111.coursework_ontology.Ecommerce.EcommerceOntology;
import set10111.coursework_ontology.elements.Phone;

import java.util.HashMap;

public class Supplier1 extends Agent {
    private Codec codec = new SLCodec();
    private Ontology ontology = EcommerceOntology.getInstance();
    //stock list, with serial number as the key
    private HashMap<Integer, Phone> itemsForSale = new HashMap<>();

    protected void setup(){
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);


    }



}
