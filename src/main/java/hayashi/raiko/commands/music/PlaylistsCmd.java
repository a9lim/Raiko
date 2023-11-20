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

package hayashi.raiko.commands.music;

import java.util.List;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.commands.MusicCommand;

public class PlaylistsCmd extends MusicCommand {
    public PlaylistsCmd(Bot bot) {
        super(bot);
        name = "playlists";
        help = "shows the available playlists";
        aliases = bot.getConfig().getAliases(name);
        guildOnly = true;
        beListening = false;
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (!bot.getPlaylistLoader().folderExists())
            bot.getPlaylistLoader().createFolder();
        if (!bot.getPlaylistLoader().folderExists()) {
            event.replyWarning(" Playlists folder does not exist and could not be created!");
            return;
        }
        List<String> list = bot.getPlaylistLoader().getPlaylistNames();
        if (list == null)
            event.replyError(" Failed to load available playlists!");
        else if (list.isEmpty())
            event.replyWarning(" There are no playlists in the Playlists folder!");
        else {
            StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available playlists:\n");
            list.forEach(str -> builder.append("`").append(str).append("` "));
            builder.append("\nType `").append(event.getClient().getTextualPrefix()).append("play playlist <name>` to play a playlist");
            event.reply(builder.toString());
        }
    }
}
