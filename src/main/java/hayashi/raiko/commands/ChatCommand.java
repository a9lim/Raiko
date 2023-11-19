package hayashi.raiko.commands;

import hayashi.jdautilities.command.Command;
import hayashi.raiko.chat.ChatBot;

public abstract class ChatCommand extends Command {
    protected final ChatBot chatBot;
    public ChatCommand(ChatBot cBot){
        chatBot = cBot;
        guildOnly = true;
        category = new Category("Chat");
    }
}
