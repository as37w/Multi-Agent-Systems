package set10111.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

import java.util.ArrayList;

public class Ram extends Phone {
    private int size;
    private Boolean firstCall;
    private ArrayList<Integer> ramList = new ArrayList<>();


    @Slot (mandatory = true)

    public int getSize(){
        return size;
    }

    public void setSize(int size){
        this.size = size;
    }

    public ArrayList<Integer> getRamList(){
        return ramList;
    }

    public void setRamList(int ram){
        if(firstCall){
            ramList.add(0);
            firstCall = false;
        }
        else
        {
            ramList.add(ram);
        }


    }
}
