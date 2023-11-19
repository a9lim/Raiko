package hayashi.raiko;
import hayashi.raiko.chat.ChatBot;
import hayashi.raiko.chat.QueuedChat;
import hayashi.raiko.entities.Prompt;
import hayashi.raiko.queue.DoubleDealingQueue;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class CTest {
    public static void main(String[] args) throws IOException {

        final BotConfig config = new BotConfig(new Prompt("Raiko"));
        config.load();
        final ChatBot chatBot = new ChatBot(config.getCgpttoken(), config.getModel());
        Scanner s = new Scanner(System.in);
        while (true){
            System.out.println(chatBot.chat(s.nextLine(), 0L));
        }
    }
}
