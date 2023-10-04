package com.example.gptclient;

public class Project {
    public static void async(Runnable task){
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.setName("async task "+thread.hashCode());
        thread.start();
    }
}
