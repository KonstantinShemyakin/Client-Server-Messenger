import org.apache.xmlbeans.XmlException;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.*;
import java.util.*;

public class ClientHandler {
    private String userName;

    public ClientHandler(String user_name){
        this.userName = user_name;
    }

    public void clientStart(int port) throws InterruptedException{
        try(Socket socket = new Socket("localhost", port);
            BufferedReader br =new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
            DataInputStream ois = new DataInputStream(socket.getInputStream());)
        {
            int prev_input_byte = 0;
            MessageProtocol message = new MessageProtocol();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DateFormat formatter_time = new SimpleDateFormat("HH:mm:ss");
            Calendar curr_date = new GregorianCalendar();

            Thread.sleep(100);
            curr_date = Calendar.getInstance();
            System.out.println("(" + formatter.format(curr_date.getTime()) + ") " + this.userName + " connected to channel.");
            message.writeMessage(curr_date, "Connected", this.userName, this.userName);
            oos.writeUTF(message.toString());
            oos.flush();
            message.clearMessage();

            while(!socket.isOutputShutdown()){

                if(br.ready()){
                    prev_input_byte = ois.available();
                    String clientCommand = br.readLine();

                    if(clientCommand.equalsIgnoreCase("-h")){
                        curr_date = Calendar.getInstance();
                        message.writeMessage(curr_date, clientCommand, this.userName, this.userName);
                        oos.writeUTF(message.toString());
                        oos.flush();
                        Thread.sleep(1000);

                        if(ois.available() - prev_input_byte > 0)     {
                            String in = ois.readUTF();
                        } else {
                            System.out.println("Problems detected on server side(no echo recieved).");
                        }
                        break;
                    } else if (clientCommand.startsWith("-m ")) {
                        String command[] = clientCommand.replace("-m ", "").split(" ");
                        curr_date = Calendar.getInstance();
                        if(command.length >= 2) {
                            message.writeMessage(curr_date, command[1], this.userName, this.userName);
                            oos.writeUTF(message.getMessage());
                            oos.flush();
                            System.out.println("(" + formatter.format(curr_date.getTime()) + ") " + this.userName + " init:" + message.getInitName() + ": " + command[1]);
                        } else {
                            message.writeMessage(curr_date, "text-template", this.userName, this.userName);
                            oos.writeUTF(message.getMessage());
                            oos.flush();
                            System.out.println("(" + formatter.format(curr_date.getTime()) + ") " + this.userName + " init:" + message.getInitName() + ": text-template-");
                        }

                        try{
                            System.out.println(command[0]);
                            Thread.sleep(Integer.valueOf(command[0]));
                        } catch(Exception e) {
                            System.out.println(e.getMessage());
                            Thread.sleep(100);
                        }
                        ;
                        if(ois.available() - prev_input_byte > 0)     {
                            String in = ois.readUTF();
                        } else {
                            System.out.println("Problems detected on server side(no echo recieved).");
                        }
                    }
                    message.clearMessage();
                    Thread.sleep(100);
                }
            }
            System.out.println("(" + formatter.format(curr_date.getTime()) + ") " + this.userName + " disconnected from channel.");

        } catch (ConnectException e) {
            System.out.println("Can't connect to server on port:" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
