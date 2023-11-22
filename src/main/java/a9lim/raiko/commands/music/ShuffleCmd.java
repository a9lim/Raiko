// Copyright 2023 a9lim (a9lim) <mx@a9l.im>.
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
import a9lim.raiko.audio.AudioHandler;
import a9lim.raiko.audio.QueuedTrack;
import a9lim.raiko.commands.MusicCommand;
import a9lim.raiko.queue.DoubleDealingQueue;

public class ShuffleCmd extends MusicCommand {
    public ShuffleCmd() {
        name = "shuffle";
        help = "shuffles the queue";
        arguments = "<MINE|ALL>";
        beListening = true;
        bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        DoubleDealingQueue<QueuedTrack> queue = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getQueue();
        int s;
        if ("mine".equalsIgnoreCase(event.getArgs())) {
            if ((s = queue.shuffle(event.getAuthor().getIdLong())) < 2)
                event.replyError("You don't have enough songs in the queue!");
            else
                event.replySuccess("You successfully shuffled your " + s + " entries.");
        } else {
            if ((s = queue.size()) < 2)
                event.replyWarning("There aren't enough songs in the queue!");
            else {
                queue.shuffle();
                event.replySuccess("You successfully shuffled all " + s + " entries.");
            }
        }
    }

}
