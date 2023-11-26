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
import a9lim.raiko.audio.RequestMetadata;
import a9lim.raiko.commands.MusicCommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeekCmd extends MusicCommand {

    public static final Pattern PATTERN = Pattern.compile("^(\\d?\\d)(?::([0-5]?\\d))?(?::([0-5]?\\d))?$");
    public SeekCmd() {
        name = "seek";
        help = "seek to a certain point in the song";
        arguments = "<position>";
        bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        long index = parseTime(event.getArgs());
        if (index < 0 ) {
            event.replyError(" Not a valid time!");
            return;
        }
        if (index < handler.getPlayer().getPlayingTrack().getDuration()) {
            handler.getPlayer().getPlayingTrack().setPosition(index);
            return;
        }
        // Skip otherwise
        RequestMetadata rm = handler.getRequestMetadata();
        event.replySuccess(" Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title
                + "** " + (rm.getOwner() == 0L ? "(autoplay)" : "(requested by **" + rm.user.username + "**)"));
        handler.getPlayer().stopTrack();
    }

    public static long parseTime (String str) {

        Matcher m = PATTERN.matcher(str);
        if(!m.find())
            return -1;

        if (m.group(3) != null) {
            return 1000 * (Long.parseLong(m.group(3)) + (Long.parseLong(m.group(2)) * 60 + Long.parseLong(m.group(1))) * 60);
        } else if (m.group(2) != null) {
            return 1000 * (Long.parseLong(m.group(2)) + Long.parseLong(m.group(1)) * 60);
        } else if (m.group(1) != null) {
            return 1000 * Long.parseLong(m.group(1));
        } else {
            return -1;
        }
    }
}
