package hayashi.raiko.commands.chat;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.ChatBot;
import hayashi.raiko.commands.ChatCommand;

public class ClearChatCmd extends ChatCommand {

    public ClearChatCmd(ChatBot chatBot, Bot bot) {
        super(chatBot);
        name = "clearchat";
        help = "wipe raiko's memory";
        aliases = bot.getConfig().getAliases(name);
    }
    @Override
    protected void execute(CommandEvent event) {
        chatBot.clear();
        event.replySuccess("Raiko Reset!");
    }
}
