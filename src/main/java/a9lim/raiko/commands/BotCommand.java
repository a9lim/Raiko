package a9lim.raiko.commands;

import a9lim.jdautilities.command.Command;
import a9lim.raiko.Bot;

public abstract class BotCommand extends Command {
    protected static Bot bot;

    public static void setBot(Bot b){
        bot = b;
        Command.setAliasSource(b.getConfig());
    }

}
