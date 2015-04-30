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
    //private ArrayList<Pair<Date, DistanceVector>> vectors;
    private HashMap<DistanceVector, Date> vectors;
    private ArrayList<Node> neighbors;
    private DatagramSocket serverSock;
    private DatagramSocket clientSocket;
    private int timeout;


    public Host(int portNumber, int timeout){
        try {
            //this.vectors = new ArrayList<Pair<Date, DistanceVector>>();
            this.vectors = new HashMap<DistanceVector, Date>();
            this.neighbors = new ArrayList<Node>();
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
            this.neighbors.add(n);
        }
        System.out.println("START:");
        curDv.showRoute();
        Server serverThread = new Server();
        serverThread.start();
        HeartBeat heartBeatThread = new HeartBeat();
        heartBeatThread.start();
    }

    /*Restore link and alert neighbor*/
    public void linkUp(String iP, int portNumber){
        Node node = new Node(iP, portNumber);
        curDv.updateLink(node, NetworkMessage.LINK_UP);
        send(new NetworkMessage(NetworkMessage.LINK_UP), iP, portNumber);
    }

    /*Destroy link and alert neighbor*/
    public void linkDown(String iP, int portNumber){
        Node node = new Node(iP, portNumber);
        curDv.updateLink(node, NetworkMessage.LINK_DOWN);
        send(new NetworkMessage(NetworkMessage.LINK_DOWN), iP, portNumber);
    }


    /*Send message via UDP packet*/
    private void send(NetworkMessage message, String iP, int portNumber){
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
                    NetworkMessage message = (NetworkMessage)in.readObject();
                    in.close();
                    switch(message.getType()){
                        case NetworkMessage.DV:
                            handleDistanceVector(message.getDv());
                            break;
                        case NetworkMessage.LINK_UP:
                            handleLinkUpdate(packet.getAddress().getHostAddress(), packet.getPort(), NetworkMessage.LINK_UP);
                            break;
                        case NetworkMessage.LINK_DOWN:
                            handleLinkUpdate(packet.getAddress().getHostAddress(), packet.getPort(), NetworkMessage.LINK_DOWN);
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
            if(vectors.get(dv) != null){
                vectors.remove(dv);
            }
            vectors.put(dv, new Date());
        }

        /*Update link*/
        private void handleLinkUpdate(String iP, int portNumber, int flag){
            if(flag == NetworkMessage.LINK_UP) {
                linkUp(iP, portNumber);
            }else{
                linkDown(iP, portNumber);
            }
        }
    }

    class HeartBeat extends Thread{
        class BellmanFord extends TimerTask {
            public void run(){
                //send DV to all neighbors
                for(Node n : neighbors){
                    //poisson reversal
                    DistanceVector temp = curDv;
                    for(java.util.Map.Entry<Node, Pair<Node, Cost>> e : temp){
                        //if the hop is the neighbor
                        //TODO: Figure out Poisson reverse
                        if(e.getValue().getKey().equals(n)){
                            //temp.updateLink(e.getKey(), NetworkMessage.LINK_DOWN);
                        }

                    }
                    if(!curDv.getOwner().equals(n))
                        send(new NetworkMessage(temp), n.getiP(), n.getPort());
                }
                //update distance vector
                for(DistanceVector distanceVector : vectors.keySet()){
                    System.out.println("NEXT VECTOR");
                    //DistanceVector distanceVector = pair.getValue();
                    Date recieved = vectors.get(distanceVector);
                    long diff = getDiff(recieved, new Date(), TimeUnit.SECONDS);
                    //If update hasn't been recieved recently
                    if(diff > 3*timeout){
                        curDv.updateLink(distanceVector.getOwner(), NetworkMessage.LINK_DOWN);
                    }
                    //distanceVector.showRoute();
                    for(java.util.Map.Entry<Node, Pair<Node, Cost>> e : distanceVector){
                        //Pair<Node, Cost> p;
                        //if the host has a entry for this node
                        Node dest = e.getKey();
                        Node hop = distanceVector.getOwner();
                        double distToHop = curDv.get(hop).getValue().getWeight();
                        //System.out.println("distToHop: "+distToHop+" hop: "+hop.getPort());

                        //distance from the node that send the vector to the destination
                        double distFromHop = distanceVector.get(dest).getValue().getWeight();
                       // System.out.println("distFromHop: "+distFromHop + " destination: "+dest.getPort());
                        Pair<Node, Cost> curEntry;
                        if((curEntry = curDv.get(dest)) != null){
                            double curWeight = curEntry.getValue().getWeight();
                            //System.out.println("curWeight: "+curWeight);
                            if(curWeight > distFromHop + distToHop){
                                curDv.put(dest, hop, distFromHop + distToHop);
                            }
                        }
                        else{
                            curDv.put(dest, hop, distFromHop + distToHop);
                        }
                        System.out.println("UPDATED ROUTE");
                        curDv.showRoute();
                    }
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
