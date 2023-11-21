// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
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


package a9lim.raiko.commands.general;

import a9lim.jdautilities.command.Command;
import a9lim.jdautilities.command.CommandClient;
import a9lim.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;

import java.util.Objects;

public class HelpCmd extends Command {
    private final CommandClient client;

    public HelpCmd(CommandClient c) {
        name = "help";
        help = "lists commands";
        category = new Category("General");
        guildOnly = false;
        client = c;
    }

    @Override
    protected void execute(CommandEvent event) {
        StringBuilder builder = new StringBuilder("**" + event.getSelfUser().getName() + "** commands:\n");
        Category category = null;
        for (Command command : client.getCommands()) {
            if (!command.isHidden() && (!command.isOwnerCommand() || event.isOwner())) {
                if (!Objects.equals(category, command.getCategory())) {
                    category = command.getCategory();
                    builder.append("\n\n  __").append(category == null ? "No Category" : category.name()).append("__:\n");
                }
                builder.append("\n`").append(client.getDefaultPrefix()).append(command.getName())
                        .append(command.getArguments() == null ? "`" : " " + command.getArguments() + "`")
                        .append(" - ").append(command.getHelp());
            }
        }
        User owner = event.getJDA().getUserById(client.getOwnerId());
        if (owner != null) {
            builder.append("\n\nFor additional help, contact **").append(owner.getName()).append("**#").append(owner.getDiscriminator());
            if (client.getServerInvite() != null)
                builder.append(" or join ").append(client.getServerInvite());
        }
        event.replyInDm(builder.toString(), unused -> {
            if (event.isFromType(ChannelType.TEXT))
                event.reactSuccess();
        }, t -> event.replyWarning("Help cannot be sent because you are blocking Direct Messages."));
    }

}
