// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>
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

import a9lim.jdautilities.command.Command;
import a9lim.jdautilities.command.CommandEvent;
import a9lim.raiko.Bot;
import a9lim.raiko.commands.OwnerCommand;
import a9lim.raiko.playlist.PlaylistLoader.Playlist;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class PlaylistCmd extends OwnerCommand {
    private static final Pattern PATTERN = Pattern.compile("[*?|\\/\":<>]");

    public PlaylistCmd() {
        guildOnly = false;
        name = "playlist";
        arguments = "<append|delete|make|setdefault>";
        help = "playlist management";
        children = new OwnerCommand[]{
                new ListCmd(),
                new AppendlistCmd(),
                new DeletelistCmd(),
                new MakelistCmd(),
                new DefaultlistCmd()
        };
    }

    @Override
    public void execute(CommandEvent event) {
        StringBuilder builder = new StringBuilder(" Playlist Management Commands:\n");
        for (Command cmd : children)
            builder.append("\n`").append(event.getClient().getDefaultPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments() == null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        event.replyWarning(builder.toString());
    }

    public class MakelistCmd extends OwnerCommand {
        public MakelistCmd() {
            name = "make";
            aliases = new String[]{"create"};
            help = "makes a new playlist";
            arguments = "<name>";
            guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = COMPILE.matcher(event.getArgs()).replaceAll("_");
            pname = PATTERN.matcher(pname).replaceAll("");
            if (pname.isEmpty()) {
                event.replyError("Please provide a name for the playlist!");
                return;
            }
            if (bot.getPlaylistLoader().getPlaylist(pname) != null) {
                event.replyError(" Playlist `" + pname + "` already exists!");
                return;
            }
            try {
                bot.getPlaylistLoader().createPlaylist(pname);
                event.replySuccess(" Successfully created playlist `" + pname + "`!");
            } catch (IOException e) {
                event.replyError(" Unable to create the playlist: " + e.getLocalizedMessage());
            }
        }
    }

    public class DeletelistCmd extends OwnerCommand {
        public DeletelistCmd() {
            name = "delete";
            aliases = new String[]{"remove"};
            help = "deletes an existing playlist";
            arguments = "<name>";
            guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = COMPILE.matcher(event.getArgs()).replaceAll("_");
            if (bot.getPlaylistLoader().getPlaylist(pname) == null) {
                event.replyError(" Playlist `" + pname + "` doesn't exist!");
                return;
            }
            try {
                bot.getPlaylistLoader().deletePlaylist(pname);
                event.replySuccess(" Successfully deleted playlist `" + pname + "`!");
            } catch (IOException e) {
                event.replyError(" I was unable to delete the playlist: " + e.getLocalizedMessage());
            }
        }
    }

    public class AppendlistCmd extends OwnerCommand {
        public AppendlistCmd() {
            name = "append";
            aliases = new String[]{"add"};
            help = "appends songs to an existing playlist";
            arguments = "<name> <URL> | <URL> | ...";
            guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] parts = COMPILE.split(event.getArgs(), 2);
            if (parts.length < 2) {
                event.replyError(" Please include a playlist name and URLs to add!");
                return;
            }
            String pname = parts[0];
            Playlist playlist = bot.getPlaylistLoader().getPlaylist(pname);
            if (playlist == null) {
                event.replyError(" Playlist `" + pname + "` doesn't exist!");
                return;
            }
            StringBuilder builder = new StringBuilder();
            playlist.getItems().forEach(item -> builder.append("\r\n").append(item));
            String[] urls = parts[1].split("\\|");
            for (String url : urls) {
                String u = url.trim();
                if (!u.isEmpty() && u.charAt(0) == '<' && u.charAt(u.length() - 1) == '>')
                    u = u.substring(1, u.length() - 1);
                builder.append("\r\n").append(u);
            }
            try {
                bot.getPlaylistLoader().writePlaylist(pname, builder.toString());
                event.replySuccess(" Successfully added " + urls.length + " items to playlist `" + pname + "`!");
            } catch (IOException e) {
                event.replyError(" I was unable to append to the playlist: " + e.getLocalizedMessage());
            }
        }
    }

    public static class DefaultlistCmd extends AutoplaylistCmd {
        public DefaultlistCmd() {
            name = "setdefault";
            aliases = new String[]{"default"};
            arguments = "<playlistname|NONE>";
            guildOnly = true;
        }
    }

    public class ListCmd extends OwnerCommand {
        public ListCmd() {
            name = "all";
            aliases = new String[]{"available", "list"};
            help = "lists all available playlists";
            guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (!bot.getPlaylistLoader().folderExists())
                bot.getPlaylistLoader().createFolder();
            if (!bot.getPlaylistLoader().folderExists()) {
                event.replyWarning(" Playlists folder does not exist and could not be created!");
                return;
            }
            List<String> list = bot.getPlaylistLoader().getPlaylistNames();
            if (list == null) {
                event.replyError(" Failed to load available playlists!");
                return;
            }
            if (list.isEmpty()) {
                event.replyWarning(" There are no playlists in the Playlists folder!");
                return;
            }
            StringBuilder builder = new StringBuilder(" Available playlists:\n");
            list.forEach(str -> builder.append("`").append(str).append("` "));
            event.replySuccess(builder.toString());
        }
    }
}
