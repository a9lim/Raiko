// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>.
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

package hayashi.raiko.commands.chat;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.chat.ChatBot;
import hayashi.raiko.commands.ChatCommand;

public class RemoveChatCmd extends ChatCommand {

    public RemoveChatCmd(Bot bot) {
        super(bot);
        name = "removechat";
        help = "delete a certain message from raiko's memory";
        arguments = "<position>";
        aliases = bot.getConfig().getAliases(name);
    }
    @Override
    protected void execute(CommandEvent event) {
        try {
            chatBot.remove(Integer.parseInt(event.getArgs()));
            event.replySuccess("Message removed!");
        } catch (Exception e){
            event.replyError("Please input a valid number!");
        }
    }
}
