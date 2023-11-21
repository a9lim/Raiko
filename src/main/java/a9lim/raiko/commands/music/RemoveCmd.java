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
import a9lim.raiko.Bot;
import a9lim.raiko.audio.AudioHandler;
import a9lim.raiko.audio.QueuedTrack;
import a9lim.raiko.commands.MusicCommand;
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
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getQueue().isEmpty()) {
            event.replyError("There is nothing in the queue!");
            return;
        }

        int pos;
        if ("all".equalsIgnoreCase(event.getArgs())) {
            pos = handler.getQueue().size();
            handler.getQueue().clear();
            if (pos == 0)
                event.replyWarning("There are no songs in the queue!");
            else
                event.replySuccess("Successfully removed " + pos + " entries.");
            return;
        }
        if ("mine".equalsIgnoreCase(event.getArgs())) {
            if ((pos = handler.getQueue().removeAll(event.getAuthor().getIdLong())) == 0)
                event.replyWarning("You don't have any songs in the queue!");
            else
                event.replySuccess("Successfully removed your " + pos + " entries.");
            return;
        }
        try {
            pos = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException e) {
            QueuedTrack qt = handler.getQueue().getDeque().peek();
            handler.getQueue().getDeque().pop();
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
        if (pos < 1 || pos > handler.getQueue().size()) {
            event.replyError("Position must be a valid integer between 1 and " + handler.getQueue().size() + "!");
            return;
        }
        QueuedTrack qt = handler.getQueue().remove(pos);
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
