package hayashi.raiko.commands.general;

import hayashi.jdautilities.command.Command;
import hayashi.jdautilities.command.CommandEvent;

import java.time.temporal.ChronoUnit;

public class PingCommand extends Command {

    public PingCommand() {
        this.name = "ping";
        this.help = "checks the bot's latency";
        this.guildOnly = false;
        this.aliases = new String[]{"pong"};
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply("Ping: ...", m -> {
            long ping = event.getMessage().getTimeCreated().until(m.getTimeCreated(), ChronoUnit.MILLIS);
            m.editMessage("Ping: " + ping + "ms | Websocket: " + event.getJDA().getGatewayPing() + "ms").queue();
        });
    }

}
