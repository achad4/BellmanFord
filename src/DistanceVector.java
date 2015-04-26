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
    public void addLink(Node n, int weight){
        Cost c = new Cost(weight, true);
        this.links.put(n, c);
    }

    /*returns true on success and false if no link exists*/
    public boolean updateLink(String iP, int port, int flag){
        Node search = new Node(iP, port);
        for(Node n : links.keySet()){
            if(n.equals(search)){
                if(flag == NetworkMessage.LINK_UP) {
                    links.get(n).restore();
                }else{
                    links.get(n).destroy();
                }
                return true;
            }
        }
        return false;
    }


}
