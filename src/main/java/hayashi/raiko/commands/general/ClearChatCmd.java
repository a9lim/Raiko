package hayashi.raiko.commands.general;
import hayashi.jdautilities.command.Command;
import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.ChatBot;

public class ClearChatCmd extends Command {

    private final ChatBot chatBot;

    public ClearChatCmd(ChatBot chatBot, Bot bot) {
        this.chatBot = chatBot;
        this.name = "clearchat";
        this.help = "wipe raiko's memory";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    @Override
    protected void execute(CommandEvent event) {
        chatBot.clear();
    }
}
