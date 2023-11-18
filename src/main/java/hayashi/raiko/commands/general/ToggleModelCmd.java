package hayashi.raiko.commands.general;
import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.ChatBot;
import hayashi.raiko.commands.ChatCommand;

public class ToggleModelCmd extends ChatCommand {

    public ToggleModelCmd(ChatBot chatBot, Bot bot) {
        super(chatBot);
        this.name = "setmodel";
        this.help = "toggle";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    @Override
    protected void execute(CommandEvent event) {
        chatBot.toggleModel();
    }
}
