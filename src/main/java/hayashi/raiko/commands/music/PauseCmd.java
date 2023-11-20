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

public class PauseCmd extends MusicCommand {
    public PauseCmd(Bot bot) {
        super(bot);
        name = "pause";
        help = "pauses the current song";
        aliases = bot.getConfig().getAliases(name);
        bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getPlayer().isPaused()) {
            handler.getPlayer().setPaused(false);
            event.replySuccess("Resumed **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**.");
        } else {
            handler.getPlayer().setPaused(true);
            event.replySuccess("Paused **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**.");
        }
    }
}
