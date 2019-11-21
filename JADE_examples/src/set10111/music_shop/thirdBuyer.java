package set10111.music_shop;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import set10111.music_shop_ontology.ECommerceOntology;

import java.util.ArrayList;

public class thirdBuyer extends Agent {
    private Codec codec = new SLCodec();
    private ArrayList<AID> sellers = new ArrayList<>();
    private Ontology ontology = ECommerceOntology.getInstance();
    private AID sellerAID;
    protected void setup(){
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        String[] args = (String[])this.getArguments();

    }






}
