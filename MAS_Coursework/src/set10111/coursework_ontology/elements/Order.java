package set10111.coursework_ontology.elements;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;

import java.util.ArrayList;

public class Order implements Concept {
    private Phone phone;
    private ArrayList<Item> parts = new ArrayList<Item>();

    @Slot (mandatory = true)
    public Phone getPhone(Phone phone){
        return phone;
    }

    public void setPhone(Phone phone){
        this.phone = phone;
    }

    @Slot (mandatory = true)
    public void setParts(ArrayList<Item> parts) {
        if (this.parts.isEmpty()) {
            this.parts.addAll(parts);
        } else {
            this.parts.clear();
            this.parts.addAll(parts);
        }
    }

        @Slot (mandatory = true)
        public ArrayList<Item> getParts(){
            return parts;
        }
    }

