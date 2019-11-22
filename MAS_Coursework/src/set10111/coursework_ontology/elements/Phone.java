package set10111.coursework_ontology.elements;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;

public class Phone implements Concept {
    private int serialNumber;
    @Slot (mandatory = true)
    public int getSerialNumber(){
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }
}