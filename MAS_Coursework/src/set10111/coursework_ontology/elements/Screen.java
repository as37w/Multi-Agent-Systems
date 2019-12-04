package set10111.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

import java.util.ArrayList;

public class Screen extends Item {
    private int length;
    private ArrayList<Integer> screenList = new ArrayList<>();

    @Slot(mandatory = true)

    public int getLength(){
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}

