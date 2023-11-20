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
import a9lim.raiko.commands.MusicCommand;

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
