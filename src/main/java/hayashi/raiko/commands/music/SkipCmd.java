// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>.
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

package hayashi.raiko.commands.music;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.audio.AudioHandler;
import hayashi.raiko.audio.RequestMetadata;
import hayashi.raiko.commands.MusicCommand;

public class SkipCmd extends MusicCommand {
    public SkipCmd(Bot bot) {
        super(bot);
        name = "skip";
        help = "skips songs";
        arguments = "<position>";
        aliases = bot.getConfig().getAliases(name);
        bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        int index;
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        try {
            index = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException e) {
            RequestMetadata rm = handler.getRequestMetadata();
            event.replySuccess(" Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title
                    + "** " + (rm.getOwner() == 0L ? "(autoplay)" : "(requested by **" + rm.user.username + "**)"));
            handler.getPlayer().stopTrack();
            return;
        }
        if (index < 1 || index > handler.getQueue().size()) {
            event.replyError(" Position must be a valid integer between 1 and " + handler.getQueue().size() + "!");
            return;
        }
        handler.getQueue().skip(index - 1);
        event.replySuccess(" Skipped to **" + handler.getQueue().get(0).getTrack().getInfo().title + "**");
        handler.getPlayer().stopTrack();
    }
}
