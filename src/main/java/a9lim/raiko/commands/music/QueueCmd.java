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

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import a9lim.jdautilities.command.CommandEvent;
import a9lim.jdautilities.menu.Paginator;
import a9lim.raiko.audio.AudioHandler;
import a9lim.raiko.audio.QueuedTrack;
import a9lim.raiko.commands.MusicCommand;
import a9lim.raiko.queue.DoubleDealingQueue;
import a9lim.raiko.settings.Settings;
import a9lim.raiko.utils.GuildUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class QueueCmd extends MusicCommand {
    private final Paginator.Builder builder;
    public QueueCmd() {
        name = "queue";
        help = "shows the current queue";
        arguments = "[pagenum]";
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
        DoubleDealingQueue<QueuedTrack> queue = ah.getQueue();
        if (queue.isEmpty()) {
            MessageCreateData nowp = ah.getNowPlaying(event.getJDA());
            event.reply(new MessageCreateBuilder()
                    .setContent(event.getClient().getWarning() + " There is no music in the queue!")
                    .setEmbeds((nowp == null ? ah.getNoMusicPlaying(event.getJDA()) : nowp).getEmbeds().getFirst()).build()
                    , m -> {
                if (nowp != null)
                    bot.getNowplayingHandler().setLastNPMessage(m);
            });
            return;
        }
        String[] songs = new String[queue.size()];
        long total = 0;
        Iterator<QueuedTrack> iterator = queue.getDeque().iterator();
        for (int i = 0; i < songs.length; i++) {
            QueuedTrack track = iterator.next();
            total += track.getTrack().getDuration();
            songs[i] = track.toString();
        }
        long fintotal = total;
        builder.setText((i1, i2) -> GuildUtil.getQueueTitle(ah, event.getClient().getSuccess(), songs.length, fintotal, ((Settings) event.getClient().getSettingsFor(event.getGuild())).getRepeatMode()))
                .setItems(songs)
                .setUsers(event.getAuthor())
                .setColor(event.getSelfMember().getColor());
        builder.build().paginate(event.getChannel(), pagenum);
    }
}
