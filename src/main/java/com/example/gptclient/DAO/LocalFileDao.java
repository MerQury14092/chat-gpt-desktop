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
        fetch();
    }

    private void createChatsFile(File file) throws IOException {
        JSONArray chats = new JSONArray();
        JSONObject object = Chat.builder()
                .id("123")
                .name("432")
                .messages(List.of())
                .build()
                .toJSON();
        chats.put(object);
        var success = file.createNewFile();
        if (!success)
            throw new IOException(STR."\{file.getName()} file can not be created");
        Files.writeString(Path.of(file.getPath()), chats.toString());
        fetch();
    }

    private void push(){
        try {
            Files.writeString(Path.of("config.json"), settings.toString(4));
            Files.writeString(Path.of("cache.json"), chats.toString(4));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public void createChat(String name) {
        chats.put(Chat.builder().id("DSASD").name(name).messages(List.of()).uriToImage("").build().toJSON());
        push();
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
