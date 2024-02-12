import java.net.*;
import java.io.*;

public class ClientInfo {
    private Socket socket;
    private String name;
    private DataInputStream is;
    private DataOutputStream os;

    public ClientInfo(String name, Socket socket, DataInputStream input, DataOutputStream output){
        this.socket = socket;
        this.name = name;
        this.is = input;
        this.os = output;
    }

    public String getName(){
        return this.name;
    }

    public Socket getSocket(){
        return this.socket;
    }

    public DataInputStream getInput(){
        return this.is;
    }

    public DataOutputStream getOutput(){
        return this.os;
    }
}
