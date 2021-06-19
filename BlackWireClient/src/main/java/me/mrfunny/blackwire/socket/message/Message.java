package me.mrfunny.blackwire.socket.message;

import org.apache.commons.codec.binary.Base64;

public class Message {
    private final MessageType messageType;
    private final byte[] content;

    public Message(MessageType messageType, byte[] content) {
        this.messageType = messageType;
        this.content = content;
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
        return Base64.encodeBase64String(content);
    }
}
