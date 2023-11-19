package hayashi.raiko;
import okhttp3.*;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

//limit ought to be 12

public class ChatBot {
    private final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
    private static final String preprompt = "You are Raiko Horikawa, fun-loving, free spirited drum tsukumogami. You're having a chat with several humans!";
    private String jsonhead;
    private boolean cheap;
    private final String apiKey;
    private final MediaType mediaType = MediaType.parse("application/json");
    public ChatBot(String apik) {
        apiKey = apik;
        cheap = false;
        clear();
    }

    public String chat(String s) {
        jsonhead += "{\"role\": \"user\", \"content\": \"" + s.replace("\n","\\n").replace("\"", "\\\"") + "\"}";
        try {
            String reply = (new JSONObject(client.newCall(new Request.Builder()
                            .url("https://api.openai.com/v1/chat/completions")
                            .post(RequestBody.create(jsonhead + "]}",mediaType))
                            .addHeader("Authorization", "Bearer " + apiKey)
                            .addHeader("Content-Type", "application/json")
                            .build())
                    .execute().body().string())
                    .getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content"));
            jsonhead += ", {\"role\": \"assistant\", \"content\": \"" + reply.replace("\n","\\n").replace("\"", "\\\"") + "\"}, ";
            return reply;
        } catch (Exception e){
            System.out.println(e);
            clear();
            return "Huh?";
        }
    }
    public void clear(){
        jsonhead = "{\"model\": \"" + (cheap ? "gpt-3.5-turbo-1106" : "gpt-4-1106-preview") + "\", \"messages\": [{\"role\": \"system\", \"content\": \"" + preprompt + "\"}, ";
    }

    public void toggleModel(){
        cheap = !cheap;
        clear();
    }
}
