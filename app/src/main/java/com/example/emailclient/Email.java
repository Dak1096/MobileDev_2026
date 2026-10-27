package com.example.emailclient;

public class Email {
    private String id;
    private String sender;
    private String subject;
    private String body;
    private String date;
    private String folder = "INBOX";
    private boolean read;
    private boolean starred;
    private long sortKey;

    public Email() {
    }

    public Email(String sender, String subject, String body, String date) {
        this.sender = sender;
        this.subject = subject;
        this.body = body;
        this.date = date;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getFolder() { return folder; }
    public void setFolder(String folder) { this.folder = folder; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public boolean isStarred() { return starred; }
    public void setStarred(boolean starred) { this.starred = starred; }

    public long getSortKey() { return sortKey; }
    public void setSortKey(long sortKey) { this.sortKey = sortKey; }

    public String getPreview() {
        return body == null ? "" : body.replace('\n', ' ');
    }

    public boolean contentEquals(Email o) {
        if (o == null) return false;
        return read == o.read && starred == o.starred && eq(folder, o.folder)
                && eq(sender, o.sender) && eq(subject, o.subject) && eq(body, o.body) && eq(date, o.date);
    }

    private static boolean eq(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
