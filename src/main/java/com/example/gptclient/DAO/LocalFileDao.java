package com.example.gptclient.DAO;

import com.example.gptclient.DTO.Chat;
import com.example.gptclient.DTO.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LocalFileDao implements DAO{
    private JSONObject settings;
    private JSONArray chats;

    private static final LocalFileDao singleton;
    static {
        try {
            singleton = new LocalFileDao();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LocalFileDao singleton(){
        return singleton;
    }

    private LocalFileDao() throws IOException {
        File configFile = new File("config.json");
        File cacheFile = new File("cache.json");
        if(!configFile.exists())
            initConfigFile(configFile);
        if(!cacheFile.exists())
            createChatsFile(cacheFile);
        if(cacheFile.exists() && configFile.exists())
            fetch();
    }

    public void fetch(){
        File configFile = new File("config.json");
        File chatsFile = new File("cache.json");
        try {
            settings = new JSONObject(Files.readString(Path.of(configFile.getPath())));
            chats = new JSONArray(Files.readString(Path.of(chatsFile.getPath())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initConfigFile(File file) throws IOException {
        JSONObject config = new JSONObject();
        config.put("api_endpoint", "api.openai.com");
        config.put("api_token", "YOUR_TOKEN");
        config.put("selected_chat", "0");
        var success = file.createNewFile();
        if (!success)
            throw new IOException(STR."\{file.getName()} file can not be created");
        Files.writeString(Path.of(file.getPath()), config.toString());
        push();
    }

    private void createChatsFile(File file) throws IOException {
        JSONArray chats = new JSONArray();
        JSONObject object = Chat.builder()
                .id("0")
                .name("First chat!")
                .messages(List.of())
                .uriToImage("")
                .build()
                .toJSON();
        chats.put(object);
        var success = file.createNewFile();
        if (!success)
            throw new IOException(STR."\{file.getName()} file can not be created");
        Files.writeString(Path.of(file.getPath()), chats.toString());
        push();
    }

    private void push(){
        try {
            Files.writeString(Path.of("config.json"), settings.toString(4));
        } catch (Exception e) {
            System.out.println("Config can not pushed");
        }
        try {
            Files.writeString(Path.of("cache.json"), chats.toString(4));
        } catch (Exception e) {
            System.out.println("Chats can not pushed");
        }
    }

    @Override
    public void deleteChat(String chatId) {
        int index = 0;
        for(int i = 0; i < chats.length(); i++){
            if(chats.getJSONObject(i).getString("id").equals(chatId))
                index = i;
        }
        chats.remove(index);
        push();
    }

    @Override
    public Chat createChat(String name, String uri) {
        JSONObject chat = new JSONObject();
        chat.put("id", random());
        chat.put("uriToImage", uri);
        chat.put("name", name);
        chat.put("messages", List.of());
        chats.put(chat);
        push();
        return Chat.builder()
                .name(name)
                .id(chat.getString("id"))
                .uriToImage(uri)
                .messages(List.of())
                .build();
    }

    private String random(){
        char[] chars = new char[10];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (Math.random()*(int)'A' + (int)'z');
        }
        return new String(chars);
    }

    @Override
    public void selectChat(String chatId) {
        settings.put("selected_chat", chatId);
        push();
    }

    @Override
    public Chat getChatById(String id) {
        for(Object obj: chats){
            if(((JSONObject)obj).getString("id").equals(id)) {
                JSONObject res = (JSONObject) obj;
                return Chat.builder()
                        .name(res.getString("name"))
                        .id(res.getString("id"))
                        .messages(json_array_to_messages(res.getJSONArray("messages")))
                        .uriToImage(res.getString("uriToImage"))
                        .build();
            }
        }
        createChatById(id);
        return getChatById(id);
    }

    @Override
    public Chat[] getAllChats() {
        Chat[] res = new Chat[chats.length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = Chat
                    .builder()
                    .id(chats.getJSONObject(i).getString("id"))
                    .name(chats.getJSONObject(i).getString("name"))
                    .uriToImage(chats.getJSONObject(i).getString("uriToImage"))
                    .messages(json_array_to_messages(chats.getJSONObject(i).getJSONArray("messages")))
                    .build();
        }
        return res;
    }

    public void createChatById(String id) {
        chats.put(Chat.builder().id(id).name("New chat").messages(List.of()).uriToImage("").build().toJSON());
        push();
    }

    @Override
    public void createNewMessage(String chatId, String role, String message) {
        fetch();
        for(Object obj: chats){
            JSONObject cur = (JSONObject) obj;
            if(cur.getString("id").equals(chatId))
                cur.append("messages", new Message(role, message).toJSON());
        }
        push();
    }

    private List<Message> json_array_to_messages(JSONArray arr){
        var res = new ArrayList<Message>();
        for (int i = 0; i < arr.length(); i++) {
            res.add(new Message(arr.getJSONObject(i).getString("role"), arr.getJSONObject(i).getString("content")));
        }
        return res;
    }

    @Override
    public JSONObject getConfig() {
        return null;
    }

    @Override
    public Chat getSelectedChat() {
        return getChatById(settings.getString("selected_chat"));
    }
}
