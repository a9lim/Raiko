/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hayashi.raiko.commands.owner;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import hayashi.jdautilities.command.Command;
import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.commands.OwnerCommand;
import hayashi.raiko.playlist.PlaylistLoader.Playlist;

public class PlaylistCmd extends OwnerCommand {
    private static final Pattern PATTERN = Pattern.compile("[*?|\\/\":<>]");
    private final Bot bot;

    public PlaylistCmd(Bot b) {
        bot = b;
        guildOnly = false;
        name = "playlist";
        arguments = "<append|delete|make|setdefault>";
        help = "playlist management";
        aliases = bot.getConfig().getAliases(name);
        children = new OwnerCommand[]{
                new ListCmd(),
                new AppendlistCmd(),
                new DeletelistCmd(),
                new MakelistCmd(),
                new DefaultlistCmd(bot)
        };
    }

    @Override
    public void execute(CommandEvent event) {
        StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " Playlist Management Commands:\n");
        for (Command cmd : children)
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments() == null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        event.reply(builder.toString());
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
                event.reply(event.getClient().getError() + " Playlist `" + pname + "` already exists!");
                return;
            }
            try {
                bot.getPlaylistLoader().createPlaylist(pname);
                event.reply(event.getClient().getSuccess() + " Successfully created playlist `" + pname + "`!");
            } catch (IOException e) {
                event.reply(event.getClient().getError() + " Unable to create the playlist: " + e.getLocalizedMessage());
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
                event.reply(event.getClient().getError() + " Playlist `" + pname + "` doesn't exist!");
                return;
            }
            try {
                bot.getPlaylistLoader().deletePlaylist(pname);
                event.reply(event.getClient().getSuccess() + " Successfully deleted playlist `" + pname + "`!");
            } catch (IOException e) {
                event.reply(event.getClient().getError() + " I was unable to delete the playlist: " + e.getLocalizedMessage());
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
                event.reply(event.getClient().getError() + " Please include a playlist name and URLs to add!");
                return;
            }
            String pname = parts[0];
            Playlist playlist = bot.getPlaylistLoader().getPlaylist(pname);
            if (playlist == null) {
                event.reply(event.getClient().getError() + " Playlist `" + pname + "` doesn't exist!");
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
                event.reply(event.getClient().getSuccess() + " Successfully added " + urls.length + " items to playlist `" + pname + "`!");
            } catch (IOException e) {
                event.reply(event.getClient().getError() + " I was unable to append to the playlist: " + e.getLocalizedMessage());
            }
        }
    }

    public static class DefaultlistCmd extends AutoplaylistCmd {
        public DefaultlistCmd(Bot bot) {
            super(bot);
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
                event.reply(event.getClient().getWarning() + " Playlists folder does not exist and could not be created!");
                return;
            }
            List<String> list = bot.getPlaylistLoader().getPlaylistNames();
            if (list == null) {
                event.reply(event.getClient().getError() + " Failed to load available playlists!");
                return;
            }
            if (list.isEmpty()) {
                event.reply(event.getClient().getWarning() + " There are no playlists in the Playlists folder!");
                return;
            }
            StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available playlists:\n");
            list.forEach(str -> builder.append("`").append(str).append("` "));
            event.reply(builder.toString());
        }
    }
}
