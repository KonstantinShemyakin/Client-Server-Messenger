import org.apache.xmlbeans.XmlException;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.*;
import java.util.*;

public class ClientHandler {
    private String userName;
    private Socket socket;
    private BufferedReader br;
    private DataOutputStream oos;
    private DataInputStream ois;
    private DateFormat dateFormatter;
    private ServerListenerThread listener;
    private int port;
    public ClientHandler(String user_name){
        this.userName = user_name;
        this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public void connect(int port) throws UnknownHostException, IOException{
        this.port = port;
        this.socket = new Socket("localhost", port);
        this.br =new BufferedReader(new InputStreamReader(System.in));
        this.oos = new DataOutputStream(socket.getOutputStream());
        this.ois = new DataInputStream(socket.getInputStream());

        this.listener = new ServerListenerThread();
        this.listener.setDaemon(true);

        MessageProtocol message = new MessageProtocol();
        Calendar curr_date = Calendar.getInstance();
        System.out.println("(" + dateFormatter.format(curr_date.getTime()) + ") " + this.userName + " connected to channel on port:" + port);
        message.addMessage(curr_date, "(" + dateFormatter.format(curr_date.getTime()) + ") " + this.userName + " connected to channel", this.userName);
        oos.writeUTF(message.toString());
        oos.flush();
        message.clearMessage();
    }

    public class ServerListenerThread extends Thread{
        //private ArrayList<MessageInfo> channelMessages;
        private ArrayList<MessageInfo> echoMessages;
        private ArrayList<MessageInfo> serverCommands;

        public ServerListenerThread(){
            //this.channelMessages = new ArrayList<>();
            this.echoMessages = new ArrayList<>();
            this.serverCommands = new ArrayList<>();
        }

        public boolean confirmEcho(String to_confirm){
            to_confirm = "%e" + to_confirm;
            if(!this.echoMessages.isEmpty()) {
                int index = -1;
                for(int i = 0; i < this.echoMessages.size(); i++){
                    if(this.echoMessages.get(i).getText().equals(to_confirm)){
                        index = i;
                        break;
                    }
                }
                if (index >= 0) {
                    this.echoMessages.remove(index);
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        /*public MessageInfo popMessageFromQue(){
            MessageInfo m = new MessageInfo();
            if(!this.channelMessages.isEmpty()) {
                m = this.channelMessages.get(0);
                this.channelMessages.remove(0);
            }
            return m;
        }*/

        public MessageInfo popServerCommand(){
            MessageInfo m = new MessageInfo();
            if(!this.serverCommands.isEmpty()) {
                m = this.serverCommands.get(0);
                this.serverCommands.remove(0);
            }
            return m;
        }

        public void run(){
            try {
                while(!socket.isOutputShutdown()){
                    String in = ois.readUTF();
                    MessageProtocol heard = new MessageProtocol();
                    heard.setMessFromString(in);

                    if(heard.getUserName().equals("Server:" + port)){
                        if(heard.getMessText().startsWith("-")){
                            this.serverCommands.add(heard.getMessageInfo());
                        } else if(heard.getMessText().startsWith("%e")){
                            this.echoMessages.add(heard.getMessageInfo());
                        }
                    } else if (heard.getMessText().equals("%c")) {
                        System.out.println("(" + dateFormatter.format(heard.getDate().getTime()) + ") " + heard.getUserName() + " connected to channel.");
                    } else if (heard.getMessText().equals("%d")) {
                        System.out.println("(" + dateFormatter.format(heard.getDate().getTime()) + ") " + heard.getUserName() + " disconnected from channel.");
                    } else {
                        System.out.println("(" + dateFormatter.format(heard.getDate().getTime()) + ") " + heard.getUserName() + ": " + heard.getMessText());
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            } catch(XmlException e) {
                e.printStackTrace();
            }
        }
    }

    public void clientStart() throws InterruptedException{
        try {
            this.listener.start();
            Thread.sleep(100);

            MessageInfo m = null;
            Calendar curr_date = null;
            String echo = null;
            MessageProtocol message = new MessageProtocol();

            while (!this.socket.isOutputShutdown()) {
                m = this.listener.popServerCommand();
                if (!m.equals(new MessageInfo())) {
                    if (m.getText().equals("-h")) {
                        System.out.println("Server closing connections.");
                        curr_date = Calendar.getInstance();
                        message.addMessage(curr_date, m.getText(), userName);
                        oos.writeUTF(message.toString());
                        oos.flush();
                        break;
                    }
                }
                if(br.ready()) {
                    String clientCommand = br.readLine();
                    curr_date = Calendar.getInstance();
                    message.addMessage(curr_date, clientCommand, userName);
                    oos.writeUTF(message.getMessage());
                    oos.flush();

                    Thread.sleep(200);
                    echo = message.getMessText();

                    if (!listener.confirmEcho(echo)) {
                        System.out.println("Problems detected on server side(no echo recieved).");
                    }
                    if (clientCommand.equalsIgnoreCase("-h")) {
                        System.out.println("Client closing connection.");
                        break;
                    }
                }
                message.clearMessage();
            }
            Thread.sleep(100);
            echo = "-h";
            if (!listener.confirmEcho(echo)) {
                System.out.println("Problems detected on server side(no echo recieved).");
            }

            this.oos.close();
            this.ois.close();
            this.socket.close();
            //System.out.println("(" + this.dateFormatter.format(m.getTime().getTime()) + ") " + this.userName + " disconnected from channel.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
