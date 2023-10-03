package com.example.gptclient.services;

import com.example.gptclient.DTO.Message;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static java.lang.StringTemplate.STR;

public class GptService {
    private static final String model = "gpt-4";
    private static final String API_Endpont = "ai.fakeopen.com";
    public static final String token = "pk-this-is-a-real-free-pool-token-for-everyone";

    public static String answer(Message[] messages){
        JSONObject requestBody = new JSONObject();
        if (messages == null)
            return null;
        for(Message message: messages) {
            JSONObject messageJson = new JSONObject();
            messageJson.put("role", message.getRole());
            messageJson.put("content", message.getContent());
            requestBody.append("messages", messageJson);
        }
        requestBody.put("model", model);
        System.out.println(requestBody);
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(STR."https://\{API_Endpont}/v1/chat/completions").toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization","Bearer "+token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().flush();
            int status = conn.getResponseCode();
            InputStream input;
            if(status < 300)
                input = conn.getInputStream();
            else
                return "err";

            Scanner scanner = new Scanner(input);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine())
                sb.append(scanner.nextLine()).append("\n");

            if (status >= 300)
                return sb.toString();

            JSONObject response = new JSONObject(sb.toString());

            return response
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream answer_stream(Message[] messages){
        JSONObject requestBody = new JSONObject();
        if (messages == null)
            return null;
        for(Message message: messages) {
            JSONObject messageJson = new JSONObject();
            messageJson.put("role", message.getRole());
            messageJson.put("content", message.getContent());
            requestBody.append("messages", messageJson);
            requestBody.put("stream", true);
        }
        requestBody.put("model", model);
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(STR."https://\{API_Endpont}/v1/chat/completions").toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization","Bearer "+token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().flush();
            return conn.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream direct(String str){
        return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
    }
}
