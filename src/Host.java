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
public class Host {
    private DistanceVector curDv;
    private ArrayList<Pair<Date, DistanceVector> vectors;
    private DatagramSocket serverSock;
    private DatagramSocket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Host(int portNumber){
        try {
            this.curDv = new DistanceVector(InetAddress.getLocalHost().getHostAddress(), portNumber);
            serverSock = new DatagramSocket(curDv.getOwner().getPort());
            clientSocket = new DatagramSocket();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (SocketException e){
            e.printStackTrace();
        }
    }

    public void init() throws UnknownHostException{
        int port = 4000;
        for(int i = 0; i<4; i++){

            //curDv.addLink(InetAddress.getLocalHost().getHostAddress(), port + i, (double)i);
        }
    }

    public void linkUp(String iP, int portNumber){
        curDv.restoreLink(iP, portNumber);

    }

    private void send(NetworkMessage message, String iP, int portNumber){
        try
        {
            InetAddress address = InetAddress.getByName(iP);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
            ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
            os.flush();
            os.writeObject(message);
            os.flush();
            //retrieves byte array
            byte[] sendBuf = byteStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, portNumber);
            clientSocket.send(packet);
            os.close();
        }
        catch (UnknownHostException e)
        {
            System.err.println("Exception:  " + e);
            e.printStackTrace();    }
        catch (IOException e)    { e.printStackTrace();
        }
    }

    /*
    * Thread to listen for updates from other hosts
    * */
    class Server extends Thread{
        public void run(){
            try {
                while (true) {
                    //recieve a DistanceVector through a byte stream
                    byte[] buf = new byte[5000];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    serverSock.receive(packet);
                    ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
                    ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(byteStream));
                    NetworkMessage dv = (NetworkMessage)in.readObject();
                    in.close();
                    switch(dv.getType()){
                        case NetworkMessage.DV:
                            handleDistanceVector(dv.getDv());
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

        public void handleDistanceVector(DistanceVector dv){
            vectors.add(new Pair<Date, DistanceVector>(new Date(), dv));

        }


    }
}
