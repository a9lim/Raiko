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

package hayashi.raiko.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import hayashi.raiko.Bot;
import hayashi.raiko.entities.Pair;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NowplayingHandler {
    private final Bot bot;
    private final HashMap<Long, Pair<Long,Long>> lastNP; // guild -> channel,message
    
    public NowplayingHandler(Bot b) {
        bot = b;
        lastNP = new HashMap<>();
    }
    
    public void init() {
        if(!bot.getConfig().useNPImages())
            bot.getThreadpool().scheduleWithFixedDelay(this::updateAll, 0, 5, TimeUnit.SECONDS);
    }
    
    public void setLastNPMessage(Message m) {
        lastNP.put(m.getGuild().getIdLong(), new Pair<>(m.getChannel().asTextChannel().getIdLong(), m.getIdLong()));
    }
    
    public void clearLastNPMessage(Guild guild) {
        lastNP.remove(guild.getIdLong());
    }
    
    private void updateAll() {
        Set<Long> toRemove = new HashSet<>();
        for(Map.Entry<Long, Pair<Long, Long>> entry : lastNP.entrySet()) {
            long guildId = entry.getKey();
            Guild guild = bot.getJDA().getGuildById(guildId);
            if(guild==null) {
                toRemove.add(guildId);
                continue;
            }
            Pair<Long,Long> pair = entry.getValue();
            TextChannel tc = guild.getTextChannelById(pair.key());
            if(tc==null) {
                toRemove.add(guildId);
                continue;
            }
            AudioHandler handler = (AudioHandler)guild.getAudioManager().getSendingHandler();
            MessageCreateData msg = handler.getNowPlaying(bot.getJDA());
            if(msg==null) {
                msg = handler.getNoMusicPlaying(bot.getJDA());
                toRemove.add(guildId);
            }
            try {
                tc.editMessageById(pair.value(), MessageEditData.fromCreateData(msg)).queue(m->{}, t -> lastNP.remove(guildId));
            } catch(Exception e) {
                toRemove.add(guildId);
            }
        }
        toRemove.forEach(lastNP::remove);
    }

    //rework
    public void updateTopic(long guildId, AudioHandler handler, boolean wait) {
        Guild guild = bot.getJDA().getGuildById(guildId);
        if(guild==null)
            return;
        TextChannel tchan = bot.getSettingsManager().getSettings(guildId).getTextChannel(guild);
        if(tchan!=null && guild.getSelfMember().hasPermission(tchan, Permission.MANAGE_CHANNEL)) {
            String otherText;
            String topic = tchan.getTopic();
            if(topic==null || topic.isEmpty())
                otherText = "\u200B";
            else if(topic.contains("\u200B"))
                otherText = topic.substring(topic.lastIndexOf('\u200B'));
            else
                otherText = "\u200B\n "+topic;
            String text = handler.getTopicFormat(bot.getJDA()) + otherText;
            if(!text.equals(tchan.getTopic()))
                try {
                    // normally here if 'wait' was false, we'd want to queue, however,
                    // new discord ratelimits specifically limiting changing channel topics
                    // mean we don't want a backlog of changes piling up, so if we hit a 
                    // ratelimit, we just won't change the topic this time
                    tchan.getManager().setTopic(text).complete(wait);
                } catch(PermissionException | RateLimitedException ignore) {}
        }
    }
    
    // "event"-based methods
    public void onTrackUpdate(long guildId, AudioTrack track, AudioHandler handler) {
        // update bot status if applicable
        if(bot.getConfig().getSongInStatus()) {
            if(track!=null && bot.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inAudioChannel()).count()<=1)
                bot.getJDA().getPresence().setActivity(Activity.listening(track.getInfo().title));
            else
                bot.resetGame();
        }
        // update channel topic if applicable
        updateTopic(guildId, handler, false);
    }
    
    public void onMessageDelete(Guild guild, long messageId) {
        Pair<Long,Long> pair = lastNP.get(guild.getIdLong());
        if(pair != null && pair.value() == messageId)
            lastNP.remove(guild.getIdLong());
    }
}
