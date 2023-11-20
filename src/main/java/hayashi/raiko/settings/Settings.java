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

package hayashi.raiko.settings;

import hayashi.jdautilities.command.GuildSettingsProvider;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.util.Collection;
import java.util.Collections;

public class Settings implements GuildSettingsProvider {
    private final SettingsManager manager;
    protected long textId, voiceId;
    private int volume;
    private String defaultPlaylist, prefix;
    private RepeatMode repeatMode;

    // why isnt this super
    public Settings(SettingsManager man, String tid, String vid, int v, String dp, RepeatMode rm, String p) {
        manager = man;
        try {
            textId = Long.parseLong(tid);
        } catch (NumberFormatException e) {
            textId = 0;
        }
        try {
            voiceId = Long.parseLong(vid);
        } catch (NumberFormatException e) {
            voiceId = 0;
        }
        volume = v;
        defaultPlaylist = dp;
        repeatMode = rm;
        prefix = p;
    }

    public Settings(SettingsManager man, long tid, long vid, int v, String dp, RepeatMode rm, String p) {
        manager = man;
        textId = tid;
        voiceId = vid;
        volume = v;
        defaultPlaylist = dp;
        repeatMode = rm;
        prefix = p;
    }

    // Getters
    public TextChannel getTextChannel(Guild guild) {
        return guild == null ? null : guild.getTextChannelById(textId);
    }

    public VoiceChannel getVoiceChannel(Guild guild) {
        return guild == null ? null : guild.getVoiceChannelById(voiceId);
    }

    public int getVolume() {
        return volume;
    }

    public String getDefaultPlaylist() {
        return defaultPlaylist;
    }

    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public Collection<String> getPrefixes() {
        return prefix == null ? Collections.emptySet() : Collections.singleton(prefix);
    }

    // Setters
    public void setTextChannel(TextChannel tc) {
        textId = tc == null ? 0 : tc.getIdLong();
        manager.writeSettings();
    }

    public void setVoiceChannel(VoiceChannel vc) {
        voiceId = vc == null ? 0 : vc.getIdLong();
        manager.writeSettings();
    }

    public void setVolume(int v) {
        volume = v;
        manager.writeSettings();
    }

    public void setDefaultPlaylist(String dp) {
        defaultPlaylist = dp;
        manager.writeSettings();
    }

    public void setRepeatMode(RepeatMode mode) {
        repeatMode = mode;
        manager.writeSettings();
    }

    public void setPrefix(String p) {
        prefix = p;
        manager.writeSettings();
    }

}
