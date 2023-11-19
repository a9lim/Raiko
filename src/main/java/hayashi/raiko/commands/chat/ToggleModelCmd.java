package hayashi.raiko.commands.chat;
import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.ChatBot;
import hayashi.raiko.commands.ChatCommand;

public class ToggleModelCmd extends ChatCommand {

    public ToggleModelCmd(ChatBot chatBot, Bot bot) {
        super(chatBot);
        name = "setmodel";
        help = "toggle";
        aliases = bot.getConfig().getAliases(name);
    }
    @Override
    protected void execute(CommandEvent event) {
        chatBot.toggleModel();
    }
}
