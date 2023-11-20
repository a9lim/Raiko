// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
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


package a9lim.raiko.commands;

import a9lim.jdautilities.command.Command;
import a9lim.raiko.Bot;
import a9lim.raiko.chat.ChatBot;

public abstract class ChatCommand extends Command {
    protected final ChatBot chatBot;
    public ChatCommand(Bot bot){
        chatBot = bot.getChatBot();
        guildOnly = true;
        category = new Category("Chat");
    }
}
