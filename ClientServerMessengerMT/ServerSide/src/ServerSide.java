import java.io.*;
import java.sql.SQLException;

public class ServerSide {
    public static void main(String args[]) throws InterruptedException, ClassNotFoundException, SQLException {
        ServerHandler server = new ServerHandler("C:\\Projects\\ClientServerMessengerMT\\ServerSide\\instances\\MessageDB.db", 3345);

        server.serverStart();
    }
}
