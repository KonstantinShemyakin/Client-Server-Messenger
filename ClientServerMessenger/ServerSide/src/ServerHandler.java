import org.apache.xmlbeans.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ServerHandler {
    public static void serverStart(int port) throws InterruptedException{
        try (ServerSocket server= new ServerSocket(3345)){
            MessageProtocol message = new MessageProtocol();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DataBaseHandler db = new DataBaseHandler("C:\\Projects\\ClientServerMessenger\\ServerSide\\instances\\MessageDB.db");

            //db.appendMessage(0, Calendar.getInstance(), "user-template", "text-template");

            System.out.println("(" + formatter.format(Calendar.getInstance().getTime()) + ") channel started on port:" + port);

            Socket client = server.accept();

            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            DataInputStream in = new DataInputStream(client.getInputStream());

            String entry = in.readUTF();
            message.setMessFromString(entry);
            System.out.println("(" + formatter.format(message.getDate().getTime()) + ") " + message.getUserName() + " connected to channel.");
            message.clearMessage();

            while(!client.isClosed()){

                entry = in.readUTF();
                message.setMessFromString(entry);

                if(message.getMessText().equalsIgnoreCase("-h")){
                    System.out.println("Client initialize connections suicide ...");
                    out.writeUTF("Server reply - " + message.getMessText() + " - OK");
                    out.flush();
                    db.close();
                    Thread.sleep(300);
                    break;
                }

                System.out.println("(" + formatter.format(message.getDate().getTime()) + ") " + message.getUserName() + " init:" + message.getInitName() + " : " + message.getMessText());
                System.out.println(message.toString());
                db.appendMessage(message.getDate(), message.getUserName(), message.getMessText());

                out.writeUTF("Server reply - " + message.getMessText() + " - OK");
                out.flush();
            }
            System.out.println("Client disconnected");
            System.out.println("Closing connections & channels.");

            in.close();
            out.close();
            client.close();

            System.out.println("Closing connections & channels - DONE.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
