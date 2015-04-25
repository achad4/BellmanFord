import java.io.Serializable;

/**
 * Avi Chad-Friedman
 * ajc2212
 */
public class Node implements Serializable{
    private String iP;
    private int port;

    public Node(String iP, int port){
        this.iP = iP;
        this.port = port;
    }

    public int getWeight(){ return port;}

    public String getiP(){ return iP;}

}
