package hayashi.raiko.commands.general;
import com.typesafe.config.ConfigException;
import hayashi.jdautilities.command.Command;
import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.ChatBot;
import hayashi.raiko.commands.ChatCommand;

public class SetModelCmd extends ChatCommand {

    public SetModelCmd(ChatBot chatBot, Bot bot) {
        super(chatBot);
        this.name = "setmodel";
        this.help = "set gpt model";
        this.arguments = "<cheap|standard|expensive>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    @Override
    protected void execute(CommandEvent event) {
        try {
            chatBot.setModel(event.getArgs());
        } catch (ConfigException ex){
            event.reply(event.getClient().getError() + " Model invalid!");
        }
    }
}
