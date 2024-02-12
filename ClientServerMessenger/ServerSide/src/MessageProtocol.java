import java.util.*;
import java.io.*;
import org.apache.xmlbeans.*;
import org.openuri.easypo.*;
import java.text.*;

public class MessageProtocol {
    private MessageDocument messDoc;

    public MessageProtocol(){
        this.messDoc = MessageDocument.Factory.newInstance();
    }

    public void writeMessage(Calendar date, String message, String user, String init){
        MessageDocument.Message mes = this.messDoc.addNewMessage();
        Header h = mes.addNewHeader();
        Body b = mes.addNewBody();

        h.setTime(date);
        b.setText(message);
        b.setUser(user);
        b.setInitName(init);
    }

    public void writeMessage(){
        MessageDocument.Message mes = this.messDoc.addNewMessage();
        Header h = mes.addNewHeader();
        Body b = mes.addNewBody();

        h.setTime(Calendar.getInstance());
        b.setText("text-template");
        b.setUser("user-template");
        b.setInitName("init-template");
    }

    public String getMessage(){
        return messDoc.toString();
    }

    public void clearMessage(){
        this.messDoc = MessageDocument.Factory.newInstance();
    }

    public void setMessFromString(String messages) throws XmlException{
        messDoc = MessageDocument.Factory.parse(messages);
    }

    public Calendar getDate(){
        MessageDocument.Message message = messDoc.getMessage();
        return message.getHeader().getTime();
    }

    public String getMessText(){
        MessageDocument.Message message = messDoc.getMessage();
        return message.getBody().getText();
    }

    public String getUserName(){
        MessageDocument.Message message = messDoc.getMessage();
        return message.getBody().getUser();
    }

    public String getInitName(){
        MessageDocument.Message message = messDoc.getMessage();
        return message.getBody().getInitName();
    }

    public String toString(){
        return messDoc.toString();
    }
}
