// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
// Copyright 2018 John Grosh (jagrosh) <john.a.grosh@gmail.com>.
//
// This file is part of Raiko.
//
// Raiko is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// Raiko is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Raiko. If not, see <http://www.gnu.org/licenses/>.


package a9lim.raiko.commands.general;

import a9lim.jdautilities.command.Command;
import a9lim.jdautilities.command.CommandEvent;

import java.time.temporal.ChronoUnit;

public class PingCmd extends Command {

    public PingCmd() {
        name = "ping";
        category = new Category("General");
        help = "checks the bot's latency";
        guildOnly = false;
        aliases = new String[]{"pong"};
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply("Ping: ...", m -> m.editMessage("Ping: " + event.getMessage().getTimeCreated().until(m.getTimeCreated(), ChronoUnit.MILLIS) + "ms | Websocket: " + event.getJDA().getGatewayPing() + "ms").queue());
    }

}
