package hayashi.raiko.commands.chat;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.chat.ChatBot;
import hayashi.raiko.commands.ChatCommand;

public class RewindChatCmd extends ChatCommand {

    public RewindChatCmd(ChatBot chatBot, Bot bot) {
        super(chatBot);
        name = "rewindchat";
        help = "delete the last few messages from raiko's memory";
        arguments = "<position>";
        aliases = bot.getConfig().getAliases(name);
    }
    @Override
    protected void execute(CommandEvent event) {
        try {
            chatBot.rewind(Integer.parseInt(event.getArgs()));
            event.replySuccess("Messages removed!");
        } catch (Exception e){
            event.replyError("Please input a valid number!");
        }
    }
}
