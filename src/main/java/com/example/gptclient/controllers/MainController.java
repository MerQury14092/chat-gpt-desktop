package com.example.gptclient.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

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
    private ScrollPane chats_content;

    @FXML
    private BorderPane root;

    private Timeline anim;

    @FXML
    private void initialize(){
        KeyValue rotate = new KeyValue(gpt_icon.rotateProperty(), gpt_icon.getRotate()-180);
        KeyFrame frame = new KeyFrame(Duration.millis(1300), rotate);
        anim = new Timeline();
        anim.getKeyFrames().add(frame);
        anim.setCycleCount(Animation.INDEFINITE);

        input_field.setMinWidth(input_field.getPrefWidth());
        message_content.setMinWidth(message_content.getPrefWidth());
        message_content.setMinHeight(message_content.getPrefHeight());
        chats_content.setMinHeight(chats_content.getPrefHeight());
        root.widthProperty().addListener(((ignor, oldVal, newVal) -> {
            if(Double.isNaN(newVal.doubleValue()) || newVal.doubleValue() == 600)
                return;
            double delta = newVal.doubleValue() - oldVal.doubleValue();
            input_field.setMinWidth(input_field.getMinWidth()+delta);
            message_content.setMinWidth(message_content.getMinWidth()+delta);
        }));

        root.heightProperty().addListener(((ignor, oldVal, newVal) -> {
            if(Double.isNaN(newVal.doubleValue()) || newVal.doubleValue() == 800)
                return;
            double delta = newVal.doubleValue() - oldVal.doubleValue();
            message_content.setMinHeight(message_content.getMinHeight()+delta);
            chats_content.setMinHeight(chats_content.getMinHeight()+delta);
        }));
    }

    @FXML
    void anim_start(MouseEvent event) {
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
    void anim_stop(MouseEvent event) {
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

}
