package me.mrfunny.blackwire.client;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import me.mrfunny.blackwire.socket.message.Message;
import me.mrfunny.blackwire.socket.message.MessageType;
import me.mrfunny.util.AESUtil;
import me.mrfunny.util.NotFileException;
import me.mrfunny.util.RSAUtil;
import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class SocketHandler implements Runnable {

    private HashMap<String, Object> data;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public SocketHandler(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public static String shuffleString(String string) {
        List<String> letters = Arrays.asList(string.split(""));
        Collections.shuffle(letters);
        StringBuilder builder = new StringBuilder();
        for (String letter : letters) {
            builder.append(letter);
        }
        return builder.toString();
    }

    public void sendMessage(String message, String receiver) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidKeySpecException {
        this.sendMessage(message.getBytes(StandardCharsets.UTF_8), receiver);
    }

    public void sendMessage(byte[] message, String receiver) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidKeySpecException {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("username", receiver);
        Message response = sendPacketMessage(new Message(MessageType.ACTION, "publickey", payload));
        Message messageToSend = new Message(MessageType.TEXT);
        String password = shuffleString(UUID.randomUUID().toString() + RandomStringUtils.randomAlphanumeric(64));
        SecretKey secretKey = AESUtil.getKeyFromPassword(password);
        messageToSend.setMessage(AESUtil.encrypt(message, secretKey));
        messageToSend.setEncryptionKey(RSAUtil.encrypt(response.getPublicKey(), password));
        out.writeObject(messageToSend);
    }


    @Override
    public void run() {
        try {
            Message response = sendPacketMessage(new Message(MessageType.ACTION, "publickey"));
            PublicKey serverPublic = response.getPublicKey();
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
                System.exit(-1);
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
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException exception) {
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
