package com.trinhthanhnam.mysocialapp.model;

public class Chat {
    String message, receiver, sender, timeStamp, type;
    boolean isSeen;

    public Chat() {
    }

    public Chat(String message, String receiver, String sender, String timeStamp, boolean isSeen, String type) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timeStamp = timeStamp;
        this.isSeen = isSeen;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timeStamp;
    }

    public void setTimestamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
