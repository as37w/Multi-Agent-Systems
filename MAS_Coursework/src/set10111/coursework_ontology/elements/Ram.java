package set10111.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Ram extends Phone {
    private int size;

    @Slot (mandatory = true)

    public int getSize(){
        return size;
    }

    public void setSize(int size){
        this.size = size;
    }
}
