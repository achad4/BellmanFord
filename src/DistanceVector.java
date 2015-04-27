import javafx.util.Pair;

import javax.xml.soap.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.*;
import java.util.Iterator;

/**
 * Avi Chad-Friedman
 * ajc2212
 *
 * Represents a distance vector to be transmitted between routers
 */
public class DistanceVector implements Serializable, Iterable<java.util.Map.Entry<Node, Pair<Node, Cost>>>{
    private Node owner;
    private HashMap<Node, Pair<Node, Cost>> links;

    public DistanceVector(String iP, int port){
        this.owner = new Node(iP, port);
        links = new HashMap<Node, Pair<Node, Cost>>();
    }

    public Node getOwner(){ return owner;}

    public Pair<Node, Cost> get(Node n){
        return this.links.get(n);
    }

    //adds an entry for destination d, next hop h, and weight w
    public void put(Node d, Node h, double w){
        Cost c = new Cost(w, true);
        if(get(d) != null){
            this.links.remove(d);
        }
        this.links.put(d, new Pair<Node, Cost>(h, c));
    }


    /*returns true on success and false if no link exists*/
    public boolean updateLink(String iP, int port, int flag){
        Node search = new Node(iP, port);
        if(this.links.get(search) != null){
            if(flag == NetworkMessage.LINK_UP) {
                links.get(search).getValue().restore();
            }else{
                links.get(search).getValue().destroy();
            }
            return true;
        }
        return false;
    }

    @Override
    //Returns iterator over the set of nodes in the distance vector
    public Iterator<java.util.Map.Entry<Node, Pair<Node, Cost>>> iterator(){
        return links.entrySet().iterator();
    }


}
