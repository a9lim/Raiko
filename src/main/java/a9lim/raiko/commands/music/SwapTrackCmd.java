// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
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
import a9lim.raiko.audio.AudioHandler;
import a9lim.raiko.audio.QueuedTrack;
import a9lim.raiko.commands.MusicCommand;
import a9lim.raiko.queue.DoubleDealingQueue;

import java.util.List;

public class SwapTrackCmd extends MusicCommand {

    public SwapTrackCmd() {
        name = "swaptrack";
        help = "swap two tracks in the current queue";
        arguments = "<track 1> <track 2>";
        bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        int a, b;
        String[] parts = COMPILE.split(event.getArgs(), 2);
        try {
            // Validate the args
            a = Integer.parseInt(parts[0]);
            b = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            event.replyError("Please provide two valid indexes.");
            return;
        }
        if (a == b) {
            event.replyError("Can't swap a track with itself.");
            return;
        }
        // Validate that a and b are available
        DoubleDealingQueue<QueuedTrack> queue = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getQueue();
        if (isUnavailablePosition(queue, a) || isUnavailablePosition(queue, b)) {
            event.replyError("Provide a valid position in the queue!");
        } else {
            // Swap the tracks
            List<QueuedTrack> tracks = queue.swap(a - 1, b - 1);
            event.replySuccess(String.format("Swapped **%s** and **%s**.", tracks.getFirst().getTrack().getInfo().title, tracks.getLast().getTrack().getInfo().title));
        }
    }

    private static boolean isUnavailablePosition(DoubleDealingQueue<QueuedTrack> queue, int position) {
        return (position < 1 || position > queue.size());
    }
}