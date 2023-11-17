package hayashi.raiko;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CTest {
    public static void main(String[] args) throws IOException {
        String API_URL = "https://api.openai.com/v1/chat/completions";

        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();

        String preprompt = "You are Raiko Horikawa, drum tsukumogami";
        String prompt = "Hi Raiko!";
        String apiKey = "";

        MediaType mediaType = MediaType.parse("application/json");
        System.out.println(mediaType);
        RequestBody body = RequestBody.create(mediaType, "{\"model\": \"gpt-4\", \"messages\": [{\"role\": \"system\", \"content\": \"" + preprompt +"\"}, {\"role\": \"user\", \"content\": \"" + prompt + "\"}]}");
        System.out.println("{\"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"system\", \"content\": \"" + preprompt +"\"}, {\"role\": \"user\", \"content\": \"" + prompt + "\"}]");
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        System.out.println(new JSONObject(client.newCall(request).execute()
                .body().string())
                .getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content"));
    }
}
