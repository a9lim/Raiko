package hayashi.raiko;
import hayashi.raiko.entities.Prompt;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class CTest {
    public static void main(String[] args) throws IOException {

        final BotConfig config = new BotConfig(new Prompt("Raiko"));
        config.load();

        final OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();

        final String preprompt = "You are Raiko Horikawa, fun-loving, free spirited drum tsukumogami. You're having a chat with several humans!";
        final String apiKey = config.getCgpttoken();
        final MediaType mediaType = MediaType.parse("application/json");
        final Scanner s = new Scanner(System.in);
        String model = "gpt-4-1106-preview";

        StringBuilder jsonhead = new StringBuilder("{\"model\": \"gpt-4-1106-preview\", \"messages\": [{\"role\": \"system\", \"content\": \"" + preprompt + "\"}, ");
        while (true) {
            jsonhead.append("{\"role\": \"user\", \"content\": \"").append(s.nextLine().replace("\n", "\\n").replace("\"", "\\\"")).append("\"}");
            try {
                String reply = (new JSONObject(client.newCall(new Request.Builder()
                                .url("https://api.openai.com/v1/chat/completions")
                                .post(RequestBody.create( jsonhead + "]}",mediaType))
                                .addHeader("Authorization", "Bearer " + apiKey)
                                .addHeader("Content-Type", "application/json")
                                .build())
                        .execute().body().string())
                        .getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getString("content"));
                jsonhead.append(", {\"role\": \"assistant\", \"content\": \"").append(reply.replace("\n", "\\n").replace("\"", "\\\"")).append("\"}, ");
                System.out.println(reply);
            } catch (Exception e){
                System.out.println(e);
                jsonhead = new StringBuilder("{\"model\": \"gpt-4-1106-preview\", \"messages\": [{\"role\": \"system\", \"content\": \"" + preprompt + "\"}, ");
                System.out.println("huh");
            }
        }
    }
}
