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

import java.util.List;

import a9lim.jdautilities.command.CommandEvent;
import a9lim.raiko.commands.MusicCommand;

public class PlaylistsCmd extends MusicCommand {
    public PlaylistsCmd() {
        name = "playlists";
        help = "shows the available playlists";
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
            StringBuilder builder = new StringBuilder(" Available playlists:\n");
            list.forEach(str -> builder.append("`").append(str).append("` "));
            builder.append("\nType `").append(event.getClient().getDefaultPrefix()).append("play playlist <name>` to play a playlist");
            event.replySuccess(builder.toString());
        }
    }
}
