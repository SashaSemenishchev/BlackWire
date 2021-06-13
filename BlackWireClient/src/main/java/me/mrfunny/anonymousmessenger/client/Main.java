package me.mrfunny.anonymousmessenger.client;

import me.mrfunny.anonymousmessenger.client.socket.MessengerSocket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static me.mrfunny.anonymousmessenger.client.socket.MessengerSocket.readFile;

public class Main {
    private final static JSONParser parser = new JSONParser();
    private static final Properties properties = new Properties();
    private static KeyPairGenerator generator;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;
    private static File mainFolder = new File(System.getProperty("user.home") + File.separator + ".blackwire");

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        File propertiesFile = new File(System.getProperty("user.home") + File.separator + ".blackwire" + File.separator + "client.properties");
        if(!mainFolder.exists()){
            mainFolder.mkdirs();
        }
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
        File privateKeyFile = new File(mainFolder.getAbsolutePath() + File.separator + "private.dem");
        File publicKeyFile = new File(mainFolder.getAbsolutePath() + File.separator + "public.dem");
        if(privateKeyFile.exists() && publicKeyFile.exists()){
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(Files.readAllBytes(publicKeyFile.toPath()));
            publicKey = keyFactory.generatePublic(publicSpec);
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(Files.readAllBytes(privateKeyFile.toPath()));
            privateKey = keyFactory.generatePrivate(privateSpec);
        } else {
            KeyPair keyPair = generator.generateKeyPair();
            try (FileOutputStream fos = new FileOutputStream(privateKeyFile)){
                fos.write(keyPair.getPrivate().getEncoded());
            }
            try (FileOutputStream fos = new FileOutputStream(publicKeyFile)){
                fos.write(keyPair.getPublic().getEncoded());
            }
        }

        try {
            HashMap<String, String> payload = new HashMap<>();
            if(token == null){
                String choice = console.readLine();
                System.out.println("Enter your username (registration): ");
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
            PrintWriter writer = new PrintWriter(propertiesFile);
            properties.store(writer, null);
            writer.close();
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
