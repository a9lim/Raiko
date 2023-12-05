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

package a9lim.raiko.audio;

import a9lim.raiko.Bot;
import a9lim.raiko.BotConfig;
import a9lim.raiko.audio.sourcefix.FixNicoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.yamusic.YandexMusicAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;
public class PlayerManager extends DefaultAudioPlayerManager {
    private final Bot bot;

    public PlayerManager(Bot b) {
        bot = b;
    }

    public void init(BotConfig config) {
        // custom sources
        YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager(true,config.getYTEmail(),config.getYTPW());
        yt.setPlaylistPageCount(10);
        registerSourceManager(yt);
        registerSourceManager(new FixNicoAudioSourceManager(config.getNNDEmail(),config.getNNDPW()));
        registerSourceManager(new YandexMusicAudioSourceManager(true));
        registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        registerSourceManager(new BandcampAudioSourceManager());
        registerSourceManager(new VimeoAudioSourceManager());
        registerSourceManager(new TwitchStreamAudioSourceManager());
        registerSourceManager(new BeamAudioSourceManager());
        registerSourceManager(new GetyarnAudioSourceManager());
        registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
        registerSourceManager(new LocalAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
    }

    public Bot getBot() {
        return bot;
    }

    public AudioHandler setUpHandler(Guild guild) {
        AudioHandler handler = (AudioHandler) guild.getAudioManager().getSendingHandler();
        if (handler != null)
            return handler;
        AudioPlayer player = createPlayer();
        player.setVolume(bot.getSettingsManager().getSettings(guild).getVolume());
        handler = new AudioHandler(this, guild, player);
        player.addListener(handler);
        guild.getAudioManager().setSendingHandler(handler);
        return handler;
    }
}
