import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Avi Chad-Friedman
 * ajc2212
 */
public class BFClient {
    public static void main(String[] args){
        try {
            Scanner scan = new Scanner(new File(args[0]));
            String[] initInfo = scan.nextLine().split(" ");
            int portNumber = Integer.parseInt(initInfo[0]);
            int timeout = Integer.parseInt(initInfo[1]);
            Host h = new Host(portNumber, timeout);
            h.start(args[0]);

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            for(;;) {
                try {
                    System.out.print(">");
                    //String command = scan.nextLine();
                    String command = "";
                    try {
                        // wait until we have data to complete a readLine()
                        while (!br.ready()) {
                            Thread.sleep(200);
                        }
                        command = br.readLine();
                    } catch (InterruptedException e) {
                        continue;
                    }
                    String[] info = command.split(" ");
                    //Message message = new Message(command, user);
                    int type;
                    String ip;
                    int port;
                    InetAddress address;
                    if ((type = parseCommand(info)) > 0) {
                        switch (type) {
                            case Message.LINK_UP:
                                ip = info[1];
                                address = InetAddress.getByName(ip);
                                ip = address.getHostAddress();
                                port = Integer.parseInt(info[2]);
                                h.linkUp(ip, port);
                                break;
                            case Message.LINK_DOWN:
                                ip = info[1];
                                address = InetAddress.getByName(ip);
                                ip = address.getHostAddress();
                                port = Integer.parseInt(info[2]);
                                h.linkDown(ip, port);
                                break;
                            case Message.SHOWRT:
                                h.getCurDv().showRoute();
                                break;
                            case Message.CHANGECOST:
                                ip = info[1];
                                address = InetAddress.getByName(ip);
                                ip = address.getHostAddress();
                                port = Integer.parseInt(info[2]);
                                double cost = Double.parseDouble(info[3]);
                                h.changeCost(ip, port, cost);
                                break;
                            case Message.TRANSFER:
                                File file = new File(info[1]);
                                String[] fileInfo = info[1].split("/");
                                String fileName = fileInfo[fileInfo.length - 1];
                                ip = info[2];
                                address = InetAddress.getByName(ip);
                                ip = address.getHostAddress();
                                port = Integer.parseInt(info[3]);
                                h.transferFile(ip, port, file, fileName);
                                break;
                        }
                    } else {
                        System.out.print(">Invalid command" + "\n");
                    }
                }catch (FileNotFoundException e){
                    System.out.print(">File not found" + "\n");
                }catch(UnknownHostException e){
                    e.printStackTrace();
                }catch (NumberFormatException e){
                    System.out.print(">Invalid cost" + "\n");
                }
            }

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
    }

    public static int parseCommand(String[] info){
        if(info[0].equals("LINKUP")){
            if(info.length != 3)
                return -1;
            return Message.LINK_UP;
        }
        else if(info[0].equals("LINKDOWN")){
            if(info.length != 3)
                return -1;
            return Message.LINK_DOWN;
        }else if(info[0].equals("SHOWRT")){
            return Message.SHOWRT;
        }else if(info[0].equals("CHANGECOST")){
            if(info.length != 4)
                return -1;
            return Message.CHANGECOST;
        }else if(info[0].equals("TRANSFER")){
            if(info.length != 4)
                return -1;
            return Message.TRANSFER;
        }
        return -1;
    }
}
