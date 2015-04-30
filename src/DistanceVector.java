import javafx.util.Pair;

import javax.xml.soap.*;
import java.io.Serializable;
import java.util.Date;
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
        //add yourself to the table
        this.put(this.owner, this.owner, 0);
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
    public boolean updateLink(Node node, int flag){
        if(this.links.get(node) != null){
            if(flag == NetworkMessage.LINK_UP) {
                System.out.println("Destroying link to "+node.getPort());
                links.get(node).getValue().restore();
            }else{
                System.out.println("Restoring link to "+node.getPort());
                links.get(node).getValue().destroy();
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

    /*Display the routing table*/
    public void showRoute(){
        Date date = new Date();
        System.out.println("\n"+date.toString() + "Distance vector is:");
        for(java.util.Map.Entry<Node, Pair<Node, Cost>> e : this){
            System.out.println("Destination = " + e.getKey().getPort() +
                    ", Cost = " + e.getValue().getValue().getWeight() + ", Link = ("
                    + e.getValue().getKey().getPort() + ")");
        }
    }

    @Override
    public int hashCode(){
        return this.owner.hashCode();
    }

    @Override
    public boolean equals(Object object){
        DistanceVector dv = (DistanceVector) object;
        if(this.owner  == dv.owner)
            return true;
        return false;
    }


}
