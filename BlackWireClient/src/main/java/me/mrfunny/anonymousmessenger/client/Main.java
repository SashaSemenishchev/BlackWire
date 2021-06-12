package me.mrfunny.anonymousmessenger.client;

import me.mrfunny.anonymousmessenger.client.socket.MessengerSocket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import static me.mrfunny.anonymousmessenger.client.socket.MessengerSocket.readFile;

public class Main {
    private final static JSONParser parser = new JSONParser();
    private static Properties properties;
    private static KeyPairGenerator generator;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        File propertiesFile = new File(System.getProperty("user.home") + File.separator + ".blackwire" + File.separator + "client.properties");
        File propertiesFolder = new File(System.getProperty("user.home") + File.separator + ".blackwire");
        propertiesFolder.mkdirs();
        //        if(true){
//            return;
//        }
        String host = null;
        String token = null;
        if(!propertiesFile.exists()){
            if(!propertiesFile.createNewFile()){
                System.out.println("Cannot create properties file. Create it by yourself: " + propertiesFile.getAbsolutePath());
                System.exit(-1);
                return;
            }
            token = null;
        }
        FileInputStream is = new FileInputStream(propertiesFile);
        System.out.println(is);
        properties.load(is);
        Console console = System.console();
        if(properties.contains("token")){
            token = properties.getProperty("token");
        }
        if(properties.contains("host")){
            host = properties.getProperty("host");
        }
        if(host == null){
            System.out.println("Enter host:port of message server: ");
            properties.setProperty("host", console.readLine());
        }
        if(properties.contains("publicKey") && properties.contains("privateKey")){
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(properties.getProperty("publicKey").getBytes(StandardCharsets.UTF_8))));
            privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(properties.getProperty("privateKey").getBytes(StandardCharsets.UTF_8))));
        } else {
            KeyPair keyPair = generator.generateKeyPair();
            properties.setProperty("publicKey", new String(keyPair.getPublic().getEncoded(), StandardCharsets.UTF_8));
            properties.setProperty("privateKey", new String(keyPair.getPrivate().getEncoded(), StandardCharsets.UTF_8));
        }
        PrintWriter writer = new PrintWriter(propertiesFile);
        properties.store(writer, null);
        try {
            HashMap<String, String> payload = new HashMap<>();
            if(token == null){
                String choice = console.readLine();
                System.out.println("Enter your username (username starts with @): ");
                payload.put("username", console.readLine().trim().intern());
                System.out.println("Enter your password: ");
                String password = new String(console.readPassword());
                payload.put("password", password);
                if(!choice.startsWith("l")){
                    String repeatPassword;
                    do {
                        System.out.println("Repeat password: ");
                        repeatPassword = new String(console.readPassword());
                    } while (!repeatPassword.equals(password));
                    payload.put("repeatPassword", repeatPassword);
                }
            } else {
                payload.put("token", token);
            }
            MessengerSocket socket = new MessengerSocket("localhost", 6666, new JSONObject(payload));
            socket.stopConnection();
        } catch (IOException exception) {
            if(exception.getMessage().toLowerCase().contains("connection reset")){
                System.out.println("Connection with server lost.");
                System.exit(0);
            } else if(exception.getMessage().toLowerCase().contains("connection refused")){
                System.out.println("Couldn't connect to server with ip: ");
                System.exit(0);
            }
            else {
                exception.printStackTrace();
            }
        } catch (Exception exception){
            exception.printStackTrace();
            System.exit(-1);
        }
    }

    public static JSONParser getParser() {
        return parser;
    }

    public static Properties getProperties() {
        return properties;
    }

    public static PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static PublicKey getPublicKey() {
        return publicKey;
    }
}
