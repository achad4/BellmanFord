
import java.io.Serializable;

/**
 * Avi Chad-Friedman
 * ajc2212
 */
public class NetworkMessage implements Serializable{
    public static final int DV = 0, LINK_UP = 1, LINK_DOWN = 2;
    private int type;
    private DistanceVector dv;

    public NetworkMessage(int type){
        this.type = type;
    }

    public NetworkMessage(DistanceVector dv){
        this.dv = dv;
        this.type = DV;
    }


    public int getType(){ return type;}

    public DistanceVector getDv(){ return dv;}

}
