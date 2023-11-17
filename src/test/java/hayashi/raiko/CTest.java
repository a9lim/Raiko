package hayashi.raiko;
import hayashi.raiko.entities.Prompt;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class CTest {
    public static void main(String[] args) throws IOException {

        BotConfig config = new BotConfig(new Prompt("Raiko"));
        config.load();

        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();

        String preprompt = "You are Raiko Horikawa, fun-loving, free spirited drum tsukumogami. You're having a chat with several humans!";
        String apiKey = config.getCgpttoken();
        MediaType mediaType = MediaType.parse("application/json");
        Scanner s = new Scanner(System.in);

        String jsonhead = "{\"model\": \"gpt-3.5-turbo-1106\", \"messages\": [{\"role\": \"system\", \"content\": \"" + preprompt + "\"}, ";
        while (true) {
            jsonhead += "{\"role\": \"user\", \"content\": \"" + s.nextLine().replace("\\","\\\\").replace("\"", "\\\"") + "\"}";
            try {
                String reply = (new JSONObject(client.newCall(new Request.Builder()
                                .url("https://api.openai.com/v1/chat/completions")
                                .post(RequestBody.create(mediaType, jsonhead + "]}"))
                                .addHeader("Authorization", "Bearer " + apiKey)
                                .addHeader("Content-Type", "application/json")
                                .build())
                        .execute().body().string())
                        .getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getString("content"));
                jsonhead += ", {\"role\": \"assistant\", \"content\": \"" + reply.replace("\\","\\\\").replace("\"", "\\\"") + "\"}, ";
                System.out.println(reply);
            } catch (Exception e){
                System.out.println(e.toString());
                jsonhead = "{\"model\": \"gpt-3.5-turbo-1106\", \"messages\": [{\"role\": \"system\", \"content\": \"" + preprompt + "\"}, ";
                System.out.println("huh");
            }
        }
    }
}
