package hayashi.raiko.commands.chat;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.chat.ChatBot;
import hayashi.raiko.commands.ChatCommand;

public class RemoveChatCmd extends ChatCommand {

    public RemoveChatCmd(ChatBot chatBot, Bot bot) {
        super(chatBot);
        name = "removechat";
        help = "delete a certain message from raiko's memory";
        arguments = "<position>";
        aliases = bot.getConfig().getAliases(name);
    }
    @Override
    protected void execute(CommandEvent event) {
        try {
            chatBot.remove(Integer.parseInt(event.getArgs()));
            event.replySuccess("Message removed!");
        } catch (Exception e){
            event.replyError("Please input a valid number!");
        }
    }
}
