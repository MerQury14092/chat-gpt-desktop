package com.example.gptclient.controllers;

import com.example.gptclient.DTO.Message;
import com.example.gptclient.services.ConfigService;
import com.example.gptclient.services.GptService;
import com.sandec.mdfx.MarkdownView;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import static com.example.gptclient.Project.async;
import static javafx.application.Platform.runLater;

public class MainController {

    @FXML
    private ImageView gpt_icon;

    @FXML
    private AnchorPane gpt_icon_background;

    @FXML
    private TextArea input_field;

    @FXML
    private VBox message_content;

    @FXML
    private ScrollPane message_content_scroll;

    @FXML
    private Label chat_name;

    @FXML
    private Separator sep;

    @FXML
    private ScrollPane chats_content;

    @FXML
    private BorderPane root;

    private Timeline anim;

    @FXML
    private void initialize(){
        KeyValue rotate = new KeyValue(gpt_icon.rotateProperty(), gpt_icon.getRotate()-180);
        KeyFrame frame = new KeyFrame(Duration.millis(1000), rotate);
        anim = new Timeline();
        anim.getKeyFrames().add(frame);
        anim.setCycleCount(Animation.INDEFINITE);

        input_field.setMinWidth(input_field.getPrefWidth());
        message_content.setMinWidth(message_content.getPrefWidth());
        message_content.setMinHeight(message_content.getPrefHeight());
        chats_content.setMinHeight(chats_content.getPrefHeight());
        sep.setMinWidth(sep.getPrefWidth());
        chat_name.setMinWidth(chat_name.getPrefWidth());
        root.widthProperty().addListener(((ignor, oldVal, newVal) -> {
            if(Double.isNaN(newVal.doubleValue()) || newVal.doubleValue() == 600)
                return;
            double delta = newVal.doubleValue() - oldVal.doubleValue();
            input_field.setMinWidth(input_field.getMinWidth()+delta);
            message_content.setMinWidth(message_content.getMinWidth()+delta);
            sep.setMinWidth(sep.getMinWidth()+delta);
            chat_name.setMinWidth(chat_name.getWidth()+delta);
        }));

        root.heightProperty().addListener(((ignor, oldVal, newVal) -> {
            if(Double.isNaN(newVal.doubleValue()) || newVal.doubleValue() == 800)
                return;
            double delta = newVal.doubleValue() - oldVal.doubleValue();
            message_content.setMinHeight(message_content.getMinHeight()+delta);
            chats_content.setMinHeight(chats_content.getMinHeight()+delta);
        }));

        chat_name.setText(ConfigService.getSelected().getName());
        async(this::show_messages_by_current_chat);
    }

    private void show_messages_by_current_chat(){
        for(Message message: ConfigService.getSelected().getMessages()){
            runLater(() -> addMessage(message.getContent(), message.getRole().equals("assistant")));
        }
    }

    @FXML
    void anim_start() {
        KeyValue prefW = new KeyValue(gpt_icon_background.prefWidthProperty(), 100);
        KeyValue prefH = new KeyValue(gpt_icon_background.prefHeightProperty(), 100);
        KeyValue layX = new KeyValue(gpt_icon_background.layoutXProperty(), 32 - 5);
        KeyValue layY = new KeyValue(gpt_icon_background.layoutYProperty(), 14 - 5);
        KeyValue x = new KeyValue(gpt_icon.layoutXProperty(), 5);
        KeyValue y = new KeyValue(gpt_icon.layoutYProperty(), 5);
        KeyFrame frame = new KeyFrame(Duration.millis(100), prefH, prefW, layX, layY, x, y);
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(frame);
        timeline.play();
        anim.play();
    }

