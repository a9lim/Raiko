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
package hayashi.raiko.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import hayashi.jdautilities.command.Command;
import hayashi.jdautilities.command.CommandEvent;
import hayashi.jdautilities.menu.ButtonMenu;
import hayashi.raiko.Bot;
import hayashi.raiko.audio.AudioHandler;
import hayashi.raiko.audio.QueuedTrack;
import hayashi.raiko.commands.MusicCommand;
import hayashi.raiko.utils.FormatUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import hayashi.raiko.playlist.PlaylistLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.PermissionException;

public class PlayCmd extends MusicCommand {
    private final static Emoji LOAD = Emoji.fromUnicode("\uD83D\uDCE5"); // 📥
    private final static Emoji CANCEL = Emoji.fromUnicode("\uD83D\uDEAB"); // 🚫

    private final String loadingEmoji;

    public PlayCmd(Bot bot) {
        super(bot);
        loadingEmoji = bot.getConfig().getLoading();
        name = "play";
        arguments = "<title|URL|subcommand>";
        help = "plays the provided song";
        aliases = bot.getConfig().getAliases(name);
        beListening = true;
        bePlaying = false;
        children = new Command[]{new PlaylistCmd(bot)};
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
            StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " Play Commands:\n")
                    .append("\n`").append(event.getClient().getPrefix()).append(name).append(" <song title>` - plays the first result from Youtube")
                    .append("\n`").append(event.getClient().getPrefix()).append(name).append(" <URL>` - plays the provided song, playlist, or stream");
            for (Command cmd : children)
                builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName()).append(" ").append(cmd.getArguments()).append("` - ").append(cmd.getHelp());
            event.reply(builder.toString());
            return;
        }
        String args = !event.getArgs().isEmpty() && event.getArgs().charAt(0) == '<' && event.getArgs().charAt(event.getArgs().length() - 1) == '>'
                ? event.getArgs().substring(1, event.getArgs().length() - 1)
                : event.getArgs().isEmpty() ? event.getMessage().getAttachments().get(0).getUrl() : event.getArgs();
        event.reply(loadingEmoji + " Loading... `[" + args + "]`", m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), args, new ResultHandler(m, event, false)));
    }

    private class ResultHandler implements AudioLoadResultHandler {
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
                        + FormatUtil.formatTime(track.getDuration()) + "` > `" + FormatUtil.formatTime(bot.getConfig().getMaxSeconds() * 1000) + "`")).queue();
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
                    .setAction(re -> {
                        if (re.equals(LOAD))
                            m.editMessage(addMsg + "\n" + event.getClient().getSuccess() + " Loaded **" + loadPlaylist(playlist, track) + "** additional tracks!").queue();
                        else
                            m.editMessage(addMsg).queue();
                    }).setFinalAction(m -> {
                        try {
                            m.clearReactions().queue();
                        } catch (PermissionException ignore) {}
                    }).build().display(m);

        }

        // why lambda
        private int loadPlaylist(AudioPlaylist playlist, AudioTrack exclude) {
            AtomicInteger count = new AtomicInteger();
            playlist.getTracks().forEach((track) -> {
                if (!bot.getConfig().isTooLong(track) && !track.equals(exclude)) {
                    AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                    handler.addTrack(new QueuedTrack(track, event.getAuthor()));
                    count.getAndIncrement();
                }
            });
            return count.get();
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            loadSingle(track, null);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            if (playlist.getTracks().size() == 1 || playlist.isSearchResult()) {
                AudioTrack single = playlist.getSelectedTrack();
                loadSingle(single == null ? playlist.getTracks().get(0) : single, null);
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
            if (ytsearch) {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " No results found for `" + event.getArgs() + "`.")).queue();
                return;
            }
            bot.getPlayerManager().loadItemOrdered(event.getGuild(), "ytsearch:" + event.getArgs(), new ResultHandler(m, event, true));
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            m.editMessage(event.getClient().getError() + " Error loading" +
                    (throwable.severity == Severity.COMMON ? ": " + throwable.getMessage() : "  track.")).queue();
        }
    }

    public class PlaylistCmd extends MusicCommand {
        public PlaylistCmd(Bot bot) {
            super(bot);
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
            event.getChannel().sendMessage(loadingEmoji + " Loading playlist **" + event.getArgs() + "**... (" + playlist.getItems().size() + " items)").queue(m -> {
                AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                playlist.loadTracks(bot.getPlayerManager(), (at) -> handler.addTrack(new QueuedTrack(at, event.getAuthor())), () -> {
                    StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                            ? event.getClient().getWarning() + " No tracks were loaded!"
                            : event.getClient().getSuccess() + " Loaded **" + playlist.getTracks().size() + "** tracks!");
                    if (!playlist.getErrors().isEmpty())
                        builder.append("\nThe following tracks failed to load:");
                    playlist.getErrors().forEach(err -> builder.append("\n`[").append(err.getIndex() + 1).append("]` **").append(err.getItem()).append("**: ").append(err.getReason()));
                    String str = builder.toString();
                    if (str.length() > 2000)
                        str = str.substring(0, 1994) + " (...)";
                    m.editMessage(FormatUtil.filter(str)).queue();
                });
            });
        }
    }
}
