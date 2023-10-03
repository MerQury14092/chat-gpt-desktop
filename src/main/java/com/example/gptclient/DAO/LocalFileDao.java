package com.example.gptclient.DAO;

import com.example.gptclient.DTO.Chat;

import java.io.File;
import java.io.IOException;

public class LocalFileDao implements DAO{

    public LocalFileDao() throws IOException {
        File configFile = new File("config.json");
        if(!configFile.exists())
            initConfigFile(configFile);

    }

    private void initConfigFile(File file) throws IOException {
        var success = file.createNewFile();
        if (!success)
            throw new IOException("config file can not be created");
    }

    @Override
    public Chat getChatById(String id) {
        return null;
    }

    @Override
    public Chat[] getAllChats() {
        return new Chat[0];
    }

    @Override
    public void createChat(String name) {

    }

    @Override
    public void createNewMessage(Chat where, String message) {

    }
}
