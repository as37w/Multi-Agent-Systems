package set10111.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Storage extends Phone {
    private int space;

    @Slot(mandatory = true)

    public int getSpace(){
        return space;
    }

    public void setSpace(int space){
        this.space = space;
    }
}

