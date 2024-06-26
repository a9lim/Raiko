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

package a9lim.raiko.commands.music;

import a9lim.jdautilities.command.Command;
import a9lim.jdautilities.command.CommandEvent;
import a9lim.jdautilities.menu.ButtonMenu;
import a9lim.raiko.audio.AudioHandler;
import a9lim.raiko.audio.QueuedTrack;
import a9lim.raiko.commands.MusicCommand;
import a9lim.raiko.playlist.PlaylistLoader;
import a9lim.raiko.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.concurrent.TimeUnit;

public class PlayCmd extends MusicCommand {
    private final static Emoji LOAD = Emoji.fromUnicode("\uD83D\uDCE5"); // 📥
    private final static Emoji CANCEL = Emoji.fromUnicode("\uD83D\uDEAB"); // 🚫

    private final String loadingEmoji;

    public PlayCmd() {
        loadingEmoji = bot.getConfig().getLoading();
        name = "play";
        arguments = "<title|URL|subcommand>";
        help = "plays the provided song";
        beListening = true;
        bePlaying = false;
        children = new Command[]{new PlaylistCmd()};
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            if (handler.getPlayer().getPlayingTrack() != null && handler.getPlayer().isPaused()) {
                handler.getPlayer().setPaused(false);
                event.replySuccess("Resumed **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**.");
                return;
            }
            StringBuilder builder = new StringBuilder(" Play Commands:\n")
                    .append("\n`").append(event.getClient().getDefaultPrefix()).append(name).append(" <song title>` - plays the first result from Youtube")
                    .append("\n`").append(event.getClient().getDefaultPrefix()).append(name).append(" <URL>` - plays the provided song, playlist, or stream");
            for (Command cmd : children)
                builder.append("\n`").append(event.getClient().getDefaultPrefix()).append(name).append(" ")
                        .append(cmd.getName()).append(" ").append(cmd.getArguments()).append("` - ").append(cmd.getHelp());
            event.replyWarning(builder.toString());
            return;
        }
        String args = !event.getArgs().isEmpty() && event.getArgs().charAt(0) == '<' && event.getArgs().charAt(event.getArgs().length() - 1) == '>'
                ? event.getArgs().substring(1, event.getArgs().length() - 1)
                : event.getArgs().isEmpty() ? event.getMessage().getAttachments().getFirst().getUrl() : event.getArgs();
        event.reply(loadingEmoji + " Loading... `[" + args + "]`", m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), args, new ResultHandler(m, event, false)));
    }

    private static final class ResultHandler implements AudioLoadResultHandler {
        private final Message m;
        private final CommandEvent event;
        private final boolean ytsearch;

        private ResultHandler(Message message, CommandEvent e, boolean b) {
            m = message;
            event = e;
            ytsearch = b;
        }

        private void loadSingle(AudioTrack track, AudioPlaylist playlist) {
            if (bot.getConfig().isTooLong(track)) {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " This track (**" + track.getInfo().title + "**) is longer than the allowed maximum: `"
                        + FormatUtil.formatTime(track.getDuration()) + "` > `" + bot.getConfig().getMaxTime() + "`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            handler.addTrack(new QueuedTrack(track, event.getAuthor()));
            int pos = handler.getQueue().size() + 1;
            String addMsg = FormatUtil.filter(event.getClient().getSuccess() + " Added **" + track.getInfo().title
                    + "** (`" + FormatUtil.formatTime(track.getDuration()) + "`) " + (pos == 0 ? "to begin playing" : " to the queue at position " + pos));
            if (playlist == null || !event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ADD_REACTION)) {
                m.editMessage(addMsg).queue();
                return;
            }
            new ButtonMenu.Builder()
                    .setText(addMsg + "\n" + event.getClient().getWarning() + " This track has a playlist of **" + playlist.getTracks().size() + "** tracks attached. Select " + LOAD.getFormatted() + " to load playlist.")
                    .setChoices(LOAD, CANCEL)
                    .setEventWaiter(bot.getWaiter())
                    .setTimeout(30, TimeUnit.SECONDS)
                    .setAction(re -> m.editMessage(re.equals(LOAD) ?
                                    (addMsg + "\n" + event.getClient().getSuccess() + " Loaded **" + loadPlaylist(playlist, track) + "** additional tracks!") :
                                    addMsg).queue())
                    .setFinalAction(m -> {
                        try {
                            m.clearReactions().queue();
                        } catch (PermissionException ignore) {}
                    }).build().display(m);

        }

        private int loadPlaylist(AudioPlaylist playlist, AudioTrack exclude) {
            int count = 0;
            for(AudioTrack track : playlist.getTracks()) {
                if (!bot.getConfig().isTooLong(track) && !track.equals(exclude)) {
                    ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).addTrack(new QueuedTrack(track, event.getAuthor()));
                    count++;
                }
            }
            return count;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            loadSingle(track, null);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            if (playlist.getTracks().size() == 1 || playlist.isSearchResult()) {
                AudioTrack single = playlist.getSelectedTrack();
                loadSingle(single == null ? playlist.getTracks().getFirst() : single, null);
                return;
            }
            if (playlist.getSelectedTrack() != null) {
                loadSingle(playlist.getSelectedTrack(), playlist);
                return;
            }
            int count = loadPlaylist(playlist, null);
            if (playlist.getTracks().isEmpty()) {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " The playlist " + (playlist.getName() == null ? "" : "(**" + playlist.getName()
                        + "**) ") + " could not be loaded or contained 0 entries")).queue();
                return;
            }
            if (count == 0) {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " All entries in this playlist " + (playlist.getName() == null ? "" : "(**" + playlist.getName()
                        + "**) ") + "were longer than the allowed maximum (`" + bot.getConfig().getMaxTime() + "`)")).queue();
                return;
            }
            m.editMessage(FormatUtil.filter(event.getClient().getSuccess() + " Found "
                    + (playlist.getName() == null ? "a playlist" : "playlist **" + playlist.getName() + "**") + " with `"
                    + playlist.getTracks().size() + "` entries; added to the queue!"
                    + (count < playlist.getTracks().size() ? "\n" + event.getClient().getWarning() + " Tracks longer than the allowed maximum (`"
                    + bot.getConfig().getMaxTime() + "`) have been omitted." : ""))).queue();
        }

        @Override
        public void noMatches() {
            if (ytsearch)
                m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " No results found for `" + event.getArgs() + "`.")).queue();
            else
                bot.getPlayerManager().loadItemOrdered(event.getGuild(), "ytsearch:" + event.getArgs(), new ResultHandler(m, event, true));
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            m.editMessage(event.getClient().getError() + " Error loading" +
                    (throwable.severity == Severity.COMMON ? ": " + throwable.getMessage() : "  track.")).queue();
        }
    }

    public class PlaylistCmd extends MusicCommand {
        public PlaylistCmd() {
            name = "playlist";
            aliases = new String[]{"pl"};
            arguments = "<name>";
            help = "plays the provided playlist";
            beListening = true;
            bePlaying = false;
        }

        @Override
        public void doCommand(CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.replyError(" Please include a playlist name.");
                return;
            }
            PlaylistLoader.Playlist playlist = bot.getPlaylistLoader().getPlaylist(event.getArgs());
            if (playlist == null) {
                event.replyError("I could not find `" + event.getArgs() + ".txt` in the Playlists folder.");
                return;
            }
            event.getChannel().sendMessage(loadingEmoji + " Loading playlist **" + event.getArgs() + "**... (" + playlist.getItems().size() + " items)").queue(m ->
                playlist.loadTracks(bot.getPlayerManager(), (at) -> ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler())
                        .addTrack(new QueuedTrack(at, event.getAuthor())), () -> {
                    StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                            ? event.getClient().getWarning() + " No tracks were loaded!"
                            : event.getClient().getSuccess() + " Loaded **" + playlist.getTracks().size() + "** tracks!");
                    if (!playlist.getErrors().isEmpty())
                        builder.append("\nThe following tracks failed to load:");
                    playlist.getErrors().forEach(err ->
                            builder.append("\n`[").append(err.getIndex() + 1).append("]` **").append(err.getItem()).append("**: ").append(err.getReason()));
                    m.editMessage(FormatUtil.filter(builder.length() > 2000 ? builder.append(" (...)").substring(0,1994) : builder.toString())).queue();
                }));
        }
    }
}
