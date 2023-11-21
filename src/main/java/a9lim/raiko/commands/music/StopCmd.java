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

public class StopCmd extends MusicCommand {
    public StopCmd() {
        name = "stop";
        help = "stops the current song and clears the queue";
        bePlaying = false;
    }

    @Override
    public void doCommand(CommandEvent event) {
        ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler() ).stopAndClear();
        event.getGuild().getAudioManager().closeAudioConnection();
        event.replySuccess(" The player has stopped and the queue has been cleared.");
    }
}
