// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
// Copyright 2016 John Grosh (jagrosh) <john.a.grosh@gmail.com>.
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
import a9lim.raiko.settings.Settings;

public class AutoplaylistCmd extends OwnerCommand {
    private final Bot bot;

    public AutoplaylistCmd(Bot b) {
        bot = b;
        guildOnly = true;
        name = "autoplaylist";
        arguments = "<name|NONE>";
        help = "sets the default playlist for the server";
        aliases = bot.getConfig().getAliases(name);
    }

    @Override
    public void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError(" Please include a playlist name or NONE");
            return;
        }
        if ("none".equalsIgnoreCase(event.getArgs())) {
            ((Settings) event.getClient().getSettingsFor(event.getGuild())).setDefaultPlaylist(null);
            event.replySuccess(" Cleared the default playlist for **" + event.getGuild().getName() + "**");
            return;
        }
        String pname = COMPILE.matcher(event.getArgs()).replaceAll("_");
        if (bot.getPlaylistLoader().getPlaylist(pname) == null) {
            event.replyError(" Could not find `" + pname + ".txt`!");
            return;
        }
        ((Settings) event.getClient().getSettingsFor(event.getGuild())).setDefaultPlaylist(pname);
        event.replySuccess(" The default playlist for **" + event.getGuild().getName() + "** is now `" + pname + "`");
    }
}
