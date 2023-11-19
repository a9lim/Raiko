package hayashi.raiko.commands.chat;
import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.chat.ChatBot;
import hayashi.raiko.commands.ChatCommand;

public class ChatCmd extends ChatCommand {

    public ChatCmd(ChatBot chatBot, Bot bot) {
        super(chatBot);
        name = "chat";
        help = "talk to raiko";
        arguments = "<text>";
        aliases = bot.getConfig().getAliases(name);
    }
    @Override
    protected void execute(CommandEvent event) {
        event.reply(chatBot.chat(event.getArgs(), event.getAuthor().getIdLong()));
    }
}
