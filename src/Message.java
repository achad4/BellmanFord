
import java.io.Serializable;

/**
 * Avi Chad-Friedman
 * ajc2212
 */
public class Message implements Serializable{
    public static final int DV = 0, LINK_UP = 1, LINK_DOWN = 2, SHOWRT = 3, CHANGECOST = 4, TRANSFER = 5, CLOSE = 6;
    private int portNumber;
    private int type;
    private DistanceVector dv;
    private String command;
    private double cost;
    private byte[] data;
    private boolean lastData;
    private Node destination;
    private String fileName;

    public Message(int type){
        this.type = type;
        lastData = false;
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

    public void setCost(double cost) { this.cost = cost; }

    public double getCost() { return cost; }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isLast(){
        return lastData;
    }

    public void setLast(boolean lastData){
        this.lastData = lastData;
    }

    public void setDestination(Node destination){
        this.destination = destination;
    }

    public Node getDestination(){
        return destination;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public String getFileName(){ return fileName; }



}
