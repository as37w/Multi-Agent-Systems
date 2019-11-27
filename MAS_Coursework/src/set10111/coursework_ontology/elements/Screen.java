package set10111.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

import java.util.ArrayList;

public class Screen extends Phone {
    private Boolean firstCall = true;
    private int length;
    private ArrayList<Integer> screenList = new ArrayList<>();

    @Slot(mandatory = true)

    public int getLength(){
        return length;
    }

    public void setSize(int length) {
        this.length = length;
    }

    public ArrayList<Integer> getScreenList(){
        return screenList;
    }

    public void setScreenList(Integer screen){
        if(firstCall){
            screenList.add(0);
            firstCall = false;
        }
        else
        {
            screenList.add(screen);
        }

    }
}

