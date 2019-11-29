package set10111.coursework_ontology.elements;

import jade.content.onto.annotations.Slot;

import java.util.ArrayList;

public class Battery extends Item {

    private ArrayList<Integer> batteryList = new ArrayList<>();
    private Boolean firstCall = true;
    private int capacity;

    @Slot(mandatory = true)

    public int getCapacity(){
        return capacity;
    }

    public void setCapacity(Integer capacity){
        this.capacity= capacity;
    }

    public ArrayList<Integer> getBatteryList(){
        return batteryList;
    }

    public void setBatteryList(int battery){
        if(firstCall){
            batteryList.add(0);
            firstCall = false;
            batteryList.add(battery);
        }
        else
        {
            batteryList.add(battery);
        }

    }


}

