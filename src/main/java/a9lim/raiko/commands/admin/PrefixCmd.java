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

package a9lim.raiko.commands.admin;

import a9lim.jdautilities.command.CommandEvent;
import a9lim.raiko.Bot;
import a9lim.raiko.commands.AdminCommand;
import a9lim.raiko.settings.Settings;

public class PrefixCmd extends AdminCommand {
    public PrefixCmd() {
        name = "prefix";
        help = "sets a server-specific prefix";
        arguments = "<prefix|NONE>";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a prefix or NONE");
            return;
        }

        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if ("none".equalsIgnoreCase(event.getArgs())) {
            s.setPrefix(null);
            event.replySuccess("Prefix cleared.");
        } else {
            s.setPrefix(event.getArgs());
            event.replySuccess("Custom prefix set to `" + event.getArgs() + "` on *" + event.getGuild().getName() + "*");
        }
    }
}
