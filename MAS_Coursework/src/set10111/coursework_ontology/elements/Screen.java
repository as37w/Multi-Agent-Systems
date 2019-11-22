package set10111.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Screen extends Phone {

    private int length;

    @Slot(mandatory = true)

    public int getLength(){
        return length;
    }

    public void setSize(int length) {
        this.length = length;
    }
}

