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

package a9lim.raiko.commands.music;

import a9lim.jdautilities.command.CommandEvent;
import a9lim.raiko.Bot;
import a9lim.raiko.commands.MusicCommand;
import a9lim.raiko.settings.RepeatMode;
import a9lim.raiko.settings.Settings;

public class RepeatCmd extends MusicCommand {
    public RepeatCmd(Bot bot) {
        super(bot);
        name = "repeat";
        help = "re-adds music to the queue when finished";
        arguments = "[off|all|single]";
        aliases = bot.getConfig().getAliases(name);
        guildOnly = true;
    }

    // override musiccommand's execute because we don't actually care where this is used
    @Override
    protected void execute(CommandEvent event) {
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        settings.setRepeatMode(switch(event.getArgs().toLowerCase()) {
            case "false", "off" -> RepeatMode.OFF;
            case "true", "on", "all" -> RepeatMode.ALL;
            case "one", "single" -> RepeatMode.SINGLE;
            default -> settings.getRepeatMode() == RepeatMode.OFF ? RepeatMode.ALL : RepeatMode.OFF;
        });
        event.replySuccess("Repeat mode is now `" + settings.getRepeatMode().getUserFriendlyName() + "`");
    }

    @Override
    public void doCommand(CommandEvent event) { /* Intentionally Empty */ }
}
