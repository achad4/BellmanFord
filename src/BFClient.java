import java.io.File;
import java.io.FileNotFoundException;
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
            String[] info = scan.nextLine().split(" ");
            int portNumber = Integer.parseInt(info[0]);
            int timeout = Integer.parseInt(info[1]);
            Host h = new Host(portNumber, timeout);
            h.start(args[0]);

            /*TESTING SHIT*/
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
    }
}
