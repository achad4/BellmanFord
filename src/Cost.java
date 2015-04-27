import java.io.Serializable;

/**
 * Avi Chad-Friedman
 * ajc2212
 * Class to encapsulate sending a link down
*/
public class Cost implements Serializable{
    private boolean active;
    private double weight;

    public Cost(double weight, boolean active){
        this.weight = weight;
        this.active = active;
    }

    public void destroy(){
        active = false;
    }

    public void restore(){
        active = true;
    }

    public double getWeight(){ return weight;};

    public boolean isActive(){ return active;}
}
