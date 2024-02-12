import org.apache.xmlbeans.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;

public class ServerHandler {
    private static DataBaseHandler dbConnection;
    private static int port;
    private static ArrayList<ClientInfo> clients;

    public ServerHandler(String db_source, int port) throws ClassNotFoundException, SQLException{
        this.dbConnection = new DataBaseHandler(db_source);
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public static class ClientFactory extends Thread{
        private ExecutorService clientPool;
        private ServerSocket server;

        public ClientFactory(int client_number, ServerSocket server_socket){
            this.clientPool = Executors.newFixedThreadPool(client_number);
            this.server = server_socket;
        }

        public void terminate(){
            clientPool.shutdown();
        }

        public void run(){
            try {
                while(!server.isClosed()) {
                    Socket client = server.accept();
                    clientPool.execute(new ClientThread(client));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ClientThread implements Runnable{
        private Socket client;
        private String name;
        public ClientThread(Socket client_socket){
            this.client = client_socket;
        }

        public void run(){
            try {
                System.out.println("Connection accepted.");
                MessageProtocol message = new MessageProtocol();
                MessageProtocol toNotify = new MessageProtocol();
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                DataInputStream in = new DataInputStream(client.getInputStream());

                String entry = in.readUTF();
                message.setMessFromString(entry);
                System.out.println("(" + dateFormatter.format(message.getDate().getTime()) + ") " + message.getUserName() + " connected to channel on port:" + port);
                clients.add(new ClientInfo(message.getUserName(), client, in, out));
                this.name = message.getUserName();

                toNotify.addMessage(message.getDate(), "%c", this.name);
                notifyAllClients(toNotify);

                toNotify.clearMessage();
                message.clearMessage();

                while (!client.isClosed()) {

                    entry = in.readUTF();
                    message.setMessFromString(entry);

                    //System.out.println("Client wrote something.");
                    dbConnection.appendMessage(message.getDate(), message.getUserName(), message.getMessText());

                    if (message.getMessText().equals("-h")) {
                        System.out.println("Client:" + message.getUserName() + " initialized disconnection.");
                        toNotify.addMessage(message.getDate(), "%e" + message.getMessText(), "Server:" + port);
                        out.writeUTF(toNotify.toString());
                        out.flush();
                        Thread.sleep(500);
                        break;
                    } else if (message.getMessText().startsWith("-m ")) {
                        //System.out.println("Client wrote message.");
                        toNotify.addMessage(message.getDate(), message.getMessText().replace("-m ", ""), message.getUserName());
                        notifyAllClients(toNotify);

                        //System.out.println("Server:" + port);
                        toNotify.clearMessage();
                        toNotify.addMessage(message.getDate(), "%e" + message.getMessText(), "Server:" + port);

                        out.writeUTF(toNotify.toString());
                        out.flush();
                    } else {
                        toNotify.addMessage(message.getDate(), "%e" + message.getMessText(), "Server:" + port);
                        out.writeUTF(toNotify.toString());
                        out.flush();
                    }

                    message.clearMessage();
                    toNotify.clearMessage();
                }
                System.out.println("Client disconnected");
                System.out.println("Closing connections & channels.");

                toNotify.clearMessage();
                toNotify.addMessage(message.getDate(), "%d", message.getUserName());
                notifyAllClients(toNotify);

                in.close();
                out.close();
                this.client.close();

                for(int i = 0; i < clients.size(); i++){
                    if(clients.get(i).getName().equals(this.name)){
                        clients.remove(i);
                    }
                }

                System.out.println("Closing connections & channels - DONE.");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void notifyAllClients(MessageProtocol message) throws IOException{
        //System.out.println("Notifyin everyone.");
        if(!clients.isEmpty()) {
            for (int i = 0; i < clients.size(); i++) {
                DataOutputStream out = clients.get(i).getOutput();
                out.writeUTF(message.toString());
                out.flush();
            }
        }
    }
    public static void serverStart(){
        try (ServerSocket server= new ServerSocket(port);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in))){
            MessageProtocol message = new MessageProtocol();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            dbConnection.clearDB();
            dbConnection.createDB();
            dbConnection.appendMessage(0, Calendar.getInstance(), "user-template", "text-template");

            System.out.println("(" + formatter.format(Calendar.getInstance().getTime()) + "):Started channel on port:" + port);

            ClientFactory clientConnection = new ClientFactory(5, server);
            clientConnection.start();

            String clientCommand;
            while(!server.isClosed()){
                if(br.ready()) {
                    clientCommand = br.readLine();
                    if (clientCommand.equals("-h")) {
                        System.out.println("Closing all connections.");
                        message.addMessage(Calendar.getInstance(), "-h", "Server:" + port);
                        notifyAllClients(message);
                        clientConnection.terminate();
                        System.out.println("Client connections closed.");
                        server.close();
                        System.out.println("Server closed.");
                        dbConnection.close();
                        break;
                    } else if (clientCommand.equals("-history")) {
                        System.out.println("Here lies the history of this channel.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
