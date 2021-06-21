package me.mrfunny.blackwire.client;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import me.mrfunny.blackwire.socket.message.Message;
import me.mrfunny.blackwire.socket.message.MessageType;
import me.mrfunny.blackwire.util.RSAUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;

public class SocketHandler implements Runnable {
    private final int port;
    private final String host;
    private final HashMap<String, Object> data;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public SocketHandler(String host, int port, HashMap<String, Object> data) throws IOException {
        this.host = host;
        this.port = port;
        this.data = data;
    }

    @Override
    public void run() {
        try {
            Message response = sendPacketMessage(new Message(MessageType.ACTION, "publickey"));
            PublicKey serverPublic = RSAUtil.fromStringToPublicKey(response.getMessage());
            HashMap<String, Object> payload = new HashMap<>();
            boolean usingPassword = false;
            if(data.containsKey("token")){
                payload.put("token", RSAUtil.encrypt(serverPublic, data.get("token").toString()));
            } else if(data.containsKey("password")){
                payload.put("username", data.get("username"));
                payload.put("password", RSAUtil.encrypt(serverPublic, data.get("password").toString()));
                usingPassword = true;
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Password needed");
                    alert.showAndWait();
                });
            }

            Message authMessage;
            if(usingPassword){
                if(data.containsKey("repeatPassword")){
                    authMessage = new Message(MessageType.ACTION, "register", payload);
                } else {
                    authMessage = new Message(MessageType.ACTION, "login", payload);
                }
            } else {
                authMessage = new Message(MessageType.ACTION, "auth", payload);
            }

            Message tokenResponse = sendPacketMessage(authMessage);
            if(tokenResponse.getMessage().toLowerCase().contains("error")){
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(tokenResponse.getMessage());
                    alert.showAndWait();
                });
                return;
            }

            // TODO: Save token, show chat window etc.
            while(socket.isConnected()){
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
            }
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException exception) {
        exception.printStackTrace();
    }
    }

    public void sendMessage(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    public Message sendPacketMessage(Message message) throws IOException, ClassNotFoundException {
        out.writeObject(message);
        out.flush();
        return (Message) in.readObject();
    }
}
