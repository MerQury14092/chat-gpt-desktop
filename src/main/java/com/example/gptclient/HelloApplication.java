package com.example.gptclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        setUserAgentStylesheet("MODENA TOUCH");
        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("/com/example/gptclient/main.fxml")
        );
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                HelloApplication.class.getResource("/com/example/gptclient/main.css").toExternalForm()
        );

        stage.setTitle("ChatGPT");
        //stage.setResizable(false);
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(900);
        scene.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode().equals(KeyCode.F1))
                System.out.println("Справка");
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}