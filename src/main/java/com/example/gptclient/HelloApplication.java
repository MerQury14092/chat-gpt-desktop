package com.example.gptclient;

import com.example.gptclient.DTO.Message;
import com.example.gptclient.services.GptService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(900);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}