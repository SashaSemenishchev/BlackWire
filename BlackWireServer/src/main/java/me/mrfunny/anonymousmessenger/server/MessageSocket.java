package me.mrfunny.anonymousmessenger.server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import me.mrfunny.anonymousmessenger.server.command.CommandInfo;
import me.mrfunny.anonymousmessenger.server.command.CommandManager;
import me.mrfunny.anonymousmessenger.server.db.DbManager;
import me.mrfunny.anonymousmessenger.server.util.RSAUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class MessageSocket {

    private static DbManager dbManager = null;

    static {
        try {
            dbManager = new DbManager();
        } catch (SQLException | ClassNotFoundException exception) {
            exception.printStackTrace();
            System.exit(-1);
        }
    }

    private static JSONParser parser = new JSONParser();
    private static CommandManager commandManager = new CommandManager();
    private static final Algorithm algorithmHS = Algorithm.HMAC256(SuperSecretClass.getServerSecret());
    private static final HashSet<Client> connectedClients = new HashSet<>();

    public MessageSocket(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        commandManager.registerCommand(new CommandInfo("/chat", "Changes chat"), (client) -> {});
        while (true) {
            Client newClient = new Client(serverSocket.accept());
            newClient.start();
            connectedClients.add(newClient);
        }
    }

    public static boolean isClientOnline(String username){
        return connectedClients.parallelStream().anyMatch(client -> client.getUsername().equals(username));
    }

    public static Client getClient(String username){
        Optional<Client> clientOptional = connectedClients.parallelStream().filter(client -> client.getUsername().equals(username)).findFirst();
        return clientOptional.orElse(null);
    }

    public static boolean isClientRegistered(String username) throws SQLException {
        boolean isRegistered;
        PreparedStatement statement = dbManager.getConnection().prepareStatement("SELECT `username` FROM users WHERE `username`=?;");
        statement.setObject(1, username);
        ResultSet rs = statement.executeQuery();
        isRegistered = rs.next();
        statement.close();
        return isRegistered;
    }

    public static void sendMessage(String sender, String receiver, String encryptedMessage) throws SQLException {
        Client senderClient = getClient(sender);
        if(isClientRegistered(receiver)){
            Client clientReceiver = getClient(receiver);
            if(clientReceiver != null){
                HashMap<String, String> payload = new HashMap<>();
                payload.put("sender", sender);
                payload.put("message", encryptedMessage);
                clientReceiver.getOutputStream().println(new JSONObject(payload).toJSONString());
            } else {
                PreparedStatement statement = dbManager.getConnection().prepareStatement("INSERT INTO waitingMessages (sender, receiver, encryptedMessage) VALUES (?,?,?);");
                statement.setObject(1, sender);
                statement.setObject(2, receiver);
                statement.setObject(3, encryptedMessage);
                statement.execute();
                statement.close();
            }
        } else {
            senderClient.out.println("Error: user not found");
        }
    }

    public static class Client extends Thread {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username = null;
        private KeyPair keyPair;

        public String getUsername() {
            return username;
        }

        public Client(Socket socket) {
            this.clientSocket = socket;
            setDaemon(true);
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                keyPair = generator.generateKeyPair();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                try {
                    out.println("Error while generating keypair for you");
                    clientSocket.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }

        public boolean isLoggedIn() {
            return username != null;
        }

        public PrintWriter getOutputStream() {
            return out;
        }

        public BufferedReader getInputStream(){
            return in;
        }

        public void sendMessage(String message){
            getOutputStream().println(message);
        }

        @Override
        public void run() {
            try{
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if(inputLine.trim().equals("")) {
                        continue;
                    }
                    try {
                        JSONObject object = (JSONObject) parser.parse(inputLine);
                        if(object.containsKey("action")){
                            switch (object.get("action").toString()){
                                case "publickey":
                                    out.println(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
                                    break;
                                case "auth":
                                case "authenticate":
                                    try{
                                        String token = RSAUtil.decrypt(keyPair.getPrivate(), object.get("token").toString());
                                        JWTVerifier verifier = JWT.require(algorithmHS).build();
                                        DecodedJWT jwt = verifier.verify(token);
                                        JSONObject loginData = (JSONObject) parser.parse(jwt.getPayload());
                                        username = loginData.get("username").toString();
                                        out.println(RSAUtil.encrypt(RSAUtil.fromStringToPublicKey(object.get("publickey").toString()), jwt.getToken()));
                                    } catch (JWTVerificationException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException exception){
                                        out.println("Failed to authenticate you.");
                                        System.out.println("Failed to authenticate user");
                                        clientSocket.close();
                                    }
                                    break;
                                case "register":
                                    if(isClientRegistered(object.get("username").toString())){
                                        out.println("Error: user with this username exists!");
                                    } else {
                                        PreparedStatement statement1 = dbManager.getConnection().prepareStatement("INSERT INTO `users` (`username`, `password`, `publickey`) VALUES (?, ?, ?);");
                                        statement1.setObject(1, object.get("username").toString());
                                        statement1.setObject(2, DigestUtils.sha256Hex(RSAUtil.decrypt(keyPair.getPrivate(), object.get("password").toString())));
                                        statement1.setObject(3, object.get("publickey").toString());
                                        statement1.execute();
                                        statement1.close();
                                        out.println(RSAUtil.encrypt(RSAUtil.fromStringToPublicKey(object.get("publickey").toString()), JWT.create().withClaim("username", object.get("username").toString()).sign(algorithmHS)));
                                        username = object.get("username").toString();
                                    }
                                    break;
                                case "validateUsername":
                                    PreparedStatement statement2 = dbManager.getConnection().prepareStatement("SELECT * FROM users WHERE `username`=? AND `password`=?;");
                                    statement2.setObject(1, object.get("username").toString());
                                    statement2.setObject(2, DigestUtils.sha256Hex(RSAUtil.decrypt(keyPair.getPrivate(), object.get("password").toString())));
                                    ResultSet rs1 = statement2.executeQuery();
                                    if(rs1.next()){
                                        out.println(RSAUtil.encrypt(RSAUtil.fromStringToPublicKey(object.get("publickey").toString()), JWT.create().withClaim("username", object.get("username").toString()).sign(algorithmHS)));
                                        username = object.get("username").toString();
                                        System.out.println("Authenticated client: " + username);
                                    } else {
                                        out.println("Error: passwords not match");
                                    }
                                    statement2.close();
                                    break;
                                case "sendMessage":
                                    MessageSocket.sendMessage(username, object.get("receiver").toString(), object.get("encryptedMessage").toString());
                                    break;
                                case "checkChat":
                                    PreparedStatement statement = dbManager.getConnection().prepareStatement("SELECT `publickey` FROM users WHERE `username`=?;");
                                    statement.setObject(1, object.get("username").toString());
                                    ResultSet rs = statement.executeQuery();
                                    if(rs.next()){
                                        out.println(rs.getString("publickey"));
                                    } else {
                                        out.println("Error: user not found");
                                    }
                                    statement.close();
                                    break;
                            }
                        }
                    } catch (ParseException exception){
                        if ("/stop".equals(inputLine)) {
                            out.println("Bye");
                            in.close();
                            out.close();
                            clientSocket.close();
                            break;
                        } else if("/help".equals(inputLine)){
                            for(CommandInfo command : commandManager.getCommands().keySet()){
                                sendMessage(command.getCommand() + " - " + command.getDescription());
                            }
                        }
                    } catch (NoSuchPaddingException | NoSuchAlgorithmException | SQLException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
                        e.printStackTrace();
                    }
                }
                in.close();
                out.close();
            } catch (SocketException socketException){
                if(!socketException.getMessage().contains("Connection reset")){
                    socketException.printStackTrace();
                }
            } catch (IOException exception){
                exception.printStackTrace();
                System.exit(-1);
            }
            try {
                in.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            connectedClients.remove(this);
            out.close();
            try {
                clientSocket.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            System.out.println("disconnecting client: " + username);
        }
    }
}
