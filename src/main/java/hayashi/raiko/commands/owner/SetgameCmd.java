/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
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
package hayashi.raiko.commands.owner;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.commands.OwnerCommand;
import net.dv8tion.jda.api.entities.Activity;

public class SetgameCmd extends OwnerCommand {
    public SetgameCmd(Bot bot) {
        name = "setgame";
        help = "sets the game the bot is playing";
        arguments = "[action] [game]";
        aliases = bot.getConfig().getAliases(name);
        guildOnly = false;
        children = new OwnerCommand[]{
                new SetlistenCmd(),
                new SetstreamCmd(),
                new SetwatchCmd()
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        String title = event.getArgs().toLowerCase().startsWith("playing") ? event.getArgs().substring(7).trim() : event.getArgs();
        try {
            event.getJDA().getPresence().setActivity(title.isEmpty() ? null : Activity.playing(title));
            event.replySuccess(" **" + event.getSelfUser().getName()
                    + "** is " + (title.isEmpty() ? "no longer playing anything." : "now playing `" + title + "`"));
        } catch (Exception e) {
            event.replyError(" The game could not be set!");
        }
    }

    private static class SetstreamCmd extends OwnerCommand {

        private SetstreamCmd() {
            name = "stream";
            aliases = new String[]{"twitch", "streaming"};
            help = "sets the game the bot is playing to a stream";
            arguments = "<username> <game>";
            guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] parts = COMPILE.split(event.getArgs(), 2);
            if (parts.length < 2) {
                event.replyError("Please include a twitch username and the name of the game to 'stream'");
                return;
            }
            try {
                event.getJDA().getPresence().setActivity(Activity.streaming(parts[1], "https://twitch.tv/" + parts[0]));
                event.replySuccess("**" + event.getSelfUser().getName()
                        + "** is now streaming `" + parts[1] + "`");
            } catch (Exception e) {
                event.replyError(" The game could not be set!");
            }
        }
    }

    private static class SetlistenCmd extends OwnerCommand {
        private SetlistenCmd() {
            name = "listen";
            aliases = new String[]{"listening"};
            help = "sets the game the bot is listening to";
            arguments = "<title>";
            guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.replyError("Please include a title to listen to!");
                return;
            }
            String title = event.getArgs().toLowerCase().startsWith("to") ? event.getArgs().substring(2).trim() : event.getArgs();
            try {
                event.getJDA().getPresence().setActivity(Activity.listening(title));
                event.replySuccess("**" + event.getSelfUser().getName() + "** is now listening to `" + title + "`");
            } catch (Exception e) {
                event.replyError(" The game could not be set!");
            }
        }
    }

    private static class SetwatchCmd extends OwnerCommand {
        private SetwatchCmd() {
            name = "watch";
            aliases = new String[]{"watching"};
            help = "sets the game the bot is watching";
            arguments = "<title>";
            guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.replyError("Please include a title to watch!");
                return;
            }
            String title = event.getArgs();
            try {
                event.getJDA().getPresence().setActivity(Activity.watching(title));
                event.replySuccess("**" + event.getSelfUser().getName() + "** is now watching `" + title + "`");
            } catch (Exception e) {
                event.replyError( " The game could not be set!");
            }
        }
    }
}
