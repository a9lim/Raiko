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

package a9lim.raiko;

import a9lim.jdautilities.commons.waiter.EventWaiter;
import a9lim.raiko.audio.AloneInVoiceHandler;
import a9lim.raiko.audio.AudioHandler;
import a9lim.raiko.audio.NowplayingHandler;
import a9lim.raiko.audio.PlayerManager;
import a9lim.raiko.chat.ChatBot;
import a9lim.raiko.gui.GUI;
import a9lim.raiko.playlist.PlaylistLoader;
import a9lim.raiko.settings.SettingsManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Bot {
    private final EventWaiter waiter;
    private final ScheduledExecutorService threadpool;
    private final BotConfig config;
    private final SettingsManager settings;
    private final PlayerManager players;
    private final PlaylistLoader playlists;
    private final NowplayingHandler nowplaying;
    private final AloneInVoiceHandler aloneInVoiceHandler;
    private final ChatBot chatBot;

    private boolean shuttingDown;
    private JDA jda;
    private GUI gui;

    public Bot(EventWaiter inwaiter, BotConfig inconfig, SettingsManager insettings) {
        waiter = inwaiter;
        config = inconfig;
        settings = insettings;
        playlists = new PlaylistLoader(inconfig);
        threadpool = Executors.newSingleThreadScheduledExecutor();
        players = new PlayerManager(this);
        players.init();
        nowplaying = new NowplayingHandler(this);
        nowplaying.init();
        aloneInVoiceHandler = new AloneInVoiceHandler(this);
        aloneInVoiceHandler.init();
        chatBot = new ChatBot(inconfig);
    }

    public BotConfig getConfig() {
        return config;
    }

    public SettingsManager getSettingsManager() {
        return settings;
    }

    public EventWaiter getWaiter() {
        return waiter;
    }

    public ScheduledExecutorService getThreadpool() {
        return threadpool;
    }

    public PlayerManager getPlayerManager() {
        return players;
    }

    public PlaylistLoader getPlaylistLoader() {
        return playlists;
    }

    public NowplayingHandler getNowplayingHandler() {
        return nowplaying;
    }

    public AloneInVoiceHandler getAloneInVoiceHandler() {
        return aloneInVoiceHandler;
    }
    public ChatBot getChatBot() {
        return chatBot;
    }

    public JDA getJDA() {
        return jda;
    }

    public void closeAudioConnection(long guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null)
            threadpool.submit(() -> guild.getAudioManager().closeAudioConnection());
    }

    public void resetGame() {
        Activity game = config.getGame() == null || "none".equalsIgnoreCase(config.getGame().getName()) ? null : config.getGame();
        if (!Objects.equals(jda.getPresence().getActivity(), game))
            jda.getPresence().setActivity(game);
    }

    public void shutdown() {
        if (shuttingDown)
            return;
        shuttingDown = true;
        threadpool.shutdownNow();
        if (jda.getStatus() != JDA.Status.SHUTTING_DOWN) {
            jda.getGuilds().forEach(g -> {
                g.getAudioManager().closeAudioConnection();
                AudioHandler ah = (AudioHandler) g.getAudioManager().getSendingHandler();
                if (ah != null) {
                    ah.stopAndClear();
                    ah.getPlayer().destroy();
//                    nowplaying.updateTopic(g.getIdLong(), ah, true);
                }
            });
            jda.shutdown();
        }
        if (gui != null)
            gui.dispose();
        System.exit(0);
    }

    public void setJDA(JDA j) {
        jda = j;
    }

    public void setGUI(GUI g) {
        gui = g;
    }
}
