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
package hayashi.raiko.commands.music;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.commands.MusicCommand;
import hayashi.raiko.settings.RepeatMode;
import hayashi.raiko.settings.Settings;

public class RepeatCmd extends MusicCommand {
    public RepeatCmd(Bot bot) {
        super(bot);
        this.name = "repeat";
        this.help = "re-adds music to the queue when finished";
        this.arguments = "[off|all|single]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }

    // override musiccommand's execute because we don't actually care where this is used
    @Override
    protected void execute(CommandEvent event) {
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        settings.setRepeatMode(switch(event.getArgs().toLowerCase()) {
            case "false", "off" -> RepeatMode.OFF;
            case "true", "on", "all" -> RepeatMode.ALL;
            case "one", "single" -> RepeatMode.SINGLE;
            default -> settings.getRepeatMode() == RepeatMode.OFF ? RepeatMode.ALL : RepeatMode.OFF;
        });
        event.replySuccess("Repeat mode is now `" + settings.getRepeatMode().getUserFriendlyName() + "`");
    }

    @Override
    public void doCommand(CommandEvent event) { /* Intentionally Empty */ }
}
