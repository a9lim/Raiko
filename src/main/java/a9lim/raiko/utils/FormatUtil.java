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

package a9lim.raiko.utils;

import a9lim.raiko.audio.RequestMetadata.UserInfo;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.util.List;

public class FormatUtil {

    public static String formatTime(long duration) {
        if (duration == Long.MAX_VALUE)
            return "LIVE";
        long seconds = duration / 1000;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;
        return (hours > 0 ? hours + ":" : "") + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    public static String formatUsername(String username, String discrim)
    {
        if(discrim == null || discrim.equals("0000"))
        {
            return username;
        }
        else
        {
            return username + "#" + discrim;
        }
    }

    public static String formatUsername(UserInfo userinfo) {
        return formatUsername(userinfo.username, userinfo.discrim);
    }

    public static String formatUsername(User user) {
        return formatUsername(user.getName(), user.getDiscriminator());
    }

    public static String progressBar(double percent) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 12; i++)
            str.append((i == (int) (percent * 12)) ? "\uD83D\uDD18" : "▬");// 🔘
        return str.toString();
    }

    public static String volumeIcon(int volume) {
        if (volume == 0)
            return "\uD83D\uDD07"; // 🔇
        if (volume < 30)
            return "\uD83D\uDD08"; // 🔈
        if (volume < 70)
            return "\uD83D\uDD09"; // 🔉
        return "\uD83D\uDD0A";     // 🔊
    }

    public static String listOfTChannels(List<TextChannel> list, String query) {
        StringBuilder out = new StringBuilder(" Multiple text channels found matching \"" + query + "\":");
        for (int i = 0; i < 6 && i < list.size(); i++) {
            TextChannel channel = list.get(i);
            out.append("\n - ").append(channel.getName()).append(" (<#").append(channel.getId()).append(">)");
        }
        if (list.size() > 6)
            out.append("\n**And ").append(list.size() - 6).append(" more...**");
        return out.toString();
    }

    public static String listOfVChannels(List<VoiceChannel> list, String query) {
        StringBuilder out = new StringBuilder(" Multiple voice channels found matching \"" + query + "\":");
        for (int i = 0; i < 6 && i < list.size(); i++) {
            VoiceChannel channel = list.get(i);
            out.append("\n - ").append(channel.getAsMention()).append(" (ID:").append(channel.getId()).append(")");
        }
        if (list.size() > 6)
            out.append("\n**And ").append(list.size() - 6).append(" more...**");
        return out.toString();
    }

    public static String listOfRoles(List<Role> list, String query) {
        StringBuilder out = new StringBuilder(" Multiple text channels found matching \"" + query + "\":");
        for (int i = 0; i < 6 && i < list.size(); i++) {
            Role role = list.get(i);
            out.append("\n - ").append(role.getName()).append(" (ID:").append(role.getId()).append(")");
        }
        if (list.size() > 6)
            out.append("\n**And ").append(list.size() - 6).append(" more...**");
        return out.toString();
    }

    public static String filter(String input) {
        return input.replace("\u202E", "")
                .replace("@everyone", "@\u0435veryone") // cyrillic letter e
                .replace("@here", "@h\u0435re") // cyrillic letter e
                .trim();
    }
}
