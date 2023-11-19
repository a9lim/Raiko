/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
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
package hayashi.raiko.audio;

import hayashi.raiko.Bot;
import hayashi.raiko.entities.Pair;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

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
        lastNP.put(m.getGuild().getIdLong(), new Pair<>(m.getTextChannel().getIdLong(), m.getIdLong()));
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
            Message msg = handler.getNowPlaying(bot.getJDA());
            if(msg==null) {
                msg = handler.getNoMusicPlaying(bot.getJDA());
                toRemove.add(guildId);
            }
            try {
                tc.editMessageById(pair.value(), msg).queue(m->{}, t -> lastNP.remove(guildId));
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
            if(track!=null && bot.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inVoiceChannel()).count()<=1)
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
