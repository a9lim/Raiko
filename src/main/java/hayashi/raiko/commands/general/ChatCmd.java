package hayashi.raiko.commands.general;
import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.ChatBot;
import hayashi.raiko.commands.ChatCommand;

public class ChatCmd extends ChatCommand {

    public ChatCmd(ChatBot chatBot, Bot bot) {
        super(chatBot);
        this.name = "chat";
        this.help = "talk to raiko";
        this.arguments = "<text>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    @Override
    protected void execute(CommandEvent event) {
        event.reply(chatBot.chat(event.getArgs()));
    }
}
