// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
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

package a9lim.raiko.commands.owner;

import a9lim.jdautilities.command.CommandEvent;
import a9lim.raiko.commands.OwnerCommand;
import a9lim.raiko.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Icon;

import java.io.IOException;
import java.io.InputStream;

public class SetavatarCmd extends OwnerCommand {
    public SetavatarCmd() {
        name = "setavatar";
        help = "sets the avatar of the bot";
        arguments = "<url>";
        guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        InputStream s = OtherUtil.imageFromUrl(event.getArgs().isEmpty() ?
                (!event.getMessage().getAttachments().isEmpty() && event.getMessage().getAttachments().getFirst().isImage() ?
                        event.getMessage().getAttachments().getFirst().getUrl() :
                        null) :
                event.getArgs());
        if (s == null) {
            event.replyError(" Invalid or missing URL");
            return;
        }
        try {
            event.getSelfUser().getManager().setAvatar(Icon.from(s)).queue(
                    v -> event.replySuccess(" Successfully changed avatar."),
                    t -> event.replyError(" Failed to set avatar."));
        } catch (IOException e) {
            event.replyError(" Could not load from provided URL.");
        }
    }
}
