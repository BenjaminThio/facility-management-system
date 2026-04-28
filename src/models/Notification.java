package src.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {
    private String content;
    private boolean isRead;
    private String from;
    private String timestamp;

    public Notification(String content, String from) {
        this.content = content;
        this.from = from;
        this.isRead = false;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getContent() { return content; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean isRead) { this.isRead = isRead; }
    public String getFrom() { return from; }
    public String getTimestamp() { return timestamp; }
}