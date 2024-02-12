import java.io.*;
import java.net.UnknownHostException;
import java.util.*;

public class ClientSide {
    public static void main(String args[]) throws IOException{

        try(Scanner in = new Scanner(System.in)) {
            System.out.print("Enter your username:");
            String name = in.next();
            ClientHandler client = new ClientHandler("beba");
            client.connect(3345);
            client.clientStart();
        } catch(InterruptedException e){
            e.printStackTrace();
        } catch(UnknownHostException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
