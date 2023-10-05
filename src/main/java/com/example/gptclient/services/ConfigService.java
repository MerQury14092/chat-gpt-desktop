package com.example.gptclient.services;

import com.example.gptclient.DAO.DAO;
import com.example.gptclient.DAO.LocalFileDao;
import com.example.gptclient.DTO.Chat;

public class ConfigService {
    private static final DAO dao;
    static {
        dao = LocalFileDao.singleton();
    }
    public static Chat getSelected(){
        return dao.getSelectedChat();
    }

    public static void saveMessage(boolean isUser, String content){
        dao.createNewMessage(getSelected().getId(), isUser?"user":"assistant", content);
    }

    public static Chat[] getAll(){
        return dao.getAllChats();
    }

    public static void selectChat(String id){
        dao.selectChat(id);
    }

    public static void deleteChat(String id){
        dao.deleteChat(id);
    }

    public static Chat createNewChat(String name, String uri){
        return dao.createChat(name, uri);
    }
}
