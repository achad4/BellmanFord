//import javafx.util.Pair;
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
    private Date start;
    public static final int DATA_SIZE = 2048;


    public Host(int portNumber, int timeout){
        try {
            this.vectors = new HashMap<DistanceVector, Date>();
            this.neighbors = new HashMap<Node, Cost>();
            this.curDv = new DistanceVector( InetAddress.getLocalHost().getHostAddress(), portNumber);
            this.timeout = timeout;
            serverSock = new DatagramSocket(curDv.getOwner().getPort());
            clientSocket = new DatagramSocket();
            this.start = new Date();
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
        Server serverThread = new Server();
        serverThread.start();
        HeartBeat heartBeatThread = new HeartBeat();
        heartBeatThread.start();
    }

    private ArrayList<DistanceVector> estimateCosts(){
        ArrayList<DistanceVector> deadNodes = new ArrayList<DistanceVector>();
        for(Node n : neighbors.keySet()){
            //check if a neighbor never started
            long diff = getDiff(start, new Date(), TimeUnit.SECONDS);
            if(diff > 3*timeout && (vectors.get(n) == null)){
                handleTimeout(n);
            }
            //poison reversal
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
            //if it's not yourself and is active
            if(!curDv.getOwner().equals(n) && neighbors.get(n).isActive())
                send(new Message(temp), n.getiP(), n.getPort());
        }
        DistanceVector temp = new DistanceVector(curDv.getOwner().getiP(), curDv.getOwner().getPort());
        for(Node node : neighbors.keySet()){
            if(curDv.get(node).getValue().isActive())
                temp.put(node, node, neighbors.get(node).getWeight());
        }
        //update distance vector
        for(DistanceVector distanceVector : vectors.keySet()){
            Date recieved = vectors.get(distanceVector);
            long diff = getDiff(recieved, new Date(), TimeUnit.SECONDS);
            if(diff > 3*timeout){
                System.out.println("TIMING OUT: " + distanceVector.getOwner().getiP()+":"+
                        distanceVector.getOwner().getPort());
                handleTimeout(distanceVector.getOwner());
                deadNodes.add(distanceVector);
                continue;
            }
            for(java.util.Map.Entry<Node, Pair<Node, Cost>> e : distanceVector){
                //if the host has a entry for this node
                Node dest = e.getKey();
                Node hop = distanceVector.getOwner();
                double distToHop = neighbors.get(hop).getWeight();
                //distance from the node that send the vector to the destination
                double distFromHop = distanceVector.get(dest).getValue().getWeight();
                Pair<Node, Cost> curEntry;
                if ((curEntry = temp.get(dest)) != null) {
                    double curWeight = curEntry.getValue().getWeight();
                    if (curWeight > distFromHop + distToHop) {
                        //check if there is a hop in between
                        Node next = temp.get(hop).getKey();
                        temp.put(dest, next, distFromHop + distToHop);
                    }
                } else {
                    temp.put(dest, hop, distFromHop + distToHop);
                }
            }
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
        if(neighbors.get(node) != null) {
            System.out.println("No neighbor at that destination");
            return;
        }
        curDv.updateLink(node, Message.LINK_UP);
        neighbors.get(node).restore();
        Message message = new Message(Message.LINK_UP);
        message.setPortNumber(this.getCurDv().getOwner().getPort());
        send(message, iP, portNumber);

    }

    /*Destroy link and alert neighbor*/
    public void linkDown(String iP, int portNumber){
        Node node = new Node(iP, portNumber);
        curDv.updateLink(node, Message.LINK_DOWN);
        Message message = new Message(Message.LINK_DOWN);
        message.setPortNumber(this.getCurDv().getOwner().getPort());
        send(message, iP, portNumber);
        neighbors.get(node).destroy();
    }

    public void changeCost(String iP, int portNumber, double cost){
        Node node = new Node(iP, portNumber);
        //curDv.updateLink(node, Message.LINK_DOWN);
        Message message = new Message(Message.CHANGECOST);
        message.setCost(cost);
        message.setPortNumber(this.getCurDv().getOwner().getPort());
        send(message, iP, portNumber);
        curDv.put(node, node, cost);
        neighbors.put(node, new Cost(cost, true));
    }

    public void handleTimeout(Node node){
        neighbors.get(node).destroy();
        //update all paths with this node as a hop
        for(java.util.Map.Entry<Node, Pair<Node, Cost>> e : curDv){
            if(e.getKey().equals(node) || e.getValue().getKey().equals(node)){
                curDv.updateLink(e.getKey(), Message.LINK_DOWN);
            }
        }
    }

    public boolean transferFile(String iP, int portNumber, File file, String fileName) throws FileNotFoundException, IOException{
        Node node  = new Node(iP, portNumber);
        Node hop;
        //check if there is a path to the destination
        if((hop = curDv.get(node).getKey()) == null){
            return false;
        }
        FileInputStream in = new FileInputStream(file);
        int total = 0;
        int len;
        byte[] buf = new byte[DATA_SIZE];
        while((len = in.read(buf, 0, DATA_SIZE)) > -1){
            Message message = new Message(Message.TRANSFER);
            message.setFileName(fileName);
            message.setDestination(node);
            //reached end of file
            if(DATA_SIZE > len) {
                System.out.println("LAST");
                byte[] lastData;
                lastData = Arrays.copyOf(buf, len);
                message.setLast(true);
                message.setData(lastData);
            }else{
                message.setData(buf);
            }
            send(message, hop.getiP(), hop.getPort());
            total += len;
        }
        return true;
    }

    public void writeFile(File file, byte[] data, boolean lastData) throws IOException{
        FileOutputStream out = new FileOutputStream(file, true);
        out.write(data);
        if(lastData){
            System.out.println("\n>File received successfully\n>");
        }

    }

    public boolean forwardMessage(Message message){
        Node hop;
        //check if there is a path to the destination
        if((hop = curDv.get(message.getDestination()).getKey()) == null){
            return false;
        }
        System.out.println("Next hop: "+hop.getiP()+":"+hop.getPort());
        send(message, hop.getiP(), hop.getPort());
        return true;
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
                            handleLinkUpdate(packet.getAddress().getHostAddress(), message.getPortNumber(), Message.LINK_UP);
                            break;
                        case Message.LINK_DOWN:
                            handleLinkUpdate(packet.getAddress().getHostAddress(), message.getPortNumber(), Message.LINK_DOWN);
                            break;
                        case Message.CHANGECOST:
                            handleChangeCost(packet.getAddress().getHostAddress(), message.getPortNumber(), message.getCost());
                            break;
                        case Message.TRANSFER:
                            System.out.println("Packet received\nSource: "+packet.getAddress().getHostAddress()
                                    +":"+message.getPortNumber());
                            handleTransfer(message);
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


        /*Recieve neighbor's DV and run BF to update own*/
        private void handleDistanceVector(DistanceVector dv){
            if(!neighbors.containsKey(dv.getOwner())){
                //new neighbor
                neighbors.put(dv.getOwner(), dv.get(curDv.getOwner()).getValue());
                curDv.put(dv.getOwner(), dv.getOwner(), dv.get(curDv.getOwner()).getValue().getWeight());
            }
            if(vectors.get(dv) != null){
                vectors.remove(dv);
            }
            vectors.put(dv, new Date());
        }

        /*Update link*/
        private void handleLinkUpdate(String iP, int portNumber, int flag){
            Node node = new Node(iP, portNumber);
            if(flag == Message.LINK_UP) {
                curDv.updateLink(node, Message.LINK_UP);
                neighbors.get(node).restore();
            }else{
                curDv.updateLink(node, Message.LINK_DOWN);
                neighbors.get(node).destroy();
            }
        }

        private void handleChangeCost(String iP, int portNumber, double cost){
            Node node = new Node(iP, portNumber);
            curDv.put(node, node, cost);
            neighbors.remove(node);
            neighbors.put(node, new Cost(cost, true));
        }

        private void handleTransfer(Message message){
            Node dest = message.getDestination();
            System.out.println("Destination: "+dest.getiP()+":"+dest.getPort());
            if(dest.equals(curDv.getOwner())){
                try {
                    File file = new File(message.getFileName());
                    writeFile(file, message.getData(), message.isLast());
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else{
                if(!forwardMessage(message)){
                    System.out.println("Transfer Path not found\n>");
                }
            }
        }


    }

    class HeartBeat extends Thread{
        class BellmanFord extends TimerTask {
            public void run(){
                ArrayList<DistanceVector> deadNodes = estimateCosts();
                for(DistanceVector d : deadNodes){
                    vectors.remove(d);
                }
            }
        }
        public void run(){
            //send heart beat every HEART_RATE seconds
            Timer timer = new Timer();
            timer.schedule(new BellmanFord(), 0, 1000*timeout);
        }
    }
}
