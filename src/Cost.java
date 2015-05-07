import java.io.Serializable;

/**
 * Avi Chad-Friedman
 * ajc2212
 * Class to encapsulate the cost and activity of a link
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

    public double getWeight(){
        if(active)
            return weight;
        return Double.MAX_VALUE;
    }

    public boolean isActive(){ return active; }
}
