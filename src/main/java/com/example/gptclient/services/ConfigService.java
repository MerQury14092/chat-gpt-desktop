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
}
