
import java.io.Serializable;

/**
 * Avi Chad-Friedman
 * ajc2212
 */
public class Message implements Serializable{
    public static final int DV = 0, LINK_UP = 1, LINK_DOWN = 2, SHOWRT = 3;
    private int portNumber;
    private int type;
    private DistanceVector dv;
    private String command;

    public Message(int type){
        this.type = type;
    }

    public Message(DistanceVector dv){
        this.dv = dv;
        this.type = DV;
    }

    public int getType(){ return type;}

    public DistanceVector getDv(){ return dv;}

    public int getPortNumber() { return portNumber; }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

}
