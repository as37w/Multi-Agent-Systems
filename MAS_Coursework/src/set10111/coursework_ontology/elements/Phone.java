package set10111.coursework_ontology.elements;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;

import java.util.concurrent.atomic.AtomicInteger;

public class Phone implements Concept {
    private int serialNumber;
    private double quantity;
    private double numDaysDue;
    private double perDayPenalty;
    private double pricePerUnit;


    public int getSerialNumber(){
        return serialNumber;
    }

    public void setSerialNumber() { serialNumber++;
    }

    public double getQuantity(){
        return quantity;
    }

    @Slot (mandatory = true)
    public void setQuantity(double quantity){
        this.quantity = quantity;
    }

    @Slot (mandatory = true)
    public double getNumDaysDue(){
        return numDaysDue;
    }

    public void setnumDaysDue(double numDays){
        numDaysDue = numDays;
    }

    @Slot (mandatory = true)
    public double getPerDayPenalty(){
        return perDayPenalty;
    }

    public void setPerDayPenalty(double perDay){
        perDayPenalty = perDay;
    }

    public double getPricePerUnit(){
        return pricePerUnit;
    }

    public void setPricePerUnit(double unitPrice){
        pricePerUnit = unitPrice;
    }


}
