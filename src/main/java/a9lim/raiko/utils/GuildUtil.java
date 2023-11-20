// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
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

package a9lim.raiko.utils;

import a9lim.raiko.audio.AudioHandler;
import a9lim.raiko.settings.RepeatMode;
import net.dv8tion.jda.api.entities.Guild;

public class GuildUtil {
    public static boolean hasHandler(Guild guild) {
        return guild.getAudioManager().getSendingHandler() != null;
    }
    public static boolean isAlone(Guild guild) {
        return guild.getAudioManager().getConnectedChannel() != null && guild.getAudioManager().getConnectedChannel().getMembers().stream()
                .allMatch(x ->
                        x.getVoiceState().isDeafened()
                                || x.getUser().isBot());
    }
    public static String getQueueTitle(AudioHandler ah, String success, int songslength, long total, RepeatMode repeatmode) {
        StringBuilder sb = new StringBuilder();
        if (ah.getPlayer().getPlayingTrack() != null)
            sb.append(ah.getStatusEmoji()).append(" **")
                    .append(ah.getPlayer().getPlayingTrack().getInfo().title).append("**\n");
        return FormatUtil.filter(sb.append(success).append(" Current Queue | ").append(songslength)
                .append(" entries | `").append(FormatUtil.formatTime(total)).append("` ")
                .append(repeatmode.getEmoji() != null ? "| " + repeatmode.getEmoji() : "").toString());
    }

}
