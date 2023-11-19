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

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.audio.AudioHandler;
import hayashi.raiko.audio.QueuedTrack;
import hayashi.raiko.commands.MusicCommand;
import net.dv8tion.jda.api.entities.User;

public class RemoveCmd extends MusicCommand {
    public RemoveCmd(Bot bot) {
        super(bot);
        name = "remove";
        help = "removes a song from the queue";
        arguments = "<position|MINE|ALL>";
        aliases = bot.getConfig().getAliases(name);
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
