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

package hayashi.raiko.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import hayashi.raiko.playlist.PlaylistLoader.Playlist;
import hayashi.raiko.queue.DoubleDealingQueue;
import hayashi.raiko.settings.RepeatMode;
import hayashi.raiko.settings.Settings;
import hayashi.raiko.utils.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;

public class AudioHandler extends AudioEventAdapter implements AudioSendHandler {
    public final static String PLAY_EMOJI = "\u25B6"; // ▶
    public final static String PAUSE_EMOJI = "\u23F8"; // ⏸
    public final static String STOP_EMOJI = "\u23F9"; // ⏹

    private final DoubleDealingQueue<QueuedTrack> queue = new DoubleDealingQueue<>();
    private final ArrayDeque<AudioTrack> defaultQueue = new ArrayDeque<>();
    private final PlayerManager manager;
    private final AudioPlayer audioPlayer;
    private final long guildId;

    private AudioFrame lastFrame;

    protected AudioHandler(PlayerManager man, Guild guild, AudioPlayer player) {
        manager = man;
        audioPlayer = player;
        guildId = guild.getIdLong();
    }

    public void pushTrack(QueuedTrack qtrack) {
        if (audioPlayer.getPlayingTrack() == null)
            audioPlayer.playTrack(qtrack.getTrack());
        else
            queue.push(qtrack);
    }

    public void addTrack(QueuedTrack qtrack) {
        if (audioPlayer.getPlayingTrack() == null)
            audioPlayer.playTrack(qtrack.getTrack());
        else
            queue.add(qtrack);
    }

    public DoubleDealingQueue<QueuedTrack> getQueue() {
        return queue;
    }

    public void stopAndClear() {
        queue.clear();
        defaultQueue.clear();
        audioPlayer.stopTrack();
        //current = null;
    }

    public boolean noMusicPlaying(JDA jda) {
        return !guild(jda).getSelfMember().getVoiceState().inAudioChannel() || audioPlayer.getPlayingTrack() == null;
    }

    public AudioPlayer getPlayer() {
        return audioPlayer;
    }

    public RequestMetadata getRequestMetadata() {
        if (audioPlayer.getPlayingTrack() == null)
            return RequestMetadata.EMPTY;
        RequestMetadata rm = audioPlayer.getPlayingTrack().getUserData(RequestMetadata.class);
        return rm == null ? RequestMetadata.EMPTY : rm;
    }

    public boolean playFromDefault() {
        if (!defaultQueue.isEmpty()) {
            audioPlayer.playTrack(defaultQueue.remove());
            return true;
        }
        Settings settings = manager.getBot().getSettingsManager().getSettings(guildId);
        if (settings == null || settings.getDefaultPlaylist() == null)
            return false;

        Playlist pl = manager.getBot().getPlaylistLoader().getPlaylist(settings.getDefaultPlaylist());
        if (pl == null || pl.getItems().isEmpty())
            return false;
        pl.loadTracks(manager, (at) -> {
            if (audioPlayer.getPlayingTrack() == null)
                audioPlayer.playTrack(at);
            else
                defaultQueue.add(at);
        }, () -> {
            if (pl.getTracks().isEmpty() && !manager.getBot().getConfig().getStay())
                manager.getBot().closeAudioConnection(guildId);
        });
        return true;
    }

    // Audio Events
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        RepeatMode repeatMode = manager.getBot().getSettingsManager().getSettings(guildId).getRepeatMode();
        // if the track ended normally, and we're in repeat mode, re-add it to the queue
        if (endReason == AudioTrackEndReason.FINISHED && repeatMode != RepeatMode.OFF) {
            QueuedTrack clone = new QueuedTrack(track.makeClone(), track.getUserData(RequestMetadata.class));
            if (repeatMode == RepeatMode.ALL)
                queue.add(clone);
            else
                queue.push(clone);
        }

        if (!queue.isEmpty()) {
            QueuedTrack qt = queue.pop();
            player.playTrack(qt.getTrack());
            return;
        }
        if (!playFromDefault()) {
            manager.getBot().getNowplayingHandler().onTrackUpdate(guildId, null, this);
            if (!manager.getBot().getConfig().getStay())
                manager.getBot().closeAudioConnection(guildId);
            // unpause, in the case when the player was paused and the track has been skipped.
            // this is to prevent the player being paused next time it's being used.
            player.setPaused(false);
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        manager.getBot().getNowplayingHandler().onTrackUpdate(guildId, track, this);
    }