    @FXML
    void anim_stop() {
        anim.pause();
        KeyValue prefW = new KeyValue(gpt_icon_background.prefWidthProperty(), 90);
        KeyValue prefH = new KeyValue(gpt_icon_background.prefHeightProperty(), 90);
        KeyValue layX = new KeyValue(gpt_icon_background.layoutXProperty(), 32);
        KeyValue layY = new KeyValue(gpt_icon_background.layoutYProperty(), 14);
        KeyValue x = new KeyValue(gpt_icon.layoutXProperty(), 0);
        KeyValue y = new KeyValue(gpt_icon.layoutYProperty(), 0);
        KeyFrame frame = new KeyFrame(Duration.millis(100), prefH, prefW, layX, layY, x, y);
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    void send_message(KeyEvent event){
        if(!event.getCode().equals(KeyCode.ENTER)) {
            return;
        }
        String prompt = input_field.getText().trim();
        input_field.setText("");
        if(prompt.isEmpty()) {
            return;
        }
        anim_start();
        addMessage(prompt, false);
        async(() -> {
            ConfigService.saveMessage(true, prompt);
            InputStream input = GptService.answer_stream(ConfigService.getSelected().getMessages().toArray(new Message[0]));
            runLater(() -> addStreamingMessage(input));
        });
    }

    private void addMessage(String str, boolean left){
        AnchorPane pane = new AnchorPane();
        pane.setMinWidth(message_content.getMinWidth());
        MarkdownView area = new MarkdownView(){
            @Override
            protected List<String> getDefaultStylesheets() {
                return List.of("/com/example/gptclient/main.css");
            }
        };
        area.setMdString(str);
        area.setLayoutX(200);
        area.setOpacity(0);
        area.setMinHeight(20);
        area.setMaxWidth(message_content.getWidth()-60);
        area.getStyleClass().add("message");
        AnchorPane.setTopAnchor(area, 6.0);
        AnchorPane.setBottomAnchor(area, 6.0);
        if (left)
            AnchorPane.setLeftAnchor(area, 10.0);
        else
            AnchorPane.setRightAnchor(area, 10.0);
        pane.getChildren().add(area);


        Timeline messageTimeline = new Timeline();
        KeyValue opacity = new KeyValue(area.opacityProperty(), 1.0);
        KeyValue layoutX = new KeyValue(area.layoutXProperty(), 0);
        KeyFrame frame = new KeyFrame(Duration.millis(200), opacity, layoutX);
        messageTimeline.getKeyFrames().add(frame);


        message_content.getChildren().add(pane);
        messageTimeline.play();
        async(() -> runLater(() -> message_content_scroll.setVvalue(1.0)));
    }

    /**
     * stop word: \n[DONE]
     */
    private void addStreamingMessage(InputStream source){
        AnchorPane pane = new AnchorPane();
        pane.setMinWidth(message_content.getMinWidth());
        MarkdownView area = new MarkdownView();
        area.setLayoutX(200);
        area.setOpacity(0);
        area.setMinHeight(20);
        area.setMaxWidth(message_content.getWidth()-60);
        area.getStyleClass().add("message");
        AnchorPane.setTopAnchor(area, 6.0);
        AnchorPane.setBottomAnchor(area, 6.0);
        AnchorPane.setLeftAnchor(area, 10.0);
        pane.getChildren().add(area);


        Timeline messageTimeline = new Timeline();
        KeyValue opacity = new KeyValue(area.opacityProperty(), 1.0);
        KeyValue layoutX = new KeyValue(area.layoutXProperty(), 0);
        KeyFrame frame = new KeyFrame(Duration.millis(200), opacity, layoutX);
        messageTimeline.getKeyFrames().add(frame);


        message_content.getChildren().add(pane);
        messageTimeline.play();

        async(() -> {
            Scanner sc = new Scanner(source);
            for (;;){
                String stroke = sc.nextLine();
                if (stroke.toUpperCase().contains("[DONE]")) {
                    runLater(this::anim_stop);
                    runLater(() -> message_content_scroll.setVvalue(1.0));
                    break;
                }
                if(stroke.isEmpty())
                    continue;
                runLater(() -> area.setMdString(area.getMdString()+getContent(stroke.replace("data: ", ""))));
                runLater(() -> message_content_scroll.setVvalue(1.0));
            }
            ConfigService.saveMessage(false, area.getMdString());
            sc.close();
        });
    }

    private String getContent(String str){
        try {
            return new JSONObject(str)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("delta")
                    .getString("content");
        } catch (JSONException e){
            return "";
        }
    }

}
