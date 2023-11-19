package hayashi.raiko.commands.general;

import hayashi.jdautilities.command.Command;
import hayashi.jdautilities.command.CommandEvent;

import java.time.temporal.ChronoUnit;

public class PingCommand extends Command {

    public PingCommand() {
        name = "ping";
        help = "checks the bot's latency";
        guildOnly = false;
        aliases = new String[]{"pong"};
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply("Ping: ...", m -> m.editMessage("Ping: " + event.getMessage().getTimeCreated().until(m.getTimeCreated(), ChronoUnit.MILLIS) + "ms | Websocket: " + event.getJDA().getGatewayPing() + "ms").queue());
    }

}
