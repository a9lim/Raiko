// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>.
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

package hayashi.raiko.commands.admin;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.jdautilities.commons.utils.FinderUtil;
import hayashi.raiko.Bot;
import hayashi.raiko.commands.AdminCommand;
import hayashi.raiko.settings.Settings;
import hayashi.raiko.utils.FormatUtil;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class SettcCmd extends AdminCommand {
    public SettcCmd(Bot bot) {
        name = "settc";
        help = "sets the text channel for music commands";
        arguments = "<channel|NONE>";
        aliases = bot.getConfig().getAliases(name);
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError(" Please include a text channel or NONE");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if ("none".equalsIgnoreCase(event.getArgs())) {
            s.setTextChannel(null);
            event.replySuccess(" Music commands can now be used in any channel");
            return;
        }
        List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
        if (list.isEmpty())
            event.replyWarning(" No Text Channels found matching \"" + event.getArgs() + "\"");
        else if (list.size() > 1)
            event.replyWarning(FormatUtil.listOfTChannels(list, event.getArgs()));
        else {
            s.setTextChannel(list.get(0));
            event.replyWarning(" Music commands can now only be used in <#" + list.get(0).getId() + ">");
        }
    }

}
