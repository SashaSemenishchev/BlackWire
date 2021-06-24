package me.mrfunny.blackwire.socket.message;

import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;

public class Message {
    private final MessageType messageType;
    private byte[] content = new byte[1024];
    private HashMap<String, Object> additionalData = new HashMap<>();
    private String action;
    private String message;
    private String encryptionKey;
    private PublicKey publicKey;

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public Message(MessageType messageType, byte[] content) {
        this.messageType = messageType;
        this.content = content;
    }

    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public Message(MessageType messageType, HashMap<String, Object> data) {
        this.messageType = messageType;
        this.additionalData = data;
    }

    public Message(MessageType messageType, String action) {
        this.messageType = messageType;
        this.action = action;
    }

    public Message(MessageType messageType, String action, HashMap<String, Object> data) {
        this.messageType = messageType;
        this.action = action;
        this.additionalData = data;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public HashMap<String, Object> getData() {
        return additionalData;
    }

    public void addData(String key, Object data){
        additionalData.put(key, data);
    }

    public void removeData(String key){
        additionalData.remove(key);
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public byte[] getContent() {
        return content;
    }

    public String getStringContent(){
        return new String(content);
    }

    public String getBase64StringContent(){
        return Base64.getEncoder().encodeToString(content);
    }

    public String getAction() {
        return action;
    }
}