    // Formatting
    public MessageCreateData getNowPlaying(JDA jda) {
        if (noMusicPlaying(jda))
            return null;
        Guild guild = guild(jda);
        AudioTrack track = audioPlayer.getPlayingTrack();
        MessageCreateBuilder mb = new MessageCreateBuilder();
        mb.setContent(FormatUtil.filter(manager.getBot().getConfig().getSuccess() + " **Now Playing in " + guild.getSelfMember().getVoiceState().getChannel().getAsMention() + "...**"));
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(guild.getSelfMember().getColor());
        RequestMetadata rm = getRequestMetadata();
        if (rm.getOwner() != 0L) {
            User u = guild.getJDA().getUserById(rm.user.id);
            if (u == null)
                eb.setAuthor(rm.user.username + "#" + rm.user.discrim, null, rm.user.avatar);
            else
                eb.setAuthor(u.getName() + "#" + u.getDiscriminator(), null, u.getEffectiveAvatarUrl());
        }

        try {
            eb.setTitle(track.getInfo().title, track.getInfo().uri);
        } catch (Exception e) {
            eb.setTitle(track.getInfo().title);
        }

        if (track instanceof YoutubeAudioTrack && manager.getBot().getConfig().useNPImages())
            eb.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/mqdefault.jpg");

        if (track.getInfo().author != null && !track.getInfo().author.isEmpty())
            eb.setFooter("Source: " + track.getInfo().author, null);
        eb.setDescription(getStatusEmoji()
                + " " + FormatUtil.progressBar((double) audioPlayer.getPlayingTrack().getPosition() / track.getDuration())
                + " `[" + FormatUtil.formatTime(track.getPosition()) + "/" + FormatUtil.formatTime(track.getDuration()) + "]` "
                + FormatUtil.volumeIcon(audioPlayer.getVolume()));

        return mb.setEmbeds(eb.build()).build();
    }

    public MessageCreateData getNoMusicPlaying(JDA jda) {
        return new MessageCreateBuilder()
                .setContent(FormatUtil.filter(manager.getBot().getConfig().getSuccess() + " **Now Playing...**"))
                .setEmbeds(new EmbedBuilder()
                        .setTitle("No music playing")
                        .setDescription(STOP_EMOJI + " " + FormatUtil.progressBar(-1) + " " + FormatUtil.volumeIcon(audioPlayer.getVolume()))
                        .setColor(guild(jda).getSelfMember().getColor())
                        .build()).build();
    }

    public String getTopicFormat(JDA jda) {
        if (noMusicPlaying(jda))
            return "No music playing " + STOP_EMOJI + " " + FormatUtil.volumeIcon(audioPlayer.getVolume());
        long userid = getRequestMetadata().getOwner();
        AudioTrack track = audioPlayer.getPlayingTrack();
        String title = track.getInfo().title;
        if (title == null || "Unknown Title".equals(title))
            title = track.getInfo().uri;
        return "**" + title + "** [" + (userid == 0 ? "autoplay" : "<@" + userid + ">") + "]"
                + "\n" + getStatusEmoji() + " "
                + "[" + FormatUtil.formatTime(track.getDuration()) + "] "
                + FormatUtil.volumeIcon(audioPlayer.getVolume());
    }

    public String getStatusEmoji() {
        return audioPlayer.isPaused() ? PAUSE_EMOJI : PLAY_EMOJI;
    }

    // Audio Send Handler methods
    /*@Override
    public boolean canProvide() 
    {
        if (lastFrame == null)
            lastFrame = audioPlayer.provide();

        return lastFrame != null;
    }

    @Override
    public byte[] provide20MsAudio() 
    {
        if (lastFrame == null) 
            lastFrame = audioPlayer.provide();

        byte[] data = lastFrame != null ? lastFrame.getData() : null;
        lastFrame = null;

        return data;
    }*/

    @Override
    public boolean canProvide() {
        return (lastFrame = audioPlayer.provide()) != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastFrame.getData());
    }

    // bruh
    @Override
    public boolean isOpus() {
        return true;
    }


    // Private methods
    private Guild guild(JDA jda) {
        return jda.getGuildById(guildId);
    }
}
