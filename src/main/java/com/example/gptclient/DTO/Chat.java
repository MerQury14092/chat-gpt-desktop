package com.example.gptclient.DTO;

import javafx.collections.MapChangeListener;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String id;
    private String name;
    private String uriToImage;
    private List<Message> messages;

    public Chat(String id, String name, String uriToImage, List<Message> messages) {
        this.id = id;
        this.name = name;
        this.uriToImage = uriToImage;
        this.messages = messages;
    }

    public Chat(String id, String name, String uriToImage) {
        this.id = id;
        this.name = name;
        this.uriToImage = uriToImage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUriToImage() {
        return uriToImage;
    }

    public void setUriToImage(String uriToImage) {
        this.uriToImage = uriToImage;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("name", name);
        object.put("uriToImage", uriToImage);
        object.put("messages", messages);
        return object;
    }

    public static ChatBuilder builder(){
        return new ChatBuilder();
    }

    public static class ChatBuilder{
        private ChatBuilder(){}
        private String id;
        private String name;
        private String uriToImage;
        private List<Message> messages;

        public ChatBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ChatBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ChatBuilder uriToImage(String uriToImage) {
            this.uriToImage = uriToImage;
            return this;
        }

        public ChatBuilder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        public Chat build(){
            return new Chat(id, name, uriToImage, messages);
        }
    }
}
