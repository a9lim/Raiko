package hayashi.raiko.commands.general;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.ChatBot;
import hayashi.raiko.commands.ChatCommand;

public class ClearChatCmd extends ChatCommand {

    public ClearChatCmd(ChatBot chatBot, Bot bot) {
        super(chatBot);
        this.name = "clearchat";
        this.help = "wipe raiko's memory";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    @Override
    protected void execute(CommandEvent event) {
        chatBot.clear();
        event.replySuccess("Raiko Reset!");
    }
}
