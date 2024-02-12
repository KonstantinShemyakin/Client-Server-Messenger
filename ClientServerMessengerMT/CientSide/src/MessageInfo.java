import java.util.Calendar;

public class MessageInfo{
    private Calendar time;
    private String user;
    private String text;

    public MessageInfo(Calendar time, String user, String text){
        this.time = time;
        this.user = user;
        this.text = text;
    }

    public MessageInfo(){
        this.time = Calendar.getInstance();
        this.user = "user-template";
        this.text = "text-template";
    }

    public boolean equals(MessageInfo message) {
        return message.text.equals(this.text) && message.user.equals(this.user) && message.time.equals(this.time);
    }

    public void setTime(Calendar time){
        this.time = time;
    }

    public void setUser(String user){
        this.user = user;
    }

    public void setText(String text){
        this.text = text;
    }

    public Calendar getTime(){
        return this.time;
    }

    public String getUser(){
        return this.user;
    }

    public String getText(){
        return this.text;
    }
}
