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
import hayashi.raiko.commands.MusicCommand;
import hayashi.raiko.settings.Settings;
import hayashi.raiko.utils.FormatUtil;

public class VolumeCmd extends MusicCommand {
    public VolumeCmd(Bot bot) {
        super(bot);
        this.name = "volume";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.help = "sets or shows volume";
        this.arguments = "[0-150]";
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        int volume = handler.getPlayer().getVolume();
        if (event.getArgs().isEmpty()) {
            event.reply(FormatUtil.volumeIcon(volume) + " Current volume is `" + volume + "`");
            return;
        }
        int nvolume;
        try {
            nvolume = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException e) {
            nvolume = -1;
        }
        if (nvolume < 0 || nvolume > 150) {
            event.reply(event.getClient().getError() + " Volume must be a valid integer between 0 and 150!");
            return;
        }
        handler.getPlayer().setVolume(nvolume);
        ((Settings)event.getClient().getSettingsFor(event.getGuild())).setVolume(nvolume);
        event.reply(FormatUtil.volumeIcon(nvolume) + " Volume changed from `" + volume + "` to `" + nvolume + "`");
    }

}
