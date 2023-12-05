// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
// Copyright 2021 John Grosh (jagrosh) <john.a.grosh@gmail.com>.
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
import a9lim.raiko.utils.GuildUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AloneInVoiceHandler {
    private final Bot bot;
    private final HashMap<Long, Instant> aloneSince = new HashMap<>();
    private long aloneTimeUntilStop;

    public AloneInVoiceHandler(Bot b) {
        bot = b;
    }

    public void init() {
        if ((aloneTimeUntilStop = bot.getConfig().getAloneTimeUntilStop()) > 0)
            bot.getThreadpool().scheduleWithFixedDelay(this::check, 0, 5, TimeUnit.SECONDS);
    }

    private void check() {
        Set<Long> toRemove = new HashSet<>();
        aloneSince.forEach((key, value) -> {
            if (value.getEpochSecond() > Instant.now().getEpochSecond() - aloneTimeUntilStop)
                return;

            Guild guild = bot.getJDA().getGuildById(key);

            if (guild == null) {
                toRemove.add(key);
                return;
            }

            ((AudioHandler) guild.getAudioManager().getSendingHandler()).stopAndClear();
            guild.getAudioManager().closeAudioConnection();

            toRemove.add(key);
        });
        toRemove.forEach(aloneSince::remove);
    }

    public void onVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (aloneTimeUntilStop > 0) {
            Guild guild = event.getEntity().getGuild();
            if (GuildUtil.hasHandler(guild)) {
                boolean alone = GuildUtil.isAlone(guild);
                boolean inList = aloneSince.containsKey(guild.getIdLong());

                if (!alone && inList)
                    aloneSince.remove(guild.getIdLong());
                else if (alone && !inList)
                    aloneSince.put(guild.getIdLong(), Instant.now());
            }
        }
    }

}
