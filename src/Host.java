import javafx.util.Pair;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Avi Chad-Friedman
 * ajc2212
 */
import java.util.Date;
import java.util.Scanner;

public class Host {
    private DistanceVector curDv;
    private ArrayList<Pair<Date, DistanceVector>> vectors;
    private DatagramSocket serverSock;
    private DatagramSocket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int timeout;


    public Host(int portNumber, int timeout){
        try {
            this.curDv = new DistanceVector(InetAddress.getLocalHost().getHostAddress(), portNumber);
            this.timeout = timeout;
            serverSock = new DatagramSocket(curDv.getOwner().getPort());
            clientSocket = new DatagramSocket();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (SocketException e){
            e.printStackTrace();
        }
    }

    /*Inititializes distance vector*/
    public void start() throws NumberFormatException, FileNotFoundException{
        Scanner scan = new Scanner(new File("config.txt"));
        String[] info;
        while((info = scan.nextLine().split(" ")) != null){
            String iP = info[0].split(":")[0];
            int portNumber = Integer.parseInt(info[0].split(":")[1]);
            Node n = new Node(iP, portNumber);
            int weight = Integer.parseInt(info[1]);
            this.curDv.addLink(n, weight);
        }
        Server serverThread = new Server();
        serverThread.start();
    }

    /*Restore link and alert neighbor*/
    public void linkUp(String iP, int portNumber){
        curDv.updateLink(iP, portNumber, NetworkMessage.LINK_UP);
        send(new NetworkMessage(NetworkMessage.LINK_UP), iP, portNumber);
    }

    /*Destroy link and alert neighbor*/
    public void linkDown(String iP, int portNumber){
        curDv.updateLink(iP, portNumber, NetworkMessage.LINK_DOWN);
        send(new NetworkMessage(NetworkMessage.LINK_DOWN), iP, portNumber);
    }

    /*Send message via UDP packet*/
    private void send(NetworkMessage message, String iP, int portNumber){
        try{
            InetAddress address = InetAddress.getByName(iP);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
            out = new ObjectOutputStream(new BufferedOutputStream(byteStream));
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
            vectors.add(new Pair<Date, DistanceVector>(new Date(), dv));
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
}
