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
        String prompt = "Hi Raiko!";
        MediaType mediaType = MediaType.parse("application/json");
        Scanner s = new Scanner(System.in);

        String head = "{\"model\": \"gpt-4\", \"messages\": [{\"role\": \"system\", \"content\": \"" + preprompt + "\"}, " +
                "{\"role\": \"user\", \"content\": \"" + prompt + "\"}";
        String reply;
        while (true) {
            RequestBody body = RequestBody.create(mediaType,
                    head + "]}");
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            System.out.println("WAITING");
            reply = new JSONObject(client.newCall(request).execute()
                    .body().string())
                    .getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content");
            System.out.println(reply);
            prompt = s.nextLine();
            if(prompt.equalsIgnoreCase("quit")) {
                System.out.println(head);
                return;
            }
            head += ", {\"role\": \"assistant\", \"content\": \"" + reply + "\"}, {\"role\": \"user\", \"content\": \"" + prompt + "\"}";
        }
    }
}
