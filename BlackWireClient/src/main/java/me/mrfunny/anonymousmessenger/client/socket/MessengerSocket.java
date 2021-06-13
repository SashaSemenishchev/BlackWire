package me.mrfunny.anonymousmessenger.client.socket;


import me.mrfunny.anonymousmessenger.client.Main;
import me.mrfunny.anonymousmessenger.client.commands.CommandInfo;
import me.mrfunny.anonymousmessenger.client.commands.CommandManager;
import me.mrfunny.anonymousmessenger.client.util.RSAUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;

public class MessengerSocket {
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final CommandManager commandManager = new CommandManager();
    private final JSONParser parser = new JSONParser();
    private boolean loggedIn = false;
    private String chosenChat = "";

    public MessengerSocket(String ip, int port, JSONObject data) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        try {
            // todo: validate chat on server and send ok
            commandManager.registerCommand(new CommandInfo("/chat", "changes chat"), () -> {

            });
            String publicKeyString = sendPacket("{\"action\": \"publickey\"}");
            PublicKey publicKey = RSAUtil.fromStringToPublicKey(publicKeyString);
            HashMap<String, String> payload = new HashMap<>();
            if(data.containsKey("token")){
                payload.put("action", "auth");
                payload.put("token", new String(cipher.doFinal(Main.getProperties().get("token").toString().getBytes(StandardCharsets.UTF_8))));
            } else if(data.containsKey("repeatPassword")) {
                payload.put("action", "register");
                payload.put("username", data.get("username").toString());
                payload.put("password", RSAUtil.encrypt(publicKey, data.get("password").toString()));
            } else {
                payload.put("action", "validateUsername");
                payload.put("username", data.get("username").toString());
                payload.put("password", RSAUtil.encrypt(publicKey, data.get("password").toString()));
            }
            payload.put("publickey", Base64.getEncoder().encodeToString(Main.getPublicKey().getEncoded()));
            String response = sendPacket(new JSONObject(payload).toJSONString()); // all types of response might be token.
            if(response.toLowerCase().contains("error")){
                System.out.println(response);
                System.exit(0);
                return;
            }
            Cipher cipher1 = Cipher.getInstance("RSA");
            cipher1.init(Cipher.DECRYPT_MODE, Main.getPrivateKey());
            Main.getProperties().setProperty("token", new String(cipher1.doFinal(response.getBytes(StandardCharsets.UTF_8))));
            loggedIn = true;
            System.out.println("You logged in! Change chat using /chat username");
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException exception) {
            exception.printStackTrace();
            System.exit(-1);
        }
         //
        Thread thread = new Thread(() -> {
            String inputLine;
            try {
                while((inputLine = in.readLine()) != null){
                    System.out.println(inputLine);
                }
            } catch (IOException exception){
                exception.printStackTrace();
            }
        });
        thread.start();
        while(true){
            System.out.println("> ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String input = reader.readLine();
            if(input.equalsIgnoreCase("/stop")){
                System.out.println(sendPacket("/stop"));
                in.close();
                out.close();
                clientSocket.close();
                System.exit(0);
            }
            if(input.trim().equals("")){
                continue;
            }
            sendMessage(input);
        }
    }

    public String sendPacket(String msg) throws IOException {
        out.println(msg);
        return in.readLine();
    }

    public void sendMessage(String msg) throws IOException {
        out.println(msg);
    }

    public void stopConnection() throws IOException  {
        in.close();
        out.close();
        clientSocket.close();
    }

    public static String readFile(String path) {
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return new String(encoded).replaceAll("\n", "").replaceAll("\r", "");
    }
}
