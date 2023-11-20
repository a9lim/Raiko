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
import a9lim.raiko.commands.MusicCommand;
import a9lim.raiko.settings.Settings;
import a9lim.raiko.utils.FormatUtil;

public class VolumeCmd extends MusicCommand {
    public VolumeCmd(Bot bot) {
        super(bot);
        name = "volume";
        aliases = bot.getConfig().getAliases(name);
        help = "sets or shows volume";
        arguments = "[0-150]";
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        int volume = handler.getPlayer().getVolume();
        if (event.getArgs().isEmpty()) {
            event.reply(FormatUtil.volumeIcon(volume) + " Current volume is `" + volume + "`");
            return;
        }
        int nvolume;
        try {
            nvolume = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException e) {
            nvolume = -1;
        }
        if (nvolume < 0 || nvolume > 150) {
            event.replyError(" Volume must be a valid integer between 0 and 150!");
            return;
        }
        handler.getPlayer().setVolume(nvolume);
        ((Settings)event.getClient().getSettingsFor(event.getGuild())).setVolume(nvolume);
        event.reply(FormatUtil.volumeIcon(nvolume) + " Volume changed from `" + volume + "` to `" + nvolume + "`");
    }

}
