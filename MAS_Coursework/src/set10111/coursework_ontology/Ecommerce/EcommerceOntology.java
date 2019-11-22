package set10111.coursework_ontology.Ecommerce;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class EcommerceOntology extends BeanOntology {

    private static Ontology theInstance = new EcommerceOntology("my_ontology");
    public static Ontology getInstance(){
        return theInstance;
    }

    private EcommerceOntology(String name){
        super(name);
        try{
            add("set10111.coursework_ontology.elements");

        }catch (BeanOntologyException e){
            e.printStackTrace();
        }
    }
}
