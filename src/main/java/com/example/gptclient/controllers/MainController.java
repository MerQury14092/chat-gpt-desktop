package com.example.gptclient.controllers;

import com.example.gptclient.DTO.Chat;
import com.example.gptclient.DTO.Message;
import com.example.gptclient.services.ConfigService;
import com.example.gptclient.services.GptService;
import com.sandec.mdfx.MarkdownView;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
    private VBox chats_content;

    @FXML
    private ScrollPane message_content_scroll;

    @FXML
    private Label chat_name;

    @FXML
    private Separator sep;

    @FXML
    private ScrollPane chats_content_scroll;

    @FXML
    private BorderPane root;

    private Timeline anim;
    private boolean isTyping;

    @FXML
    private void initialize(){
        isTyping = false;
        KeyValue rotate = new KeyValue(gpt_icon.rotateProperty(), gpt_icon.getRotate()-180);
        KeyFrame frame = new KeyFrame(Duration.millis(1000), rotate);
        anim = new Timeline();
        anim.getKeyFrames().add(frame);
        anim.setCycleCount(Animation.INDEFINITE);

        input_field.setMinWidth(input_field.getPrefWidth());
        message_content.setMinWidth(message_content.getPrefWidth());
        message_content.setMinHeight(message_content.getPrefHeight());
        chats_content_scroll.setMinHeight(chats_content_scroll.getPrefHeight());
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
            chats_content_scroll.setMinHeight(chats_content_scroll.getMinHeight()+delta);
        }));

        chat_name.setText(ConfigService.getSelected().getName());
        async(() -> {
            Chat[] chats = ConfigService.getAll();
            for(Chat chat : chats)
                runLater(() -> addChat(chat));
        });
        async(this::show_messages_by_current_chat);
    }

    private void show_messages_by_current_chat(){
        message_content.getChildren().clear();
        runLater(() -> chat_name.setText(ConfigService.getSelected().getName()));
        for(Message message: ConfigService.getSelected().getMessages()){
            if(message.getRole().equals("system"))
                continue;
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
        isTyping = true;
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
        isTyping = false;
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

    @FXML
    private void interrupt_generating(){
        anim_stop();
        isTyping = false;
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

        assignContextMenuForMessage(area);
        message_content.getChildren().add(pane);
        messageTimeline.play();
        async(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            runLater(() -> message_content_scroll.setVvalue(1.0));
        });
    }

    private void assignContextMenuForMessage(Node pane){
        ContextMenu menu = new ContextMenu();
        MenuItem delete = new MenuItem("delete");
        MenuItem edit = new MenuItem("edit");
        MenuItem showInfo = new MenuItem("show more info");
        menu.getItems().addAll(delete, edit, showInfo);
        pane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(!event.getButton().equals(MouseButton.SECONDARY))
                return;
            menu.show(pane, event.getScreenX(), event.getScreenY());
        });
    }

    /**
     * stop word: \n[DONE]
     */
    private void addStreamingMessage(InputStream source){
        AnchorPane pane = new AnchorPane();
        pane.setMinWidth(message_content.getMinWidth());
        MarkdownView area = new MarkdownView(){
            @Override
            protected List<String> getDefaultStylesheets() {
                return List.of("/com/example/gptclient/main.css");
            }
        };
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

        assignContextMenuForMessage(area);
        message_content.getChildren().add(pane);
        messageTimeline.play();

        async(() -> {
            Scanner sc = new Scanner(source);
            while (isTyping) {
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

    @FXML
    private void createNewChat(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Creating new conversation...");
        VBox box = new VBox();
        alert.setContentText("Пока недоступно");
        alert.showAndWait();
    }

    private void changeSelectionChatView(String chatId){
        for(Node node: chats_content.getChildren()){
            ChatView pane = (ChatView) ((AnchorPane) node).getChildren().get(0);
            if(pane.getStyleClass().size() > 1){
                pane.getStyleClass().remove(1);
            }
            if(pane.chatId.equals(chatId))
                pane.getStyleClass().add("selected-chat-view");
        }
    }

    private class ChatView extends AnchorPane{
        public String chatId;
        public ChatView(Chat chat){
            chatId = chat.getId();
            this.setMinWidth(140);
            this.setMaxWidth(140);
            this.getStyleClass().add("chat-view");
            ImageView preview = new ImageView();
            async(() -> {
                try {
                    var ref = new Object() {
                        Image img = new Image(URI.create(chat.getUriToImage()).toURL().openStream());
                    };
                    if(ref.img.getWidth()/ ref.img.getHeight() != 1){
                        int w = (int) Math.min(ref.img.getHeight(), ref.img.getWidth());
                        PixelReader reader = ref.img.getPixelReader();
                        ref.img = new WritableImage(reader,(int) (ref.img.getWidth()/2-w/2),(int) (ref.img.getHeight()/2-w/2),w,w);
                    }
                    runLater(() -> preview.setImage(ref.img));
                } catch (Exception e) {
                    System.out.println(STR."\{e.getClass()}: \{e.getMessage()}");
                    runLater(() -> preview.setImage(new Image(MainController.class.getResourceAsStream("/com/example/gptclient/icons/chat.png"))));
                }
            });
            preview.setFitWidth(50);
            preview.setFitHeight(50);
            preview.setLayoutX(10);
            preview.setLayoutY(10);
            Rectangle rect = new Rectangle(preview.getX(), preview.getY(), preview.getFitWidth(), preview.getFitHeight());
            rect.setArcWidth(50);
            rect.setArcHeight(50);
            preview.setClip(rect);
            Label name = new Label(chat.getName());
            name.setPrefWidth(90);
            name.setMaxWidth(90);
            name.setPrefHeight(50);
            name.setLayoutY(10);
            name.setLayoutX(50);
            name.setFont(Font.font("JetBrains Mono"));
            if(name.getText().length() >= 10){
                String[] words = name.getText().split(" ");
                if(words[0].length() >= 10)
                    name.setText(words[0].substring(0, 6)+"...");
                else
                    name.setText(words[0]);
            }
            name.setAlignment(Pos.CENTER);
            this.setOnMouseClicked(mouseEvent -> {
                if (!mouseEvent.getButton().equals(MouseButton.PRIMARY))
                    return;

                if (ConfigService.getSelected().getId().equals(chat.getId()))
                    return;
                ConfigService.selectChat(chat.getId());
                changeSelectionChatView(chat.getId());
                show_messages_by_current_chat();
            });
            if(chat.getId().equals(ConfigService.getSelected().getId()))
                this.getStyleClass().add("selected-chat-view");
            this.getChildren().addAll(preview, name);
            AnchorPane.setRightAnchor(this, 5.0);
            AnchorPane.setLeftAnchor(this, 5.0);
            AnchorPane.setTopAnchor(this, 5.0);
            AnchorPane.setBottomAnchor(this, 5.0);

            ContextMenu menu = new ContextMenu();
            MenuItem first = new MenuItem("Print ID");
            first.setOnAction(event -> {
                System.out.println(chatId);
            });
            MenuItem second = new MenuItem("second");
            MenuItem third = new MenuItem("third");
            menu.getItems().addAll(first, second, third);

            this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if(event.getButton().equals(MouseButton.SECONDARY)){
                    menu.show(this, event.getScreenX(), event.getScreenY());
                }
            });
        }
    }

    private void addChat(Chat chat){
        AnchorPane el = new AnchorPane();
        el.getChildren().add(new ChatView(chat));
        chats_content.getChildren().addAll(el);
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
