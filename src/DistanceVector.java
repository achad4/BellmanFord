import java.io.Serializable;
import java.util.HashMap;

/**
 * Avi Chad-Friedman
 * ajc2212
 *
 * Represents a distance vector to be transmitted between routers
 */
public class DistanceVector implements Serializable{
    private Node owner;
    private HashMap<Node, Cost> links;

    public DistanceVector(String iP, int port){
        this.owner = new Node(iP, port);
        links = new HashMap<Node, Cost>();
    }

    public Node getOwner(){ return owner;}

    /*Create a new node and add a link to that node*/
    public void addLink(String iP, int port, Cost weight){
        Node n = new Node(iP, port);
        this.links.put(n, weight);
    }

    /*returns true on success and false if no link exists*/
    public boolean restoreLink(String iP, int port){
        Node search = new Node(iP, port);
        for(Node n : links.keySet()){
            if(n.equals(search)){
                links.get(n).restore();
                return true;
            }
        }
        return false;
    }


}
