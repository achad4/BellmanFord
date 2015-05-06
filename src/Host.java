import javafx.util.Pair;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Avi Chad-Friedman
 * ajc2212
 */

public class Host {
    private DistanceVector curDv;
    private HashMap<DistanceVector, Date> vectors;
    private HashMap<Node, Cost> neighbors;
    private DatagramSocket serverSock;
    private DatagramSocket clientSocket;
    private int timeout;


    public Host(int portNumber, int timeout){
        try {
            this.vectors = new HashMap<DistanceVector, Date>();
            this.neighbors = new HashMap<Node, Cost>();
            //TODO: Figure out this IP shit!!
            this.curDv = new DistanceVector( InetAddress.getByName("localhost").getHostAddress(), portNumber);
            this.timeout = timeout;
            serverSock = new DatagramSocket(curDv.getOwner().getPort());
            clientSocket = new DatagramSocket();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (SocketException e){
            e.printStackTrace();
        }
    }

    public DistanceVector getCurDv(){
        return curDv;
    }

    /*Inititializes distance vector and start the tasks*/
    public void start(String config) throws NumberFormatException, FileNotFoundException, UnknownHostException{
        Scanner scan = new Scanner(new File(config));
        String[] info;
        scan.nextLine();
        while(scan.hasNextLine()){
            info = scan.nextLine().split(" ");
            String iP = info[0].split(":")[0];
            InetAddress address = InetAddress.getByName(iP);
            iP = address.getHostAddress();
            int portNumber = Integer.parseInt((info[0].split(":"))[1]);
            Node n = new Node(iP, portNumber);
            int weight = Integer.parseInt(info[1]);
            this.curDv.put(n, n, weight);
            this.neighbors.put(n, new Cost(weight, true));
        }
        System.out.println("START:");
        curDv.showRoute();
        Server serverThread = new Server();
        serverThread.start();
        HeartBeat heartBeatThread = new HeartBeat();
        heartBeatThread.start();
    }

    private ArrayList<DistanceVector> estimateCosts(){
        ArrayList<DistanceVector> deadNodes = new ArrayList<DistanceVector>();
        for(Node n : neighbors.keySet()){
            //poisson reversal
            DistanceVector temp = new DistanceVector(curDv.getOwner().getiP(), curDv.getOwner().getPort());
            for(java.util.Map.Entry<Node, Pair<Node, Cost>> e : curDv) {
                temp.put(e.getKey(), e.getValue().getKey(), e.getValue().getValue().getWeight());
                if (!e.getKey().equals(e.getValue().getKey())) {
                    //if I hop through the neighbor to get to the destination
                    if (n.equals(e.getValue().getKey())) {
                        temp.updateLink(e.getKey(), Message.LINK_DOWN);
                    }
                }
            }
            if(!curDv.getOwner().equals(n))
                send(new Message(temp), n.getiP(), n.getPort());
        }
        DistanceVector temp = new DistanceVector(curDv.getOwner().getiP(), curDv.getOwner().getPort());
        for(Node node : neighbors.keySet()){
            if(curDv.get(node).getValue().isActive())
                temp.put(node, node, neighbors.get(node).getWeight());
        }
        //update distance vector
        for(DistanceVector distanceVector : vectors.keySet()){

            //System.out.println("NEXT VECTOR");
            //distanceVector.showRoute();
            //DistanceVector distanceVector = pair.getValue();
            //System.out.println("OWNER: "+distanceVector.getOwner().getPort()+"  "+distanceVector.hashCode());
            Date recieved = vectors.get(distanceVector);
            //System.out.println("RECIEVED: "+ recieved.getTime());
            long diff = getDiff(recieved, new Date(), TimeUnit.SECONDS);
            //If update hasn't been recieved recently
            if(diff > 3*timeout){
                System.out.println("date: "+recieved.getTime()/1000);
                System.out.println("diff: "+diff);
                System.out.println("TIMING OUT LINK: "+distanceVector.getOwner().getPort());
                //curDv.updateLink(distanceVector.getOwner(), NetworkMessage.LINK_DOWN);
                //neighbors.get(distanceVector.getOwner()).destroy();
                //vectors.remove(distanceVector);
                handleTimeout(distanceVector);
                deadNodes.add(distanceVector);
                //distanceVector.getOwner().setDead(true);
                temp.get(distanceVector.getOwner()).getKey().setDead(true);
                continue;
            }
            //distanceVector.showRoute();
            for(java.util.Map.Entry<Node, Pair<Node, Cost>> e : distanceVector){
                //if the host has a entry for this node
                Node dest = e.getKey();
                Node hop = distanceVector.getOwner();
                //double distToHop = curDv.get(hop).getValue().getWeight();
                double distToHop = neighbors.get(hop).getWeight();
                //System.out.println("distToHop: "+distToHop+" hop: "+hop.getPort());

                //distance from the node that send the vector to the destination
                double distFromHop = distanceVector.get(dest).getValue().getWeight();
                //System.out.println("distFromHop: "+distFromHop + " destination: "+dest.getPort());
                Pair<Node, Cost> curEntry;

                if (((curEntry = temp.get(dest)) != null) && curDv.get(dest).getValue().isActive()) {
                    double curWeight = curEntry.getValue().getWeight();
                    //System.out.println("curWeight: " + curWeight);
                    //If the path hasn't changed check if the distance has
                    //if(hop.equals(curEntry.getKey())){
                        //if(distFromHop == Double.MAX_VALUE) {
                          //  curDv.updateLink(dest, Message.LINK_DOWN);
                        //curDv.put(dest, hop, distFromHop + distToHop);

                    //}
                    if (curWeight > distFromHop + distToHop) {
                        //check if there is a hop in between
                        Node next = temp.get(hop).getKey();
                        temp.put(dest, next, distFromHop + distToHop);
                    }
                } else {
                    temp.put(dest, hop, distFromHop + distToHop);
                }

            }
            //System.out.println("UPDATED ROUTE");
            //curDv.showRoute()
        }
        curDv = temp;
        return deadNodes;
    }

    public long getDiff(Date date1, Date date2, TimeUnit timeUnit){
        long diff = date2.getTime() - date1.getTime();
        return timeUnit.convert(diff,TimeUnit.MILLISECONDS);
    }

    /*Restore link and alert neighbor*/
    public void linkUp(String iP, int portNumber){
        Node node = new Node(iP, portNumber);
        curDv.updateLink(node, Message.LINK_UP);
        send(new Message(Message.LINK_UP), iP, portNumber);
    }

    /*Destroy link and alert neighbor*/
    public void linkDown(String iP, int portNumber){
        Node node = new Node(iP, portNumber);
        curDv.updateLink(node, Message.LINK_DOWN);
        Message message = new Message(Message.LINK_DOWN);
        message.setPortNumber(this.getCurDv().getOwner().getPort());
        send(message, iP, portNumber);
        System.out.println("linkdown: "+this.curDv.getOwner().getiP()+" "+portNumber);
        neighbors.get(node).destroy();
        curDv.showRoute();
    }

    public void handleTimeout(DistanceVector distanceVector){
        Node owner = distanceVector.getOwner();
        neighbors.get(distanceVector.getOwner()).destroy();
        //update all paths with this node as a hop
        for(java.util.Map.Entry<Node, Pair<Node, Cost>> e : curDv){
            if(e.getKey().equals(owner) || e.getValue().getKey().equals(owner)){
                curDv.updateLink(e.getKey(), Message.LINK_DOWN);
            }
        }
    }


    /*Send message via UDP packet*/
    private void send(Message message, String iP, int portNumber){
        try{
            InetAddress address = InetAddress.getByName(iP);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
            out.flush();
            out.writeObject(message);
            out.flush();
            byte[] sendBuf = byteStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, portNumber);
            clientSocket.send(packet);
            out.close();
        }catch (UnknownHostException e)
        {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    /*
    * Thread to listen for updates from other hosts
    * */
    class Server extends Thread{
        public void run(){
            try {
                while (true) {
                    //TODO: consider having iP be a inetaddress instead of string
                    //recieve a DistanceVector through a byte stream
                    byte[] buf = new byte[5000];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    serverSock.receive(packet);
                    ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
                    ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(byteStream));
                    Message message = (Message)in.readObject();
                    in.close();
                    switch(message.getType()){
                        case Message.DV:
                            handleDistanceVector(message.getDv());
                            break;
                        case Message.LINK_UP:
                            handleLinkUpdate(packet.getAddress().getHostAddress(), packet.getPort(), Message.LINK_UP);
                            break;
                        case Message.LINK_DOWN:
                            System.out.println("LINKDOWN");
                            handleLinkUpdate(packet.getAddress().getHostAddress(), message.getPortNumber(), Message.LINK_DOWN);
                            break;
                    }
                }
            }catch (SocketException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }

        //TODO: write BF function to update DV
        /*Recieve neighbor's DV and run BF to update own*/
        private void handleDistanceVector(DistanceVector dv){
            //vectors.add(new Pair<Date, DistanceVector>(new Date(), dv));
            if(dv.getOwner().getPort() == 4001) {
                //System.out.println("4001 HEEEEERE");
            }
            if(vectors.get(dv) != null){
                vectors.remove(dv);
            }
            vectors.put(dv, new Date());
        }

        /*Update link*/
        private void handleLinkUpdate(String iP, int portNumber, int flag){
            if(flag == Message.LINK_UP) {
                linkUp(iP, portNumber);
            }else{
                System.out.println(iP);
                Node node = new Node(iP, portNumber);
                curDv.updateLink(node, Message.LINK_DOWN);
                neighbors.get(node).destroy();
            }
        }


    }

    class HeartBeat extends Thread{
        class BellmanFord extends TimerTask {
            public void run(){
                ArrayList<DistanceVector> deadNodes = estimateCosts();
                for(DistanceVector d : deadNodes){
                    //System.out.println("Removing DV"+d.getOwner().getPort());
                    vectors.remove(d);
                }
            }

            public long getDiff(Date date1, Date date2, TimeUnit timeUnit){
                long diff = date2.getTime() - date1.getTime();
                return timeUnit.convert(diff,TimeUnit.MILLISECONDS);
            }
        }
        public void run(){
            //send heart beat every HEART_RATE seconds
            Timer timer = new Timer();
            timer.schedule(new BellmanFord(), 0, 1000*timeout);
        }
    }
}
