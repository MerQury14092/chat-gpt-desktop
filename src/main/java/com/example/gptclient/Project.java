package com.example.gptclient;

public class Project {
    public static void async(Runnable task){
        new Thread(task).start();
    }
}
