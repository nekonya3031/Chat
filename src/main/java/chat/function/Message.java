package chat.function;

import java.util.Date;

public class Message {
    Long id;
    String text;
    String name;
    Date date;
    int type;

    public Message(Long id, String text, String name, int type) {
        this.id = id;
        this.text = text;
        this.name = name;
        this.type = type;
        this.date = new Date();
    }

    public Message(Long id, String text, String name, int type, Date date) {
        this.id = id;
        this.text = text;
        this.name = name;
        this.type = type;
        this.date = date;
    }

    public Message(String json) {
        Message msg = Version.gson.fromJson(json, Message.class);
        this.type = msg.type;
        this.name = msg.name;
        this.date = msg.date;
        this.id = msg.id;
        this.text = msg.text;
    }

    public String toSend() {
        return Version.gson.toJson(this);
    }

    public Message chatMessage(String text, String author) {
        return new Message(Version.getMessage_id(), text, author, 0);
    }

    public Message systemMessage(String text) {
        return new Message(-1L, text, "SERVER", 1);
    }
}
