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

package a9lim.raiko.commands;

import a9lim.jdautilities.command.Command;
import net.dv8tion.jda.api.Permission;

public abstract class AdminCommand extends Command {
    public AdminCommand() {
        category = new Category("Admin", event -> event.getAuthor().getId().equals(event.getClient().getOwnerId()) || event.getGuild() == null || event.getMember().hasPermission(Permission.MANAGE_SERVER));
        guildOnly = true;
    }
}
