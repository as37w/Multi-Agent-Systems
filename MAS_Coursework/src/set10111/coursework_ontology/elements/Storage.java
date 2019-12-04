package set10111.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

import java.util.ArrayList;

public class Storage extends Item {
    private int space;
    private ArrayList<Integer> storageList = new ArrayList<>();

    @Slot(mandatory = true)

    public int getSpace(){
        return space;
    }

    public void setSpace(int space){
        this.space = space;
    }

    public ArrayList<Integer> getStorageList(){
        return storageList;
    }

}

