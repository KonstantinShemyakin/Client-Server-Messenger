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

    public void addMessage(Calendar date, String message, String user){
        MessageDocument.Message mes = this.messDoc.addNewMessage();
        Header h = mes.addNewHeader();
        Body b = mes.addNewBody();

        h.setTime(date);
        b.setText(message);
        b.setUser(user);
    }

    public String getMessage(){
        return messDoc.toString();
    }

    public void clearMessage(){
        this.messDoc = MessageDocument.Factory.newInstance();
    }

    public void setMessFromString(String messages) throws XmlException{
        this.messDoc = MessageDocument.Factory.parse(messages);
    }

    public void setMessFromMI(MessageInfo message){
        this.messDoc = MessageDocument.Factory.newInstance();
        this.addMessage(message.getTime(), message.getText(), message.getUser());
    }

    public Calendar getDate(){
        MessageDocument.Message message = this.messDoc.getMessage();
        return message.getHeader().getTime();
    }

    public String getMessText(){
        MessageDocument.Message message = this.messDoc.getMessage();
        return message.getBody().getText();
    }

    public String getUserName(){
        MessageDocument.Message message = this.messDoc.getMessage();
        return message.getBody().getUser();
    }

    public void setDate(Calendar date){
        MessageDocument.Message message = this.messDoc.getMessage();
        Header h = message.getHeader();
        h.setTime(date);
    }

    public void setMessText(String text){
        MessageDocument.Message message = this.messDoc.getMessage();
        Body b = message.getBody();
        b.setText(text);
    }

    public void setUserName(String user){
        MessageDocument.Message message = this.messDoc.getMessage();
        Body b = message.getBody();
        b.setUser(user);
    }

    public String toString(){
        return this.messDoc.toString();
    }
}
