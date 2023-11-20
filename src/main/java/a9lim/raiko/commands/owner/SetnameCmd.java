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

package a9lim.raiko.commands.owner;

import a9lim.jdautilities.command.CommandEvent;
import a9lim.raiko.Bot;
import a9lim.raiko.commands.OwnerCommand;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
@Deprecated
public class SetnameCmd extends OwnerCommand {
    public SetnameCmd(Bot bot) {
        name = "setname";
        help = "sets the name of the bot";
        arguments = "<name>";
        aliases = bot.getConfig().getAliases(name);
        guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String oldname = event.getSelfUser().getName();
            event.getSelfUser().getManager().setName(event.getArgs()).complete(false);
            event.replySuccess(" Name changed from `" + oldname + "` to `" + event.getArgs() + "`");
        } catch (RateLimitedException e) {
            event.replyError(" Name can only be changed twice per hour!");
        } catch (Exception e) {
            event.replyError(" That name is not valid!");
        }
    }
}
