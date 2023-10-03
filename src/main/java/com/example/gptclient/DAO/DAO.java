package com.example.gptclient.DAO;

import com.example.gptclient.DTO.Chat;

public interface DAO {
    Chat getChatById(String id);
    Chat[] getAllChats();
    void createChat(String name);
    void createNewMessage(Chat where, String message);
}
