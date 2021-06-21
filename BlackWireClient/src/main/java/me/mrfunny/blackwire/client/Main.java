package me.mrfunny.blackwire.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

public class Main extends Application {

    private static Stage staticStage;
    private static Properties properties = new Properties();
    private static String basePath = System.getProperty("user.home") + File.separator + ".blackwire";
    private static boolean loadIntoSetup = false;
    private static boolean askServer = false;
    private static String errorMessage = null;
    private static String host;
    private static String id;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;

    public static String getHost() {
        return host;
    }

    public static String getId() {
        return id;
    }

    public static PublicKey getPublicKey() {
        return publicKey;
    }

    public static PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static Properties getProperties() {
        return properties;
    }

    public static String getBasePath() {
        return basePath;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("BlackWire");
        // Check and show user any error
        if(errorMessage != null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error on starting application");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            System.exit(-1);
            return;
        }

        // Show dialog for entering host and port of message server
        if(askServer){
            TextInputDialog textInputDialog = new TextInputDialog();
            textInputDialog.setHeaderText("Host value not found");
            textInputDialog.setContentText("Enter host:port of message server: ");
            Optional<String> resultOptional = textInputDialog.showAndWait();
            if(resultOptional.isPresent()){
                host = resultOptional.get();
                id = host.replaceAll(":", "").replaceAll("\\.", "_");
            } else {
                System.exit(0);
                return;
            }
        }

        // TODO check for private and public keys.

        // Show dialog, that asks user for login or registration
        if(loadIntoSetup){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            ButtonType login = new ButtonType("Login", ButtonBar.ButtonData.YES);
            ButtonType register = new ButtonType("Register", ButtonBar.ButtonData.NO);
            ButtonType cancel = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(login, register, cancel);
            Optional<ButtonType> choiceOptional = alert.showAndWait();
            if(choiceOptional.isPresent()){
                ButtonType choice = choiceOptional.get();
                if(choice == ButtonType.YES){
                    stage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("login.fxml")))));
                } else if(choice == ButtonType.NO){
                    stage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("register")))));
                } else {
                    System.exit(0);
                    return;
                }
            } else {
                System.exit(0);
                return;
            }

        } else {
            stage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("main.fxml")))));
        }

        stage.show();
        staticStage = stage;
    }

    public static Stage getStaticStage() {
        return staticStage;
    }

    public static void main(String[] args) {
        try {
            File propertiesFile = new File(basePath + File.separator + "client.properties");
            if (!propertiesFile.exists()) {
                if (!propertiesFile.createNewFile()) {
                    errorMessage = "Cannot create properties file. Exiting";
                }
                loadIntoSetup = true;
                askServer = true;
            }
            FileInputStream fileInputStream = new FileInputStream(propertiesFile);
            properties.load(fileInputStream);
            fileInputStream.close();
            if(properties.getProperty("host") == null){
                askServer = true;
            }
        } catch (Exception exception){
            errorMessage = exception.getMessage();
        }
        launch(args);
    }
}
