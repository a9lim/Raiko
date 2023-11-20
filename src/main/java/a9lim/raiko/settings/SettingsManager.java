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

package a9lim.raiko.settings;

import a9lim.jdautilities.command.GuildSettingsManager;
import a9lim.raiko.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Guild;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class SettingsManager implements GuildSettingsManager<Settings> {
    private final HashMap<Long, Settings> settings;

    public SettingsManager() {
        settings = new HashMap<>();
        try {
            JSONObject loadedSettings = new JSONObject(new String(Files.readAllBytes(OtherUtil.getPath("serversettings.json"))));
            loadedSettings.keySet().forEach((id) -> {
                JSONObject o = loadedSettings.getJSONObject(id);

                // Legacy version support: On versions 0.3.3 and older, the repeat mode was represented as a boolean.
                if (!o.has("repeat_mode") && o.has("repeat") && o.getBoolean("repeat"))
                    o.put("repeat_mode", RepeatMode.ALL);


                settings.put(Long.parseLong(id), new Settings(this,
                        o.has("text_channel_id") ? o.getString("text_channel_id") : null,
                        o.has("voice_channel_id") ? o.getString("voice_channel_id") : null,
                        o.has("volume") ? o.getInt("volume") : 100,
                        o.has("default_playlist") ? o.getString("default_playlist") : null,
                        o.has("repeat_mode") ? o.getEnum(RepeatMode.class, "repeat_mode") : RepeatMode.OFF,
                        o.has("prefix") ? o.getString("prefix") : null));
            });
        } catch (IOException | JSONException e) {
            LoggerFactory.getLogger("Settings").warn("Failed to load server settings (this is normal if no settings have been set yet): " + e);
        }
    }

    /**
     * Gets non-null settings for a Guild
     *
     * @param guild the guild to get settings for
     * @return the existing settings, or new settings for that guild
     */
    @Override
    public Settings getSettings(Guild guild) {
        return getSettings(guild.getIdLong());
    }

    public Settings getSettings(long guildId) {
        return settings.computeIfAbsent(guildId, id -> createDefaultSettings());
    }

    private Settings createDefaultSettings() {
        return new Settings(this, 0, 0, 100, null, RepeatMode.OFF, null);
    }

    protected void writeSettings() {
        JSONObject obj = new JSONObject();
        settings.forEach((key, s) -> {
            JSONObject o = new JSONObject();
            if (s.textId != 0)
                o.put("text_channel_id", Long.toString(s.textId));
            if (s.voiceId != 0)
                o.put("voice_channel_id", Long.toString(s.voiceId));
            if (s.getVolume() != 100)
                o.put("volume", s.getVolume());
            if (s.getDefaultPlaylist() != null)
                o.put("default_playlist", s.getDefaultPlaylist());
            if (s.getRepeatMode() != RepeatMode.OFF)
                o.put("repeat_mode", s.getRepeatMode());
            if (s.getPrefix() != null)
                o.put("prefix", s.getPrefix());
            obj.put(Long.toString(key), o);
        });
        try {
            Files.write(OtherUtil.getPath("serversettings.json"), obj.toString(4).getBytes());
        } catch (IOException ex) {
            LoggerFactory.getLogger("Settings").warn("Failed to write to file: " + ex);
        }
    }
}
