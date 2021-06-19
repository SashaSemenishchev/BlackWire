package me.mrfunny.blackwire.client;

import me.mrfunny.blackwire.socket.message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketHandler implements Runnable {
    private final int port;
    private final String host;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public SocketHandler(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        while(socket.isConnected()){
            try {
                Message message = (Message) in.readObject();
                switch (message.getMessageType()){

                    case TEXT:
                        break;
                    case FILE:
                        break;
                    case VOICE:
                        break;
                    case ACTION:
                        break;
                }
            } catch (IOException | ClassNotFoundException exception) {
                exception.printStackTrace();
            }
        }
    }
}
