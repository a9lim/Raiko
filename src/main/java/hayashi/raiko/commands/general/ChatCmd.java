package hayashi.raiko.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import okhttp3.*;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class ChatCmd extends Command {
    private final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .build();

    private final String preprompt = "You are Raiko Horikawa";

    private final String apiKey;
    public ChatCmd(String apiKey, Bot bot) {
        this.apiKey = apiKey;
        this.name = "chat";
        this.help = "talk to raiko";
        this.arguments = "<text>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    @Override
    protected void execute(CommandEvent event) {
        String prompt = event.getArgs();
        MediaType mediaType = MediaType.parse("application/json");
        System.out.println(mediaType);
        RequestBody body = RequestBody.create(mediaType, "{\"model\": \"gpt-4\", \"messages\": [{\"role\": \"system\", \"content\": \"" + preprompt +"\"}, {\"role\": \"user\", \"content\": \"" + prompt + "\"}]}");
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            event.reply(new JSONObject(client.newCall(request).execute()
                    .body().string())
                    .getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content"));

        } catch (Exception e){
            System.out.println("h");
        }
    }
}
