package hayashi.raiko.commands;

import hayashi.jdautilities.command.Command;
import hayashi.raiko.ChatBot;

public abstract class ChatCommand extends Command {
    protected final ChatBot chatBot;
    public ChatCommand(ChatBot chatBot){
        this.chatBot = chatBot;
        this.guildOnly = true;
        this.category = new Category("Chat");
    }
}
