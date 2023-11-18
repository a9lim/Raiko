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
package hayashi.raiko.commands.music;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.audio.AudioHandler;
import hayashi.raiko.audio.RequestMetadata;
import hayashi.raiko.commands.MusicCommand;

public class SkipCmd extends MusicCommand {
    public SkipCmd(Bot bot) {
        super(bot);
        this.name = "skip";
        this.help = "skips songs";
        this.arguments = "<position>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        int index;
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        try {
            index = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException e) {
            RequestMetadata rm = handler.getRequestMetadata();
            event.reply(event.getClient().getSuccess() + " Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title
                    + "** " + (rm.getOwner() == 0L ? "(autoplay)" : "(requested by **" + rm.user.username + "**)"));
            handler.getPlayer().stopTrack();
            return;
        }
        if (index < 1 || index > handler.getQueue().size()) {
            event.reply(event.getClient().getError() + " Position must be a valid integer between 1 and " + handler.getQueue().size() + "!");
            return;
        }
        handler.getQueue().skip(index - 1);
        event.reply(event.getClient().getSuccess() + " Skipped to **" + handler.getQueue().get(0).getTrack().getInfo().title + "**");
        handler.getPlayer().stopTrack();
    }
}
