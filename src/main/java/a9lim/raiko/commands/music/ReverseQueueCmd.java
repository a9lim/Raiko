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

public class ReverseQueueCmd extends MusicCommand {

    public ReverseQueueCmd() {
        name = "reverse";
        help = "seija gaming";
        bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        DoubleDealingQueue<QueuedTrack> queue = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getQueue();
        if (queue.size() < 2)
            event.replyError("There is no music in the queue!");
        else {
            queue.reverse();
            event.replySuccess("Queue Reversed!");
        }
    }
}