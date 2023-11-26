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

import a9lim.jdautilities.command.CommandEvent;
import a9lim.raiko.audio.AudioHandler;
import a9lim.raiko.audio.QueuedTrack;
import a9lim.raiko.commands.MusicCommand;
import a9lim.raiko.queue.DoubleDealingQueue;
import net.dv8tion.jda.api.entities.User;

public class RemoveCmd extends MusicCommand {
    public RemoveCmd() {
        name = "remove";
        help = "removes a song from the queue";
        arguments = "<position|MINE|ALL>";
        beListening = true;
        bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        DoubleDealingQueue<QueuedTrack> queue = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getQueue();
        if (queue.isEmpty()) {
            event.replyError("There is nothing in the queue!");
            return;
        }
        int pos;
        switch(event.getArgs().toLowerCase()) {
            case "all" -> {
                pos = queue.size();
                if (pos == 0)
                    event.replyWarning("There are no songs in the queue!");
                else {
                    queue.clear();
                    event.replySuccess("Successfully removed " + pos + " entries.");
                }
            }
            case "mine" -> {
                if ((pos = queue.removeAll(event.getAuthor().getIdLong())) == 0)
                    event.replyWarning("You don't have any songs in the queue!");
                else
                    event.replySuccess("Successfully removed your " + pos + " entries.");
            }
            default -> {
                try {
                    pos = Integer.parseInt(event.getArgs());
                } catch (NumberFormatException e) {
                    QueuedTrack qt = queue.pop();
                    User u;
                    try {
                        u = event.getJDA().getUserById(qt.getIdentifier());
                    } catch (Exception f) {
                        u = null;
                    }
                    event.replySuccess("Removed **" + qt.getTrack().getInfo().title
                            + "** from the queue (requested by " + (u == null ? "someone" : "**" + u.getName() + "**") + ")");
                    return;
                }
                if (pos < 1 || pos > queue.size()) {
                    event.replyError("Position must be a valid integer between 1 and " + queue.size() + "!");
                    return;
                }
                QueuedTrack qt = queue.remove(pos);
                User u;
                try {
                    u = event.getJDA().getUserById(qt.getIdentifier());
                } catch (Exception e) {
                    u = null;
                }
                event.replySuccess("Removed **" + qt.getTrack().getInfo().title
                        + "** from the queue (requested by " + (u == null ? "someone" : "**" + u.getName() + "**") + ")");
            }
        }
    }
}
