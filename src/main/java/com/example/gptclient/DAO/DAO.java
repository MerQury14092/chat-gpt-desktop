package com.example.gptclient.DAO;

import com.example.gptclient.DTO.Chat;
import org.json.JSONObject;

public interface DAO {
    Chat getChatById(String id);
    Chat[] getAllChats();
    void createNewMessage(String chatId, String role,  String message);
    JSONObject getConfig();
    Chat getSelectedChat();
    void selectChat(String chatId);
    void deleteChat(String chatId);
    Chat createChat(String name, String uri);
}
