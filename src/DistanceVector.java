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
    private HashMap<Node, Double> links;

    public DistanceVector(){
        links = new HashMap<Node, Double>();
    }

    /*Create a new node and add a link to that node*/
    public void addLink(String iP, int port, Double weight){
        Node next = new Node(iP, port);
        //if the link exists, update it
        for(Node n : links.keySet()){
            if(n.getiP().equals(next.getiP())){
                links.remove(n);
                links.put(next, weight);
                return;
            }
        }
        this.links.put(next, weight);
    }

    class Cost{
        boolean deleted;
        int weight;
    }


}
