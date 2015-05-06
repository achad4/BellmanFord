import java.io.Serializable;

/**
 * Avi Chad-Friedman
 * ajc2212
 */
public class Node implements Serializable, Comparable<Node>{
    private String iP;
    private int port;

    public Node(String iP, int port){
        this.iP = iP;
        this.port = port;
    }

    public String getiP(){ return iP;}

    public int getPort(){ return port;}

    public int compareTo(Node n){
        if(this.getiP() == n.getiP() && this.getPort() == n.getPort())
            return 0;
        return -1;
    }

    @Override
    //Hash the nodes by IP address only
    public int hashCode(){
        return this.iP.hashCode() + this.port;
    }

    @Override
    public boolean equals(Object object){
        Node node = (Node) object;
        if(this.getiP().equals(node.getiP()) && this.getPort() == node.getPort())
            return true;
        return false;
    }


}
