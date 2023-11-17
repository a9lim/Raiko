package hayashi.raiko;

import hayashi.jdautilities.command.Command;
import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import okhttp3.*;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

//limit ought to be 12

public class ChatBot {
    private OkHttpClient client;
    private final String preprompt = "You are Raiko Horikawa, fun-loving, free spirited drum tsukumogami. You're having a chat with several humans!";
    private String jsonhead;
    private final String apiKey;
    private final MediaType mediaType = MediaType.parse("application/json");
    public ChatBot(String apiKey) {
        reset();
        this.apiKey = apiKey;
    }
    public String chat(String s) {
        jsonhead += "{\"role\": \"user\", \"content\": \"" + s.replace("\\","\\\\").replace("\"", "\\\"") + "\"}";
        System.out.println("NEW MESSAGE \n" + jsonhead + "]}");
        try {
            String out = client.newCall(new Request.Builder()
                            .url("https://api.openai.com/v1/chat/completions")
                            .post(RequestBody.create(mediaType, jsonhead + "]}"))
                            .addHeader("Authorization", "Bearer " + apiKey)
                            .addHeader("Content-Type", "application/json")
                            .build())
                    .execute().body().string();
            System.out.println("NEW RESPONSE \n" + out);
            String reply = new JSONObject(out)
                    .getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content");
            jsonhead += ", {\"role\": \"assistant\", \"content\": \"" + reply.replace("\\","\\\\").replace("\"", "\\\"") + "\"}, ";
            return reply;
        } catch (Exception e){
            System.out.println(e.toString());
            reset();
            return "Huh?";
        }
    }
    public void clear(){
        jsonhead = "{\"model\": \"gpt-4\", \"messages\": [{\"role\": \"system\", \"content\": \"" + preprompt + "\"}, ";
    }

    public void reset(){
         client = new OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
        clear();
        System.out.println("Chatbot Refreshed");
    }
}
