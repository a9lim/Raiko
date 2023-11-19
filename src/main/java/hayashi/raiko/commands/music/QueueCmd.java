/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
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

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.jdautilities.menu.Paginator;
import hayashi.raiko.Bot;
import hayashi.raiko.audio.AudioHandler;
import hayashi.raiko.audio.QueuedTrack;
import hayashi.raiko.commands.MusicCommand;
import hayashi.raiko.settings.RepeatMode;
import hayashi.raiko.settings.Settings;
import hayashi.raiko.utils.FormatUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class QueueCmd extends MusicCommand {
    private final Paginator.Builder builder;

    public QueueCmd(Bot bot) {
        super(bot);
        name = "queue";
        help = "shows the current queue";
        arguments = "[pagenum]";
        aliases = bot.getConfig().getAliases(name);
        bePlaying = true;
        botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS};
        builder = new Paginator.Builder()
                .setColumns(1)
                .setFinalAction(m -> {
                    try {
                        m.clearReactions().queue();
                    } catch (PermissionException ignore) {}
                })
                .setItemsPerPage(10)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .wrapPageEnds(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1, TimeUnit.MINUTES);
    }

    @Override
    public void doCommand(CommandEvent event) {
        int pagenum = 1;
        try {
            pagenum = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException ignore) {}
        AudioHandler ah = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        Deque<QueuedTrack> deque = ah.getQueue().getDeque();
        if (deque.isEmpty()) {
            MessageEditData nowp = ah.getNowPlaying(event.getJDA());
            event.reply(new MessageEditBuilder()
                    .setContent(event.getClient().getWarning() + " There is no music in the queue!")
                    .setEmbeds((nowp == null ? ah.getNoMusicPlaying(event.getJDA()) : nowp).getEmbeds().get(0)).build().getContent()
                    , m -> {
                if (nowp != null)
                    bot.getNowplayingHandler().setLastNPMessage(m);
            });
            return;
        }
        String[] songs = new String[deque.size()];
        long total = 0;
        Iterator<QueuedTrack> iterator = deque.iterator();
        for (int i = 0; i < deque.size(); i++) {
            QueuedTrack track = iterator.next();
            total += track.getTrack().getDuration();
            songs[i] = track.toString();
        }
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        long fintotal = total;
        builder.setText((i1, i2) -> getQueueTitle(ah, event.getClient().getSuccess(), songs.length, fintotal, settings.getRepeatMode()))
                .setItems(songs)
                .setUsers(event.getAuthor())
                .setColor(event.getSelfMember().getColor());
        builder.build().paginate(event.getChannel(), pagenum);
    }

    private String getQueueTitle(AudioHandler ah, String success, int songslength, long total, RepeatMode repeatmode) {
        StringBuilder sb = new StringBuilder();
        if (ah.getPlayer().getPlayingTrack() != null)
            sb.append(ah.getStatusEmoji()).append(" **")
                    .append(ah.getPlayer().getPlayingTrack().getInfo().title).append("**\n");
        return FormatUtil.filter(sb.append(success).append(" Current Queue | ").append(songslength)
                .append(" entries | `").append(FormatUtil.formatTime(total)).append("` ")
                .append(repeatmode.getEmoji() != null ? "| " + repeatmode.getEmoji() : "").toString());
    }
}
