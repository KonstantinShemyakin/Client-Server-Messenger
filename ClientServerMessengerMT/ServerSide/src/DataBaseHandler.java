import java.sql.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class DataBaseHandler {
    private Connection conn;
    private Statement statmt;
    private ResultSet resSet;
    private ArrayList<MessageInfo> messageQueue;

    public DataBaseHandler(String source) throws ClassNotFoundException, SQLException{
        this.conn = null;
        Class.forName("org.sqlite.JDBC");
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + source);
        this.statmt = this.conn.createStatement();
        this.messageQueue = new ArrayList<>(1);

        //System.out.println("База подключена!");
    }

    public void createDB() throws SQLException{
        this.statmt.execute("CREATE TABLE if not exists 'messages' " +
                "('ID' INTEGER, 'TIME' DATETIME, 'USER' STRING, 'TEXT' STRING);");
    }

    public void appendMessage(int id, Calendar time, String user, String text) throws SQLException{
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        this.statmt.execute("INSERT INTO 'messages' ('ID', 'TIME', 'USER', 'TEXT') " +
                "VALUES (" + id + ", '" + formatter.format(time.getTime()) + "', '" + user + "', '" + text + "'); ");
    }

    public void appendMessage(Calendar time, String user, String text) throws SQLException{
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        this.resSet = statmt.executeQuery("SELECT ID FROM messages");
        int id = 0;
        while(resSet.next()){
            id = resSet.getInt("ID");
        }
        id += 1;

        this.statmt.execute("INSERT INTO 'messages' ('ID', 'TIME', 'USER', 'TEXT') " +
                "VALUES (" + id + ", '" + formatter.format(time.getTime()) + "', '" + user + "', '" + text + "'); ");
    }

    public void addMessageToQue(Calendar time, String user, String text){
        MessageInfo message = new MessageInfo(time, user, text);
        this.messageQueue.add(message);
    }

    public void pushMessageQue() throws SQLException{
        if(!this.messageQueue.isEmpty()) {
            MessageInfo message = this.messageQueue.get(0);
            this.messageQueue.remove(0);
            this.appendMessage(message.getTime(), message.getUser(), message.getText());
        }
    }

    public void printDB()  throws SQLException, ParseException{
        this.resSet = this.statmt.executeQuery("SELECT * FROM messages");

        while(resSet.next())
        {
            int id = this.resSet.getInt("ID");
            Calendar time = new GregorianCalendar();
            String time_date = this.resSet.getString("TIME");
            String user = this.resSet.getString("USER");
            String text = this.resSet.getString("TEXT");

            DateFormat format_date = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat format_time = new SimpleDateFormat("HH:mm:ss");
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            time.setTime(formatter.parse(time_date));
            System.out.println( "Id = " + id);
            System.out.println( "Date = " + format_date.format(time.getTime()) + "\nTime = " + format_time.format(time.getTime()));
            System.out.println( "Message = " + text);
            System.out.println( "Client = " + user);
            System.out.println();
        }
    }

    public void clearDB() throws SQLException{
        statmt.execute("DROP TABLE IF EXISTS messages");
    }

    public void close() throws SQLException{
        this.conn.close();
        this.statmt.close();
        this.resSet.close();
    }
}
