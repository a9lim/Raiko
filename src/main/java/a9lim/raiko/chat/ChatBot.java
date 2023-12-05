// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
//
// This file is part of Raiko.
//
// Raiko is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// Raiko is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Raiko. If not, see <http://www.gnu.org/licenses/>.

package a9lim.raiko.chat;

import a9lim.raiko.BotConfig;
import a9lim.raiko.queue.DoubleDealingQueue;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class ChatBot {

    // HttpClient with long wait times because gpt often takes a while
    private final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
    private String preprompt;
    private String head;
    private static final int capacity = 10;
    private final DoubleDealingQueue<QueuedChat> chathist = new DoubleDealingQueue<>(capacity);
    private String temp = "";
    private boolean cheap;
    private final String apiKey;
    private final MediaType mediaType = MediaType.parse("application/json");

    // Set up details with config
    public ChatBot(BotConfig c) {
        apiKey = c.getCgpttoken();
        cheap = c.getModel();
        preprompt = c.getPreprompt();
        clearHead();
    }

    // Get model in use
    public String getModel(){
        return (cheap ? "gpt-3.5-turbo-1106" : "gpt-4-1106-preview");
    }

    // Function to chat
    public String chat(String s, long l) {
        // Append and json-ize new prompt to previous reply, escaping forbidden characters (\n and ")
        temp += "{\"role\":\"user\",\"content\":" + JSONObject.quote(s) + "}";

        // If chat history is full, clear out space
        if(chathist.size() == capacity)
            chathist.pop();

        // Add new prompt to chat history
        chathist.add(new QueuedChat(temp,l));
        try {
            JSONObject o = new JSONObject(client.newCall(new Request.Builder()
                            .url("https://api.openai.com/v1/chat/completions")
                            .post(RequestBody.create(head + chathist + "]}",mediaType))
                            .addHeader("Authorization", "Bearer " + apiKey)
                            .addHeader("Content-Type", "application/json")
                            .build())
                    .execute().body().string());
            System.out.println(o);
            // Send full request to openai, and process and save result as reply
            String reply = (String) o.query("/choices/0/message/content");
            System.out.println(reply);

            // Save and json-ize new reply, escaping forbidden characters (\n and ")
            temp = ", {\"role\":\"assistant\",\"content\":" + JSONObject.quote(reply) + "},";

            // Return reply
            return reply;
        } catch (Exception e){
            // If something goes wrong, clear chat history in case some message was causing the issue and return error
            e.printStackTrace();
            clear();
            return "Huh?";
        }
    }
    public void clear(){
        chathist.clear();
    }

    public void remove(int i){
        chathist.remove(capacity - i);
    }

    public void rewind(int i){
        chathist.backskip(i);
    }

    public void toggleModel(){
        cheap = !cheap;
        clearHead();
    }
    public void setPreprompt(String s){
        preprompt = s;
    }

    public void clearHead(){
        head = "{\"model\":\"" + getModel() + "\",\"messages\":[{\"role\":\"system\",\"content\":\"" + preprompt + "\"},";
    }
}
