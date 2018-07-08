package state;

public class Message {
    private final String sender;
    private final String text;
    public Message(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }
    public String toString() { return "<" + sender + ">: " + text; }
}
