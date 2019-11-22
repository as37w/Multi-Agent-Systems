package set10111.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Battery extends Phone {

    private int capacity;

    @Slot(mandatory = true)

    public int getCapacity(){
        return capacity;
    }

    public void setCapacity(){
        this.capacity= capacity;
    }
}

