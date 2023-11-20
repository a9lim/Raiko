// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
// Copyright 2017 John Grosh (jagrosh) <john.a.grosh@gmail.com>.
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

package a9lim.raiko.commands.owner;

import a9lim.jdautilities.command.CommandEvent;
import a9lim.raiko.Bot;
import a9lim.raiko.commands.OwnerCommand;
import net.dv8tion.jda.api.OnlineStatus;

public class SetstatusCmd extends OwnerCommand {
    public SetstatusCmd(Bot bot) {
        name = "setstatus";
        help = "sets the status the bot displays";
        arguments = "<status>";
        aliases = bot.getConfig().getAliases(name);
        guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            OnlineStatus status = OnlineStatus.fromKey(event.getArgs());
            if (status == OnlineStatus.UNKNOWN) {
                event.replyError("Please include one of the following statuses: `ONLINE`, `IDLE`, `DND`, `INVISIBLE`");
                return;
            }
            event.getJDA().getPresence().setStatus(status);
            event.replySuccess("Set the status to `" + status.getKey().toUpperCase() + "`");
        } catch (Exception e) {
            event.replyError(" The status could not be set!");
        }
    }
}
