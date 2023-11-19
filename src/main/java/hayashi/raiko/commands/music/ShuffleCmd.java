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

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.audio.AudioHandler;
import hayashi.raiko.commands.MusicCommand;

public class ShuffleCmd extends MusicCommand {
    public ShuffleCmd(Bot bot) {
        super(bot);
        name = "shuffle";
        help = "shuffles the queue";
        arguments = "<MINE|ALL>";
        aliases = bot.getConfig().getAliases(name);
        beListening = true;
        bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        int s;
        if ("mine".equalsIgnoreCase(event.getArgs())) {
            if ((s = handler.getQueue().shuffle(event.getAuthor().getIdLong())) < 2)
                event.replyError("You don't have enough songs in the queue!");
            else
                event.replySuccess("You successfully shuffled your " + s + " entries.");
        } else {
            if ((s = handler.getQueue().size()) < 2)
                event.replyWarning("There aren't enough songs in the queue!");
            else {
                handler.getQueue().shuffle();
                event.replySuccess("You successfully shuffled all " + s + " entries.");
            }
        }
    }

}
