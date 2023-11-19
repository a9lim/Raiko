/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hayashi.raiko.settings;

import hayashi.jdautilities.command.GuildSettingsProvider;

import java.util.Collection;
import java.util.Collections;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.*;

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
