package com.example.gptclient.DTO;

public class Chat {
    private String id;
    private String name;
    private String uriToImage;
    private Message[] messages;

    public Chat(String id, String name, String uriToImage, Message[] messages) {
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

    public Message[] getMessages() {
        return messages;
    }

    public void setMessages(Message[] messages) {
        this.messages = messages;
    }

    public static ChatBuilder builder(){
        return new ChatBuilder();
    }

    private static class ChatBuilder{
        private String id;
        private String name;
        private String uriToImage;
        private Message[] messages;

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

        public ChatBuilder messages(Message[] messages) {
            this.messages = messages;
            return this;
        }

        public Chat build(){
            return new Chat(id, name, uriToImage, messages);
        }
    }
}
