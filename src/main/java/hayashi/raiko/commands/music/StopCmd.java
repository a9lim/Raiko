// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>.
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

package hayashi.raiko.commands.music;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.audio.AudioHandler;
import hayashi.raiko.commands.MusicCommand;

public class StopCmd extends MusicCommand {
    public StopCmd(Bot bot) {
        super(bot);
        name = "stop";
        help = "stops the current song and clears the queue";
        aliases = bot.getConfig().getAliases(name);
        bePlaying = false;
    }

    @Override
    public void doCommand(CommandEvent event) {
        ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler() ).stopAndClear();
        event.getGuild().getAudioManager().closeAudioConnection();
        event.replySuccess(" The player has stopped and the queue has been cleared.");
    }
}
