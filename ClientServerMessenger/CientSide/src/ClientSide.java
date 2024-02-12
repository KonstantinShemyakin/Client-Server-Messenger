import java.io.*;

public class ClientSide {
    public static void main(String args[]){
        ClientHandler client = new ClientHandler("beba");
        try {
            client.clientStart(3345);
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}
