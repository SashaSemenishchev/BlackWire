package me.mrfunny.blackwire.server.socket;

import me.mrfunny.blackwire.server.Main;
import me.mrfunny.blackwire.socket.message.Message;
import me.mrfunny.blackwire.socket.message.MessageType;
import me.mrfunny.util.RSAUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class Client extends Thread {

    private final Socket socket;
    private final KeyPair keyPair;

    public Client(Socket socket) throws Exception {
        this.socket = socket;
        keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        setDaemon(true);
    }

    @Override
    public void run() {
        System.out.println("Connecting new client: " + socket.getInetAddress());
        ObjectInputStream in;
        ObjectOutputStream out;
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            Message message;
            while ((message = (Message) in.readObject()) != null){
                if(message.getMessageType().equals(MessageType.ACTION)){
                    if (message.getAction().equals("publickey")) {
                        Message response = new Message(MessageType.ACTION);
                        response.setPublicKey(keyPair.getPublic());
                        out.writeObject(response);
                    }
                }
            }
            in.close();
            out.close();
            socket.close();
        } catch (SocketException socketException){
            if(!socketException.getMessage().contains("Connection reset")){
                socketException.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException exception){
            exception.printStackTrace();
            System.exit(-1);
        }
        Main.connectedClients.remove(this);
    }
}
