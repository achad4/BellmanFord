import javafx.util.Pair;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Avi Chad-Friedman
 * ajc2212
 */
import java.util.Date;
public class Host {
    private DistanceVector prevDv;
    private DistanceVector curDv;
    private ArrayList<Pair<Date, DistanceVector> vectors;
    private int portNumber;
    private String iP;
    private DatagramSocket serverSock;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Host(int portNumber, String iP){
        this.portNumber = portNumber;
        this.iP = iP;
    }

    public void init(){
        int port = 4000;
        for(int i = 0; i<4; i++){
            curDv.addLink("localhost", port + i, i);
        }
    }

    public void linkUp(String iP, int portNumber){
        //Double oldWeight = prevDv.get();
        //curDv.addLink();
    }
    /*
    * Thread to listen for updates from other hosts
    * */
    class Server extends Thread{
        public void run(){
            try {
                serverSock = new DatagramSocket(portNumber);
                while (true) {
                    //recieve a DistanceVector through a byte stream
                    byte[] buf = new byte[5000];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    serverSock.receive(packet);
                    serverSock.receive(packet);
                    ByteArrayInputStream byteStream = new
                            ByteArrayInputStream(buf);
                    ObjectInputStream in = new
                            ObjectInputStream(new BufferedInputStream(byteStream));
                    DistanceVector dv = (DistanceVector)in.readObject();
                    in.close();
                }
            }catch (SocketException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }


    }
}
