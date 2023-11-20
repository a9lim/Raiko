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
import a9lim.raiko.audio.AudioHandler;
import a9lim.raiko.audio.QueuedTrack;
import a9lim.raiko.commands.MusicCommand;
import a9lim.raiko.queue.DoubleDealingQueue;

public class MoveTrackCmd extends MusicCommand {

    public MoveTrackCmd(Bot bot) {
        super(bot);
        name = "movetrack";
        help = "move a track in the current queue to a different position";
        arguments = "<from> <to>";
        aliases = bot.getConfig().getAliases(name);
        bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        int from, to;

        String[] parts = COMPILE.split(event.getArgs(), 2);
        try {
            // Validate the args
            from = Integer.parseInt(parts[0]);
            to = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            event.replyError("Please provide two valid indexes.");
            return;
        }
        if (from == to) {
            event.replyError("Can't move a track to the same position.");
            return;
        }
        // Validate that from and to are available
        DoubleDealingQueue<QueuedTrack> queue = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getQueue();
        if (isUnavailablePosition(queue, from) || isUnavailablePosition(queue, to)) {
            event.replyError("Provide a valid position in the queue!");
        } else {
            // Move the track
            event.replySuccess(String.format("Moved **%s** from position `%d` to `%d`.", queue.moveItem(from - 1, to - 1).getTrack().getInfo().title, from, to));
        }
    }

    private static boolean isUnavailablePosition(DoubleDealingQueue<QueuedTrack> queue, int position) {
        return (position < 1 || position > queue.size());
    }
}