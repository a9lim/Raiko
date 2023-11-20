// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>.
// Copyright 2017 John Grosh (jagrosh) <john.a.grosh@gmail.com>.
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
