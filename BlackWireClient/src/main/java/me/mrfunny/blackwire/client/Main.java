package me.mrfunny.blackwire.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("BlackWire");
        stage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("main.fxml")))));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
