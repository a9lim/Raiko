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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class NowplayingCmd extends MusicCommand {
    public NowplayingCmd(Bot bot) {
        super(bot);
        name = "nowplaying";
        help = "shows the song that is currently playing";
        aliases = bot.getConfig().getAliases(name);
        botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        MessageCreateData m = handler.getNowPlaying(event.getJDA());
        if (m != null) {
            event.reply(m, msg -> bot.getNowplayingHandler().setLastNPMessage(msg));
            return;
        }
        event.reply(handler.getNoMusicPlaying(event.getJDA()));
        bot.getNowplayingHandler().clearLastNPMessage(event.getGuild());
    }
}
