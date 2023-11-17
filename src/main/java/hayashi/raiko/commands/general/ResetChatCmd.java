package hayashi.raiko.commands.general;
import hayashi.jdautilities.command.Command;
import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.ChatBot;

public class ResetChatCmd extends Command {

    private final ChatBot chatBot;

    public ResetChatCmd(ChatBot chatBot, Bot bot) {
        this.chatBot = chatBot;
        this.name = "resetchat";
        this.help = "turn raiko off and on again";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    @Override
    protected void execute(CommandEvent event) {
        chatBot.reset();
    }
}
