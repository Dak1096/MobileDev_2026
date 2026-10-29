package com.example.emailclient;

public class Email {
    private final String sender;
    private final String subject;
    private final String preview;
    private final String date;

    public Email(String sender, String subject, String preview, String date) {
        this.sender = sender;
        this.subject = subject;
        this.preview = preview;
        this.date = date;
    }

    public String getSender()  { return sender; }
    public String getSubject() { return subject; }
    public String getPreview() { return preview; }
    public String getDate()    { return date; }
}