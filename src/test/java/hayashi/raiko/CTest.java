package hayashi.raiko;
import hayashi.raiko.chat.ChatBot;
import hayashi.raiko.entities.Prompt;

import java.io.IOException;
import java.util.Scanner;
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
